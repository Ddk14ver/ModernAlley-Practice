package dev.revere.alley.bootstrap;

import dev.revere.alley.bootstrap.lifecycle.Service;

import java.io.IOException;

/**
 * @author Remi
 * @project alley-practice
 * @date 24/07/2025
 */
public interface UpdaterService extends Service {
    /**
     * Checks for updates to the bootstrap by comparing the current version with the latest version available on GitHub.
     * If an update is available, it logs a warning message.
     * 通过将当前版本与 GitHub 上的最新版本进行比较来检查引导程序更新。
     * 如果有更新可用，则记录警告消息。
     */
    void checkForUpdates();

    /**
     * Downloads the latest version of the bootstrap and updates it.
     * 下载最新版本的引导程序并进行更新。
     *
     * @param version The version to download and update to.
     *        要下载并更新到的版本。
     */
    void downloadAndUpdate(String version);

    /**
     * Fetches the latest version of the bootstrap from GitHub.
     * 从 GitHub 获取引导程序的最新版本。
     *
     * @return The latest version as a String.
     *         返回最新版本字符串。
     * @throws IOException If there is an error fetching the version.
     *                    如果获取版本时发生错误。
     */
    String getLatestVersion() throws IOException;
}