package dev.revere.alley.feature.match.combat.legacy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import dev.revere.alley.AlleyPlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Keeps server consumption native while spoofing the local 1.8 run-eating state. */
final class LegacyFoodUseController {
    private static final byte USING_ITEM_FLAG = 0x01;
    private static final long CLIENT_USE_INTERRUPT_TICKS = 5L;

    private final Set<UUID> interruptedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> retryBlockedPlayers = ConcurrentHashMap.newKeySet();
    private Method getHandleMethod;
    private Method getEntityDataMethod;
    private Method entityDataGetMethod;
    private Method accessorIdMethod;
    private Field livingFlagsField;
    private Object livingFlagsAccessor;
    private Integer livingFlagsIndex;
    private boolean warningLogged;

    LegacyFoodUseController() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                AlleyPlugin.getInstance(), ListenerPriority.HIGHEST,
                PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                filterUsingMetadata(event);
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                AlleyPlugin.getInstance(), ListenerPriority.HIGHEST,
                PacketType.Play.Client.USE_ITEM) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (interruptedPlayers.contains(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        });
    }

    void interruptUse(Player player) {
        if (!isConsuming(player)) return;

        UUID playerId = player.getUniqueId();
        if (this.retryBlockedPlayers.contains(playerId)
                || !this.interruptedPlayers.add(playerId)) return;

        try {
            sendClientUsingState(player, false);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            this.interruptedPlayers.remove(playerId);
            logWarning(exception);
            return;
        }

        maintainClientInterrupt(player, playerId, CLIENT_USE_INTERRUPT_TICKS - 1L);
    }

    private void maintainClientInterrupt(Player player, UUID playerId, long remainingTicks) {
        AlleyPlugin plugin = AlleyPlugin.getInstance();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!this.interruptedPlayers.contains(playerId)) return;
            if (!player.isOnline() || !player.hasActiveItem()) {
                finishClientInterrupt(player, playerId, false);
                return;
            }
            if (remainingTicks <= 0L) {
                finishClientInterrupt(player, playerId, true);
                return;
            }

            try {
                sendClientUsingState(player, false);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                this.interruptedPlayers.remove(playerId);
                logWarning(exception);
                return;
            }
            maintainClientInterrupt(player, playerId, remainingTicks - 1L);
        }, 1L);
    }

    private void finishClientInterrupt(Player player, UUID playerId, boolean restoreUsingState) {
        this.interruptedPlayers.remove(playerId);
        if (restoreUsingState && player.isOnline() && player.hasActiveItem()) {
            try {
                sendClientUsingState(player, true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                logWarning(exception);
            }
        }

        this.retryBlockedPlayers.add(playerId);
        AlleyPlugin plugin = AlleyPlugin.getInstance();
        plugin.getServer().getScheduler().runTask(plugin,
                () -> this.retryBlockedPlayers.remove(playerId));
    }

    private void filterUsingMetadata(PacketEvent event) {
        Player viewer = event.getPlayer();
        if (!this.interruptedPlayers.contains(viewer.getUniqueId())) return;

        try {
            PacketContainer original = event.getPacket();
            Integer entityId = original.getIntegers().readSafely(0);
            if (entityId == null || entityId != viewer.getEntityId()) return;

            PacketContainer filtered = original.deepClone();
            List<WrappedDataValue> values = filtered.getDataValueCollectionModifier().readSafely(0);
            if (values == null || values.isEmpty()) return;

            int flagsIndex = getLivingFlagsIndex();
            boolean changed = false;
            List<WrappedDataValue> filteredValues = new ArrayList<>(values.size());
            for (WrappedDataValue value : values) {
                Object rawValue = value.getValue();
                if (value.getIndex() == flagsIndex && rawValue instanceof Byte flags) {
                    rawValue = (byte) (flags & ~USING_ITEM_FLAG);
                    changed = true;
                }
                filteredValues.add(new WrappedDataValue(
                        value.getIndex(), value.getSerializer(), rawValue));
            }

            if (changed) {
                filtered.getDataValueCollectionModifier().write(0, filteredValues);
                event.setPacket(filtered);
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            logWarning(exception);
        }
    }

    private void sendClientUsingState(Player player, boolean using) throws ReflectiveOperationException {
        Object handle = getHandle(player);
        Object entityData = getEntityData(handle);
        Object accessor = getLivingFlagsAccessor();
        byte serverFlags = ((Number) getEntityDataValue(entityData, accessor)).byteValue();
        byte clientFlags = using
                ? (byte) (serverFlags | USING_ITEM_FLAG)
                : (byte) (serverFlags & ~USING_ITEM_FLAG);

        var protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, player.getEntityId());
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        packet.getDataValueCollectionModifier().write(0, List.of(
                new WrappedDataValue(getLivingFlagsIndex(), serializer, clientFlags)));
        protocolManager.sendServerPacket(player, packet, false);
    }

    private boolean isConsuming(Player player) {
        if (!player.hasActiveItem()) return false;
        ItemStack activeItem = player.getActiveItem();
        if (activeItem.isEmpty()) return false;

        Consumable consumable = activeItem.getData(DataComponentTypes.CONSUMABLE);
        if (consumable == null) return false;
        ItemUseAnimation animation = consumable.animation();
        return animation == ItemUseAnimation.EAT || animation == ItemUseAnimation.DRINK;
    }

    private Object getHandle(Player player) throws ReflectiveOperationException {
        if (this.getHandleMethod == null) {
            this.getHandleMethod = player.getClass().getMethod("getHandle");
        }
        return this.getHandleMethod.invoke(player);
    }

    private Object getEntityData(Object handle) throws ReflectiveOperationException {
        if (this.getEntityDataMethod == null) {
            this.getEntityDataMethod = handle.getClass().getMethod("getEntityData");
        }
        return this.getEntityDataMethod.invoke(handle);
    }

    private Object getLivingFlagsAccessor() throws ReflectiveOperationException {
        if (this.livingFlagsAccessor == null) {
            Class<?> livingEntityClass = Class.forName("net.minecraft.world.entity.LivingEntity");
            this.livingFlagsField = livingEntityClass.getDeclaredField("DATA_LIVING_ENTITY_FLAGS");
            this.livingFlagsField.setAccessible(true);
            this.livingFlagsAccessor = this.livingFlagsField.get(null);
        }
        return this.livingFlagsAccessor;
    }

    private int getLivingFlagsIndex() throws ReflectiveOperationException {
        if (this.livingFlagsIndex == null) {
            Object accessor = getLivingFlagsAccessor();
            if (this.accessorIdMethod == null) {
                this.accessorIdMethod = accessor.getClass().getMethod("id");
            }
            this.livingFlagsIndex = ((Number) this.accessorIdMethod.invoke(accessor)).intValue();
        }
        return this.livingFlagsIndex;
    }

    private Object getEntityDataValue(Object entityData, Object accessor) throws ReflectiveOperationException {
        if (this.entityDataGetMethod == null) {
            Class<?> accessorClass = Class.forName("net.minecraft.network.syncher.EntityDataAccessor");
            this.entityDataGetMethod = entityData.getClass().getMethod("get", accessorClass);
        }
        return this.entityDataGetMethod.invoke(entityData, accessor);
    }

    private void logWarning(Throwable exception) {
        if (this.warningLogged) return;
        this.warningLogged = true;
        AlleyPlugin.getInstance().getLogger().warning(
                "Unable to control legacy run-eating metadata: "
                        + exception.getClass().getSimpleName());
    }
}
