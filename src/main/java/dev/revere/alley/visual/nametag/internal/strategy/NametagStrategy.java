package dev.revere.alley.visual.nametag.internal.strategy;

import dev.revere.alley.visual.nametag.model.NametagContext;
import dev.revere.alley.visual.nametag.NametagView;

/**
 * 名字标签策略接口，定义根据上下文创建 NametagView 的契约。
 * @author Remi
 * @project alley-practice
 * @date 27/06/2025
 */
public interface NametagStrategy {
    /**
     * Creates a NametagView based on the given context.
     * 根据给定的上下文创建 NametagView。
     *
     * @param context The context containing the viewer and target.
     *        包含观察者和目标的上下文。
     * @return A NametagView if this strategy applies, otherwise null.
     *         如果此策略适用则返回 NametagView，否则返回 null。
     */
    NametagView createNametagView(NametagContext context);
}