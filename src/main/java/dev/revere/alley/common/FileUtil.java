package dev.revere.alley.common;

import dev.revere.alley.common.logger.Logger;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;

/**
 * @author Remi
 * @project alley-practice
 * @date 23/06/2025
 */
@UtilityClass
public class FileUtil {
    /**
     * Deletes the specified world folder, including all its contents. If an
     * initial deletion attempt fails, an alternative method is used to ensure
     * all files and subdirectories are removed. Logs any exceptions encountered
     * during the process.
     * 删除指定的世界文件夹及其所有内容。如果初始删除尝试失败，则使用替代方法
     * 确保所有文件和子目录被移除。记录过程中遇到的任何异常。
     *
     * @param worldFolder the folder representing the world to delete
     *                    代表要删除的世界的文件夹
     */
    public void deleteWorldFolder(File worldFolder) {
        try {
            deleteDirectory(worldFolder);
        } catch (Exception e) {
            Logger.logException("Failed to delete world folder", e);

            try {
                Files.walk(worldFolder.toPath())
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (Exception ex) {
                                Logger.logException("Failed to delete: " + path, ex);
                            }
                        });
            } catch (Exception ex) {
                Logger.logException("Alternative deletion method also failed", ex);
            }
        }
    }

    /**
     * Deletes a directory and all its contents, including subdirectories and files.
     * Logs an error message for any file or directory that fails to be deleted.
     * 删除目录及其所有内容，包括子目录和文件。
     * 对删除失败的任何文件或目录记录错误消息。
     *
     * @param directory the directory to delete
     *                  要删除的目录
     */
    public void deleteDirectory(File directory) {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        Logger.error("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }

        if (!directory.delete()) {
            Logger.error("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }

    /**
     * Converts single-quoted YAML values to double-quoted ones.
     * 将单引号YAML值转换为双引号值。
     * <p>
     * Regex pattern explanation:
     * 正则表达式模式说明：
     * <ul>
     *   <li><code>:\s+</code> – matches a colon followed by spaces (YAML key-value separator)
     *                         匹配冒号后跟空格（YAML键值分隔符）</li>
     *   <li><code>'</code> – matches opening single quote
     *                        匹配开头的单引号</li>
     *   <li><code>[^']*</code> – captures everything that's not a single quote (the content)
     *                             捕获所有非单引号的内容（即值的内容）</li>
     *   <li><code>'</code> – matches closing single quote
     *                        匹配结尾的单引号</li>
     *   <li><code>(?=\s*$)</code> – positive lookahead for end of line (with optional whitespace)
     *                                正向前瞻匹配行尾（可带空白字符）</li>
     * </ul>
     * <p>
     * This ensures that only single quotes wrapping entire values are replaced,
     * while quotes within text (e.g., <code>can't</code>) remain untouched.
     * 这确保仅替换包裹整个值的单引号，而文本内的引号（如 <code>can't</code>）保持不变。
     *
     * @param content the YAML content to process
     *                要处理的YAML内容
     * @return processed content with double quotes
     *         处理后的双引号内容
     */
    public String convertQuotesToDouble(String content) {
        return content.replaceAll("(:\\s+)'([^']*)'(?=\\s*$)", "$1\"$2\"");
    }

    /**
     * Post-processes a saved config file to convert single quotes to double quotes.
     * 对保存的配置文件进行后处理，将单引号转换为双引号。
     *
     * @param file the config file to process.
     *             要处理的配置文件
     */
    public void postProcessConfigFile(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            String processedContent = convertQuotesToDouble(content);
            Files.write(file.toPath(), processedContent.getBytes());
        } catch (IOException exception) {
            Logger.error("Failed to post-process config file: " + file.getName());
        }
    }
}