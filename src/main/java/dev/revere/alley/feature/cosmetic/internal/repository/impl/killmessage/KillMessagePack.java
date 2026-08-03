package dev.revere.alley.feature.cosmetic.internal.repository.impl.killmessage;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.common.text.CC;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @author 作者 Remi
 * @project alley-practice
 * @project 项目 alley-practice
 * @date 27/06/2025
 * @date 日期 27/06/2025
 */
public abstract class KillMessagePack extends BaseCosmetic {
    private final Map<EntityDamageEvent.DamageCause, List<String>> messagesByCause = new EnumMap<>(EntityDamageEvent.DamageCause.class);
    private List<String> genericMessages = Collections.emptyList();

    public KillMessagePack() {
        super();
        this.loadMessages();
    }

    /**
     * Concrete classes must implement this to specify which .yml file to load.
     * 具体类必须实现此方法以指定要加载的.yml文件。
     *
     * @return The name of the resource file (e.g., "salty_messages.yml").
     * @return 资源文件的名称（例如 "salty_messages.yml"）。
     */
    protected abstract String getResourceFileName();

    private void loadMessages() {
        String fileName = getResourceFileName();
        if (fileName == null || fileName.isEmpty()) {
            Logger.error("Kill message pack tried to load with empty or null file name. Aborting loading.");
            return;
        }

        String configPath = "cosmetics/messages/" + fileName;

        ConfigService configService = AlleyPlugin.getInstance().getService(ConfigService.class);
        if (configService == null) {
            Logger.error("ConfigService is null when loading " + fileName + ". Service not available!");
            return;
        }

        FileConfiguration config = configService.getConfig(configPath);
        if (config == null) {
            Logger.error("Could not load kill message config: " + configPath);
            Logger.error("Make sure the file is added to ConfigService.configFileNames array!");
            return;
        }

        try {
            for (String key : config.getKeys(false)) {
                try {
                    EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.valueOf(key.toUpperCase());
                    this.messagesByCause.put(cause, config.getStringList(key));
                } catch (IllegalArgumentException e) {
                    if (key.equalsIgnoreCase("GENERIC")) {
                        this.genericMessages = config.getStringList(key);
                    } else {
                        Logger.error("Unknown damage cause in " + fileName + ": " + key);
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException("Failed to load kill message pack: " + fileName, e);
        }
    }

    /**
     * Gets a random message for a given damage cause, with fallback to GENERIC.
     * 获取给定伤害原因的随机消息，如果不可用则回退到GENERIC。
     *
     * @param cause The cause of death.
     * @param cause 死亡原因。
     * @return A random message string, or null if none are available.
     * @return 一个随机的消息字符串，如果没有可用的则返回null。
     */
    public String getRandomMessage(EntityDamageEvent.DamageCause cause) {
        List<String> messageList = messagesByCause.get(cause);

        if (messageList == null || messageList.isEmpty()) {
            messageList = genericMessages;
        }

        if (messageList == null || messageList.isEmpty()) {
            return null;
        }

        return messageList.get(ThreadLocalRandom.current().nextInt(messageList.size()));
    }

    /**
     * Gathers all message strings from all categories into a single list.
     * 将所有类别的所有消息字符串收集到一个列表中。
     */
    public List<String> getDisplayableMessages() {
        List<String> allMessages = messagesByCause.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));
        allMessages.addAll(genericMessages);

        return Collections.unmodifiableList(allMessages);
    }

    @Override
    public List<String> getDisplayLore() {
        List<String> lore = new ArrayList<>();
        List<String> displayable = getDisplayableMessages();
        if (displayable.isEmpty()) {
            lore.add("&7" + this.getDescription());
        } else {
            for (String message : displayable) {
                lore.add(CC.translate("&f- &6" + message.replace("{victim}", "victim").replace("{killer}", "killer")));
            }
        }
        return lore;
    }
}
