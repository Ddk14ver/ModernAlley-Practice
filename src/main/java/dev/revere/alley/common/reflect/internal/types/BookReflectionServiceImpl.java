package dev.revere.alley.common.reflect.internal.types;

import dev.revere.alley.common.reflect.Reflection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;

/**
 * @author Emmy
 * @project Alley
 * @since 03/04/2025
 */
public class BookReflectionServiceImpl implements Reflection {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    /**
     * Opens a book for a player using modern Bukkit API.
     * 使用现代 Bukkit API 为玩家打开一本书。
     *
     * @param player the player
     *               玩家
     * @param book   the book
     *               书
     */
    public void openBook(Player player, ItemStack book) {
        if (book.getType() != Material.WRITTEN_BOOK) {
            throw new IllegalArgumentException("ItemStack must be a written book");
        }

        int slot = player.getInventory().getHeldItemSlot();
        ItemStack oldItem = player.getInventory().getItemInMainHand();
        player.getInventory().setItem(slot, book);

        player.openBook(book);

        player.getInventory().setItem(slot, oldItem);
    }

    /**
     * Creates a book ItemStack with the given title, author, and pages.
     * 使用给定的标题、作者和页面创建一本书的 ItemStack。
     *
     * @param title  the title of the book
     *               书的标题
     * @param author the author of the book
     *               书的作者
     * @param pages  the pages of the book
     *               书的页面
     * @return the book ItemStack
     *         书的 ItemStack
     */
    public ItemStack createBook(String title, String author, String[] pages) {
        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta meta = (BookMeta) bookItem.getItemMeta();

        if (meta != null) {
            meta.setTitle(title);
            meta.setAuthor(author);
            meta.pages(Arrays.stream(pages)
                .map(page -> SERIALIZER.deserialize(page))
                .toArray(Component[]::new));
            bookItem.setItemMeta(meta);
        }

        return bookItem;
    }
}
