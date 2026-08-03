package dev.revere.alley.common.logger;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.constants.PluginConstant;
import dev.revere.alley.common.text.CC;
import lombok.experimental.UtilityClass;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Remi
 * @project Alley
 * @date 5/27/2024
 */
@UtilityClass
public class Logger {
    private final static ConsoleCommandSender consoleSender;
    private static final Map<UUID, Exception> storedExceptions;

    private static final String PHASE_HEADER_PREFIX = "&6&l--- ";
    private static final String PHASE_HEADER_SUFFIX = " ---";
    private static final String TASK_PREFIX_SUCCESS = "&a✔  &f";
    private static final String TASK_PREFIX_FAIL = "&c✖ &f";

    static {
        consoleSender = AlleyPlugin.getInstance().getServer().getConsoleSender();
        storedExceptions = new HashMap<>();
    }

    /**
     * Log a message to the console.
     * 将消息记录到控制台。
     *
     * @param message the message to log
     *                要记录的消息
     */
    public void info(String message) {
        consoleSender.sendMessage(CC.translate(CC.PREFIX + message));
    }

    /**
     * Log a message to the console without any prefix.
     * 将消息记录到控制台，不带任何前缀。
     *
     * @param message the message to log
     *                要记录的消息
     */
    public void infoNoPrefix(String message) {
        consoleSender.sendMessage(CC.translate(message));
    }

    /**
     * Log an error to the console.
     * 将错误记录到控制台。
     *
     * @param message the error message to log
     *                要记录的错误消息
     */
    public void error(String message) {
        consoleSender.sendMessage(CC.translate(CC.ERROR_PREFIX + "&c(ERROR) &8" + message));
    }

    /**
     * Log a warning to the console.
     * 将警告记录到控制台。
     *
     * @param message the warning message to log
     *                要记录的警告消息
     */
    public void warn(String message) {
        consoleSender.sendMessage(CC.translate(CC.WARNING_PREFIX + "&e(WARNING) &8" + message));
    }

    /**
     * Log an exception to the console.
     * 将异常记录到控制台。
     *
     * @param message   the info message or the class name
     *                  信息消息或类名
     * @param exception the exception to log
     *                  要记录的异常
     */
    public static void logException(String message, Exception exception) {
        UUID errorId = UUID.randomUUID();
        storedExceptions.put(errorId, exception);

        Arrays.asList(
                "",
                CC.ERROR_PREFIX + "&c&lEXCEPTION",
                " &f" + message + ": &r" + exception.getMessage(),
                "",
                " &c(Type &4viewerror " + errorId + " &cin console to see details)",
                ""
        ).forEach(line -> consoleSender.sendMessage(CC.translate(line)));
    }

    /**
     * Retrieve and print the full stack trace of a stored exception.
     * 检索并打印已存储异常的完整堆栈跟踪。
     *
     * @param errorId The UUID of the error.
     *                错误的 UUID。
     */
    @SuppressWarnings("all")
    public static void viewException(UUID errorId) {
        Exception exception = storedExceptions.get(errorId);
        if (exception == null) {
            consoleSender.sendMessage(CC.translate(CC.ERROR_PREFIX + "&cNo exception found with ID: " + errorId));
            return;
        }

        Arrays.asList(
                "",
                CC.MENU_BAR + CC.MENU_BAR + CC.MENU_BAR + CC.MENU_BAR,
                "",
                "&c&lVIEWING ERROR: " + errorId,
                ""
        ).forEach(line -> consoleSender.sendMessage(CC.translate(line)));

        exception.printStackTrace();

        StackTraceElement[] stackTrace = exception.getStackTrace();
        String locationMessage = "&cError occurred at: Unknown location";

        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().startsWith(AlleyPlugin.getInstance().getService(PluginConstant.class).getPackageDirectory())) {
                locationMessage = "&cError occurred at: " + element.getClassName() + " (Line " + element.getLineNumber() + ")";
                break;
            }
        }

        consoleSender.sendMessage("");
        consoleSender.sendMessage(CC.translate(locationMessage));
        consoleSender.sendMessage("");
        consoleSender.sendMessage(CC.MENU_BAR + CC.MENU_BAR + CC.MENU_BAR + CC.MENU_BAR);
        consoleSender.sendMessage("");

        storedExceptions.remove(errorId);
    }

    /**
     * Log the time it takes to run a task with clear success/failure indication.
     * 记录运行任务所需的时间，并带有明确的成功/失败指示。
     *
     * @param taskName the name of the task to run
     *                 要运行的任务名称
     * @param runnable the task to run
     *                 要运行的任务
     */
    public void logTime(String taskName, Runnable runnable) {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            runnable.run();
            success = true;
        } catch (Exception exception) {
            logException("Failed to run the " + taskName + " task", exception);
        } finally {
            long end = System.currentTimeMillis();
            String prefix = success ? TASK_PREFIX_SUCCESS : TASK_PREFIX_FAIL;
            String message = success ? "&fSuccessfully initialized &6" : "&cFailed to initialize &6";
            consoleSender.sendMessage(CC.translate(prefix + message + taskName + " &fin &6" + (end - start) + "ms&f."));
        }
    }
    /**
     * Log the time it takes to run a task.
     * 记录运行任务所需的时间。
     *
     * @param runnableTaskName the name of the task to run
     *                         要运行的任务名称
     * @param runnable         the task to run
     *                         要运行的任务
     */
    public void logTimeTask(String runnableTaskName, Runnable runnable) {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            runnable.run();
            success = true;
        } catch (Exception exception) {
            logException("Failed to run the " + runnableTaskName + " task", exception);
        } finally {
            long end = System.currentTimeMillis();
            String prefix = success ? TASK_PREFIX_SUCCESS : TASK_PREFIX_FAIL;
            String message = success ? "&fSuccessfully ran &6" : "&cFailed to run &6";
            consoleSender.sendMessage(CC.translate( prefix + message + runnableTaskName + " &fin &6" + (end - start) + "ms&f."));
        }
    }

    /**
     * Measure the runtime of a task and log it to the console with the provided action in its parameter.
     * 测量任务的运行时间，并使用参数中提供的操作将其记录到控制台。
     *
     * @param action   the action
     *                 操作
     * @param task     the task to measure
     *                 要测量的任务
     * @param runnable the task to run
     *                 要运行的任务
     */
    public void logTimeWithAction(String action, String task, Runnable runnable) {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            runnable.run();
            success = true;
        } catch (Exception exception) {
            logException("Failed to " + action + " the " + task + " task", exception);
        } finally {
            long runtime = System.currentTimeMillis() - start;
            String prefix = success ? TASK_PREFIX_SUCCESS : TASK_PREFIX_FAIL;
            String message = success ? "&fSuccessfully " + action + "&f the &6" : "&cFailed to " + action + "&f the &6";
            consoleSender.sendMessage(CC.translate(prefix + message + task + " &fin &6" + runtime + "ms&f."));
        }
    }

    /**
     * Logs the start of a major initialization phase.
     * 记录主要初始化阶段的开始。
     * @param phaseName The name of the phase (e.g., "Service Setup Phase").
     *                  阶段名称（例如"Service Setup Phase"）。
     */
    public void logPhaseStart(String phaseName) {
        consoleSender.sendMessage(CC.translate(""));
        consoleSender.sendMessage(CC.translate(PHASE_HEADER_PREFIX + phaseName.toUpperCase() + PHASE_HEADER_SUFFIX));
    }

    /**
     * Logs the completion of a major initialization phase.
     * 记录主要初始化阶段的完成。
     * @param phaseName The name of the phase (e.g., "Service Initialization").
     *                  阶段名称（例如"Service Initialization"）。
     */
    public void logPhaseComplete(String phaseName) {
        consoleSender.sendMessage(CC.translate(PHASE_HEADER_PREFIX + phaseName.toUpperCase() + " COMPLETE" + PHASE_HEADER_SUFFIX));
        consoleSender.sendMessage(CC.translate(""));
    }

    /**
     * Sends a message to both the command sender and the console log with a standard prefix.
     * 使用标准前缀将消息同时发送给命令发送者和控制台日志。
     *
     * @param sender  The command sender to send the message to.
     *                要发送消息的命令发送者。
     * @param message The message to send.
     *                要发送的消息。
     */
    public void sendMessageAndLog(CommandSender sender, String message) {
        sender.sendMessage(CC.translate(CC.PREFIX + message));
        Logger.info(message);
    }
}