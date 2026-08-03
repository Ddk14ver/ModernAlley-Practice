package dev.revere.alley.feature.layout;

import dev.revere.alley.library.menu.Menu;
import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.layout.data.LayoutData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Service interface for kit layout management.
 * 套件布局管理的服务接口。
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface LayoutService extends Service {
    /**
     * Gets the menu instance used for the kit layout editor.
     * 获取用于套件布局编辑器的菜单实例。
     *
     * @return The layout editor Menu.
     *         布局编辑器菜单。
     */
    Menu getLayoutMenu();

    /**
     * Creates the specific ItemStack (a book) that represents a single kit layout.
     * 创建表示单个套件布局的特定物品堆栈（一本书）。
     *
     * @param layout The layout data to represent.
     *               要表示的布局数据。
     * @return The ItemStack representing the layout book.
     *         表示布局书的物品堆栈。
     */
    ItemStack getLayoutBook(LayoutData layout);

    /**
     * Gives a player all the layout selection books for a specific kit.
     * 给玩家提供特定套件的所有布局选择书。
     *
     * @param player  The player to give the books to.
     *                要接收这些书的玩家。
     * @param kitName The name of the kit.
     *                套件的名称。
     */
    void giveBooks(Player player, String kitName);
}