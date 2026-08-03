package dev.revere.alley.adapter.core;

import dev.revere.alley.bootstrap.lifecycle.Service;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface CoreAdapter extends Service {

    /**
     * Gets the active core implementation that was detected during startup.
     * 获取在启动期间检测到的活动核心实现。
     *
     * @return The Core implementation for the currently enabled core bootstrap.
     *         当前启用的核心引导程序的 Core 实现。
     */
    Core getCore();
}