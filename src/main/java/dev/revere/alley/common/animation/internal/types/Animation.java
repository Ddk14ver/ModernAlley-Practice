package dev.revere.alley.common.animation.internal.types;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 03/04/2025
 */
public abstract class Animation {
    private final List<String> frames;

    private final long updateInterval;
    private long lastUpdateTime;

    private int frameIndex;

    /**
     * Constructor for the AbstractAnimation class.
     * AbstractAnimation 类的构造函数。
     *
     * @param frames         The frames of the animation.
     *                       动画的帧。
     * @param updateInterval The interval in milliseconds between animation frames.
     *                       动画帧之间的间隔（以毫秒为单位）。
     */
    protected Animation(List<String> frames, long updateInterval) {
        this.frames = frames;
        this.updateInterval = updateInterval;
        this.frameIndex = 0;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Gets the current animation frame based on timing logic.
     * 基于时间逻辑获取当前动画帧。
     *
     * @return The current animation frame.
     *         当前动画帧。
     */
    public String getCurrentFrame() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastUpdateTime >= this.updateInterval) {
            this.frameIndex = (this.frameIndex + 1) % this.frames.size();
            this.lastUpdateTime = currentTime;
        }
        return this.frames.get(this.frameIndex);
    }
}