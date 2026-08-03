package dev.revere.alley.common.animation.internal.config;

import dev.revere.alley.common.TaskUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @date 27/03/2024 - 14:55
 */
@Getter
@Setter
public class TextAnimation {
    private String text;
    private List<String> animation;
    private boolean animated;
    private long ticks;
    private int index;

    /**
     * Constructor for the TextAnimation class.
     * TextAnimation 类的构造函数。
     *
     * @param config The configuration file.
     *              配置文件。
     * @param path   The config path where the animation settings are stored.
     *              存储动画设置的配置路径。
     */
    public TextAnimation(FileConfiguration config, String path) {
        this.text = config.getString(path + ".text", "null");
        this.animated = config.getBoolean(path + ".animated", false);
        this.ticks = config.getLong(path + ".ticks", 10L);
        this.animation = config.getStringList(path + ".animation");
        this.index = 0;

        if (this.animated && !this.animation.isEmpty()) {
            this.runAnimation();
        }
    }

    private void runAnimation() {
        TaskUtil.runTaskTimer(() -> {
            this.text = this.animation.get(this.index);
            this.index++;
            if (this.index == this.animation.size()) this.index = 0;
        }, this.ticks, this.ticks);
    }
}