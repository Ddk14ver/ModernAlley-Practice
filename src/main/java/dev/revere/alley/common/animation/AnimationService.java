package dev.revere.alley.common.animation;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.common.animation.internal.config.TextAnimation;
import dev.revere.alley.common.animation.internal.types.Animation;

import java.util.Set;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface AnimationService extends Service {
    /**
     * Gets the set of all discovered internal animations.
     * 获取所有已发现的内部动画的集合。
     *
     * @return An unmodifiable set of internal animations.
     *         一个不可修改的内部动画集合。
     */
    Set<Animation> getInternalAnimations();

    /**
     * Gets the set of all discovered configuration-based text animations.
     * 获取所有已发现的基于配置的文本动画的集合。
     *
     * @return An unmodifiable set of text animations.
     *         一个不可修改的文本动画集合。
     */
    Set<TextAnimation> getConfigAnimations();

    /**
     * Retrieves a specific animation instance by its class and type.
     * 根据其类和类型检索特定的动画实例。
     *
     * @param clazz The class of the animation to retrieve.
     *             要检索的动画的类。
     * @param type  The type of animation (INTERNAL or CONFIG).
     *             动画的类型（INTERNAL 或 CONFIG）。
     * @param <T>   The animation's type.
     *             动画的类型。
     * @return The requested animation instance.
     *         请求的动画实例。
     * @throws IllegalArgumentException if the animation is not found.
     *                                  如果未找到动画。
     */
    <T> T getAnimation(Class<T> clazz, AnimationType type);
}