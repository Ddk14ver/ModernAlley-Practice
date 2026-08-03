package dev.revere.alley.library.command;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.library.command.annotation.CommandData;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * Command Framework - CommandArgs <br>
 * 命令框架 - 命令参数 <br>
 * This class is passed to the command methods and contains various utilities as
 * 此类被传递给命令方法，包含各种工具方法以及
 * well as the command info.
 * 命令信息。
 *
 * @author minnymin3
 */
@Getter
public class CommandArgs {
    private final CommandSender sender;
    private final Command command;
    private final String label;
    private final String[] args;

    protected CommandArgs(CommandSender sender, Command command, String label, String[] args, int subCommand) {
        String[] modArgs = new String[args.length - subCommand];
        if (args.length - subCommand >= 0) System.arraycopy(args, subCommand, modArgs, 0, args.length - subCommand);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(label);

        for (int x = 0; x < subCommand; x++) {
            stringBuilder.append(".").append(args[x]);
        }

        String cmdLabel = stringBuilder.toString();

        this.sender = sender;
        this.command = command;
        this.label = cmdLabel;
        this.args = modArgs;
    }

    public String getArgs(int index) {
        return args[index];
    }

    public int length() {
        return args.length;
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public Player getPlayer() {
        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            return null;
        }
    }

    /**
     * Formats the command's usage string with color codes.
     * 使用颜色代码格式化命令的用法字符串。
     * Formatting: <required> = AQUA, [optional] = GRAY, command/label = YELLOW, "Usage:" = GOLD
     * 格式规则：<必选> = 青色，[可选] = 灰色，命令/标签 = 黄色，"Usage:" = 金色
     *
     * @return The command's usage string with color codes.
     *         带有颜色代码的命令用法字符串。
     */
    public String getUsage() {


        CommandFramework commandFramework = AlleyPlugin.getInstance().getService(CommandFramework.class);
        Method method = commandFramework.getCommandMap().get(this.label).getKey();
        CommandData commandData = method.getAnnotation(CommandData.class);

        // access annotation data because sub commands will inherit the parent command's usage, which is usually like "help <page>", ruining the entire point of this method
        // 访问注解数据，因为子命令会继承父命令的用法（如"help <page>"），这会破坏此方法的用途
        String rawUsage = commandData.usage();

        StringBuilder formattedUsage = new StringBuilder();

        // start with "Usage: /command"
        // 以 "Usage: /command" 开头
        formattedUsage.append(ChatColor.GOLD).append("Usage: ").append(ChatColor.YELLOW).append("/").append(label);
        //if formattedUsage contains a dot, split by dot and take the first part
        // 如果 formattedUsage 包含点号，按点号分割并取第一部分
        if (label.contains(".")) {
            formattedUsage = new StringBuilder(formattedUsage.toString().split("\\.")[0]);
        }

        // then append the rest of the usage
        // 然后追加用法的其余部分
        String[] parts = rawUsage.split(" ");

        //the rest should be self-explanatory :D
        // 剩下的应该不言自明 :D
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];

            if (part.startsWith("[") && part.endsWith("]")) {
                formattedUsage.append(" ").append(ChatColor.GRAY).append(part);
            } else if (part.startsWith("<") && part.endsWith(">")) {
                formattedUsage.append(" ").append(ChatColor.AQUA).append(part);
            } else {
                formattedUsage.append(" ").append(ChatColor.YELLOW).append(part);
            }
        }

        return formattedUsage.toString();
    }

    public void sendUsage() {
        sender.sendMessage(this.getUsage());
    }
}