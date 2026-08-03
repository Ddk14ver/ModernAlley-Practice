package dev.revere.alley.library.menu;

import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

/**
 * @author Emmy
 * 作者：Emmy
 * @project Alley
 * 项目：Alley
 * @since 24/01/2025
 * 自：24/01/2025
 */
@UtilityClass
public class MenuUtil {
    /**
     * Checks if the player has a next page.
     * 检查玩家是否有下一页。
     *
     * @param player the player viewing the menu
     *               正在查看菜单的玩家
     * @param offset the delta to modify the page number by
     *               用于修改页码的偏移量
     * @param menu   the menu
     *               菜单实例
     * @return true if the player has a next page
     *         如果玩家有下一页则返回true
     */
    public boolean hasNext(Player player, int offset, PaginatedMenu menu) {
        int pg = menu.getPage() + offset;
        return menu.getPages(player) >= pg;
    }

    /**
     * Checks if the player has a previous page.
     * 检查玩家是否有上一页。
     *
     * @param offset the delta to modify the page number by
     *               用于修改页码的偏移量
     * @param menu   the menu
     *               菜单实例
     * @return true if the player has a previous page
     *         如果玩家有上一页则返回true
     */
    public boolean hasPrevious(int offset, PaginatedMenu menu) {
        int pg = menu.getPage() + offset;
        return pg > 0;
    }
}
