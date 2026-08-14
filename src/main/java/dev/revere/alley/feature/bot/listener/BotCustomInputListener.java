package dev.revere.alley.feature.bot.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.bot.CustomBotProfile;
import dev.revere.alley.feature.bot.internal.BotServiceImpl;
import dev.revere.alley.feature.bot.menu.CustomBotMenu;
import dev.revere.alley.feature.bot.match.BotMatchSession;
import dev.revere.alley.feature.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BotCustomInputListener implements Listener {
    private static final Map<UUID, PendingInput> PENDING = new ConcurrentHashMap<>();

    private final BotServiceImpl service;

    public BotCustomInputListener(BotServiceImpl service) {
        this.service = service;
    }

    public static void request(Player player, Kit kit, InputType type) {
        PENDING.put(player.getUniqueId(), new PendingInput(kit, type));
        player.closeInventory();
        player.sendMessage(CC.translate(type == InputType.NAME
                ? "&eEnter a valid bot name in chat, or type &ccancel&e."
                : "&eEnter a premium Minecraft player ID for the skin, or type &ccancel&e."));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        PendingInput pending = PENDING.remove(event.getPlayer().getUniqueId());
        if (pending == null) return;
        event.setCancelled(true);
        String input = event.getMessage().trim();
        Bukkit.getScheduler().runTask(AlleyPlugin.getInstance(),
                () -> handleInput(event.getPlayer(), pending, input));
    }

    private void handleInput(Player player, PendingInput pending, String input) {
        if (!player.isOnline()) return;
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(CC.translate("&cCustom bot editing cancelled."));
            new CustomBotMenu(pending.kit()).openMenu(player);
            return;
        }
        if (!CustomBotProfile.isValidName(input)) {
            PENDING.put(player.getUniqueId(), pending);
            player.sendMessage(CC.translate("&cUse 1-16 letters, numbers, or underscores. Try again or type cancel."));
            return;
        }

        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class)
                .getProfile(player.getUniqueId());
        CustomBotProfile custom = profile.getProfileData().getCustomBotProfile();
        if (custom == null) {
            custom = new CustomBotProfile();
            profile.getProfileData().setCustomBotProfile(custom);
        }
        if (pending.type() == InputType.NAME) {
            Player nameOwner = Bukkit.getPlayerExact(input);
            if (nameOwner != null && !nameOwner.getScoreboardTags().contains(BotMatchSession.BOT_ENTITY_TAG)) {
                PENDING.put(player.getUniqueId(), pending);
                player.sendMessage(CC.translate("&cThat name is currently in use. Try another name or type cancel."));
                return;
            }
            custom.setName(input);
            service.queueCustomProfileSave(profile);
            player.sendMessage(CC.translate("&aCustom bot name set to &e" + input + "&a."));
            new CustomBotMenu(pending.kit()).openMenu(player);
            return;
        }

        player.sendMessage(CC.translate("&eDownloading and validating that premium skin..."));
        CustomBotProfile finalCustom = custom;
        service.resolveSkin(input).whenComplete((skin, throwable) ->
                Bukkit.getScheduler().runTask(AlleyPlugin.getInstance(), () -> {
                    if (!player.isOnline()) return;
                    if (throwable != null || skin == null) {
                        PENDING.put(player.getUniqueId(), pending);
                        player.sendMessage(CC.translate("&cThat premium player or skin could not be found. Try again or type cancel."));
                        return;
                    }
                    finalCustom.setSkinName(input);
                    service.queueCustomProfileSave(profile);
                    player.sendMessage(CC.translate("&aCustom bot skin set to &e" + input + "&a."));
                    new CustomBotMenu(pending.kit()).openMenu(player);
                }));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PENDING.remove(event.getPlayer().getUniqueId());
    }

    public enum InputType {
        NAME,
        SKIN
    }

    private record PendingInput(Kit kit, InputType type) {
    }
}
