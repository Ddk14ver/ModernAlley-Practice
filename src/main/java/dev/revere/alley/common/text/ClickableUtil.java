package dev.revere.alley.common.text;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Utility class for creating clickable chat components.
 * 用于创建可点击聊天组件的工具类。
 *
 * @author Emmy
 * @project Alley
 * @date 08/10/2024 - 20:16
 */
@UtilityClass
public class ClickableUtil {
    private final String EMPTY_SPACE_BETWEEN_COMPONENTS;

    static {
        EMPTY_SPACE_BETWEEN_COMPONENTS = "    ";
    }

    /**
     * Create a clickable component with a command and hover text.
     * 创建一个带有命令和悬停文本的可点击组件。
     *
     * @param message   the message to be displayed.
     *                  要显示的消息。
     * @param command   the command to be executed when clicked.
     *                  点击时要执行的命令。
     * @param hoverText the text to be displayed when hovered over.
     *                  悬停时显示的文本。
     * @return a TextComponent that is clickable and has hover text.
     *         一个可点击并带有悬停文本的TextComponent。
     */
    public @NotNull TextComponent createComponent(String message, String command, String hoverText) {
        TextComponent clickableMessage = new TextComponent(CC.translate(message));
        clickableMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));

        String hover = CC.translate(hoverText);
        BaseComponent[] hoverComponent = new ComponentBuilder(hover).create();

        clickableMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent));
        return clickableMessage;
    }

    /**
     * Create a link component with a URL and hover text.
     * 创建一个带有URL和悬停文本的链接组件。
     *
     * @param message   the message to be displayed.
     *                  要显示的消息。
     * @param url       the URL to be opened when clicked.
     *                  点击时要打开的URL。
     * @param hoverText the text to be displayed when hovered over.
     *                  悬停时显示的文本。
     * @return a TextComponent that is a link and has hover text.
     *         一个链接组件，带有悬停文本。
     */
    public @NotNull TextComponent createLinkComponent(String message, String url, String hoverText) {
        TextComponent linkMessage = new TextComponent(CC.translate(message));
        linkMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        String hover = CC.translate(hoverText);
        BaseComponent[] hoverComponent = new ComponentBuilder(hover).create();

        linkMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent));
        return linkMessage;
    }

    /**
     * Creates a chain of clickable links separated by a separator.
     * 创建由分隔符分隔的可点击链接链。
     *
     * @param components A list of individual link components.
     *                   独立的链接组件列表。
     * @param separator  The text to place between each component (e.g., " | ").
     *                   每个组件之间放置的文本（例如 " | "）。
     * @return A single TextComponent containing the chained links.
     *         包含链接链的单个TextComponent。
     */
    public TextComponent createComponentChain(List<TextComponent> components, String separator) {
        if (components == null || components.isEmpty()) {
            return new TextComponent("");
        }

        TextComponent finalComponent = components.get(0);

        for (int i = 1; i < components.size(); i++) {
            TextComponent separatorComponent = new TextComponent(CC.translate(separator));
            finalComponent.addExtra(separatorComponent);

            finalComponent.addExtra(components.get(i));
        }

        return finalComponent;
    }

    /**
     * Send clickable page navigation messages to a player.
     * 向玩家发送可点击的页面导航消息。
     * This calculates the current page, the total number of pages, and the base command for navigation.
     * 这将计算当前页面、总页数以及导航的基础命令。
     * This allows the player to navigate through pages with ease.
     * 这使玩家可以轻松地在页面之间导航。
     *
     * @param player         the player
     *                       玩家
     * @param page           the current page
     *                       当前页码
     * @param totalPages     the total number of pages
     *                       总页数
     * @param command        the command to be executed upon clicking
     *                       点击时要执行的命令
     * @param keepInPosition whether to keep the next page button in the same position in every page (displayBoth must be false)
     *                       是否在每一页中将下一页按钮保持在同一位置（displayBoth必须为false）
     * @param displayBoth    whether to always show both buttons or not (keepInPosition won't affect this, it will be ignored)
     *                       是否始终显示两个按钮（keepInPosition不会影响此设置，将被忽略）
     */
    public void sendPageNavigation(Player player, int page, int totalPages, String command, boolean keepInPosition, boolean displayBoth) {
        TextComponent nextPage = createComponent(page == totalPages ? "&a&m[Next Page]" : "&a[Next Page]", command + " " + (page + 1), page == totalPages ? "&cYou are already on the last page." : "&7Click to view page &6" + (page + 1) + "&7.");
        TextComponent previousPage = createComponent(page == 1 ? "&c&m[Previous Page]" : "&c[Previous Page]", command + " " + (page - 1), page == 1 ? "&cYou are already on the first page." : "&7Click to view page &6" + (page - 1) + "&7.");

        if (displayBoth) {
            TextComponent component = new TextComponent("");
            component.addExtra(EMPTY_SPACE_BETWEEN_COMPONENTS);
            component.addExtra(previousPage);
            component.addExtra(EMPTY_SPACE_BETWEEN_COMPONENTS);
            component.addExtra(nextPage);
            player.spigot().sendMessage(component);
        } else {
            if (page > 1 && page < totalPages) {
                TextComponent component = new TextComponent("");

                if (keepInPosition) {
                    component.addExtra(EMPTY_SPACE_BETWEEN_COMPONENTS);
                    component.addExtra(nextPage);
                    component.addExtra(EMPTY_SPACE_BETWEEN_COMPONENTS);
                    component.addExtra(previousPage);
                } else {
                    component.addExtra(EMPTY_SPACE_BETWEEN_COMPONENTS);
                    component.addExtra(previousPage);
                    component.addExtra(EMPTY_SPACE_BETWEEN_COMPONENTS);
                    component.addExtra(nextPage);
                }

                player.spigot().sendMessage(component);
            } else if (page < totalPages) {
                TextComponent component = new TextComponent("");
                component.addExtra(EMPTY_SPACE_BETWEEN_COMPONENTS);
                component.addExtra(nextPage);
                player.spigot().sendMessage(component);
            } else if (page > 1) {
                TextComponent component = new TextComponent("");
                component.addExtra(EMPTY_SPACE_BETWEEN_COMPONENTS);
                component.addExtra(previousPage);
                player.spigot().sendMessage(component);
            }
        }
    }
}