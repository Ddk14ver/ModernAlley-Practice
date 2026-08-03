package dev.revere.alley.library.menu.pagination;

import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import dev.revere.alley.library.menu.impl.PageGlassButton;
import dev.revere.alley.library.menu.pagination.impl.button.PageButton;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.util.*;

@Getter
public abstract class PaginatedMenu extends Menu {

    private int page = 1;

    {
        setUpdateAfterClick(false);
    }

    @Override
    public String getTitle(Player player) {
        return getPrePaginatedTitle(player);
    }

    /**
     * Changes the page number
     * 更改页码
     *
     * @param player player viewing the inventory
     *               正在查看物品栏的玩家
     * @param mod    delta to modify the page number by
     *               用于修改页码的增量
     */
    public final void modPage(Player player, int mod) {
        page += mod;
        getButtons().clear();
        openMenu(player);
    }

    /**
     * @param player player viewing the inventory
     *               正在查看物品栏的玩家
     */
    public final int getPages(Player player) {
        int buttonAmount = getAllPagesButtons(player).size();

        if (buttonAmount == 0) {
            return 1;
        }

        return (int) Math.ceil(buttonAmount / (double) getMaxItemsPerPage());
    }

    @Override
    public final Map<Integer, Button> getButtons(Player player) {
        int minIndex = (int) ((double) (page - 1) * getMaxItemsPerPage());
        int maxIndex = (int) ((double) (page) * getMaxItemsPerPage());
        int topIndex = 0;

        HashMap<Integer, Button> buttons = new HashMap<>();

        for (Map.Entry<Integer, Button> entry : getAllPagesButtons(player).entrySet()) {
            int ind = entry.getKey();

            if (ind >= minIndex && ind < maxIndex) {
                ind -= (int) ((double) (getMaxItemsPerPage()) * (page - 1)) - 9;
                buttons.put(ind, entry.getValue());

                if (ind > topIndex) {
                    topIndex = ind;
                }
            }
        }

        buttons.put(0, new PageButton(this, -1));
        buttons.put(8, new PageButton(this, 1));


        Map<Integer, Button> global = getGlobalButtons(player);

        if (global != null) {
            buttons.putAll(global);
        }

        return buttons;
    }

    /**
     * Method to get the maximum number of items that can be displayed on a single page.
     * 获取单页可显示的最大物品数量。
     *
     * @return the maximum number of items per page.
     *         每页最大物品数量。
     */
    public int getMaxItemsPerPage() {
        return 36;
    }

    /**
     * Validates the slot number to ensure it does not conflict with reserved slots.
     * 验证槽位号以确保不与保留槽位冲突。
     * Reserved slots are typically used for glass panes or other UI elements.
     * 保留槽位通常用于玻璃板或其他UI元素。
     *
     * @param slot the original slot number to validate
     *             要验证的原始槽位号
     * @return a valid slot number that does not conflict with reserved slots
     *         不与保留槽位冲突的有效槽位号
     */
    public int validateSlot(int slot) {
        int slotsPerPage = 36;

        List<Integer> baseSlotsToAvoid = Arrays.asList(0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36);

        int page = slot / slotsPerPage;

        int pageOffset = page * slotsPerPage;

        Set<Integer> slotsToAvoid = new HashSet<>();
        for (int baseSlot : baseSlotsToAvoid) {
            slotsToAvoid.add(baseSlot + pageOffset);
        }

        while (slotsToAvoid.contains(slot)) {
            slot++;
        }

        return slot;
    }

    /**
     * Adds glass panes to the inventory to avoid reserved slots.
     * 向物品栏添加玻璃板以避免使用保留槽位。
     * This method fills the reserved slots with glass buttons to ensure they are not used for other items.
     * 此方法用玻璃按钮填充保留槽位，以确保它们不被其他物品占用。
     *
     * @param buttons a map of buttons representing the inventory slots
     *                表示物品栏槽位的按钮映射
     */
    public void addGlassToAvoidedSlots(Map<Integer, Button> buttons) {
        int slotsPerPage = getMaxItemsPerPage();
        List<Integer> baseSlotsToAvoid = Arrays.asList(0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36);

        for (int page = 0; page <= (buttons.size() / slotsPerPage); page++) {
            int pageOffset = page * slotsPerPage;

            for (int baseSlot : baseSlotsToAvoid) {
                int slot = baseSlot + pageOffset;
                if (!buttons.containsKey(slot)) {
                    buttons.put(slot, new PageGlassButton(Material.BLACK_STAINED_GLASS_PANE));
                }
            }
        }
    }

    /**
     * @param player player viewing the inventory
     *               正在查看物品栏的玩家
     * @return a Map of button that returns items which will be present on all pages
     *         返回将在所有页面中显示的按钮映射
     */
    public Map<Integer, Button> getGlobalButtons(Player player) {
        return null;
    }

    /**
     * @param player player viewing the inventory
     *               正在查看物品栏的玩家
     * @return title of the inventory before the page number is added
     *         添加页码之前的物品栏标题
     */
    public abstract String getPrePaginatedTitle(Player player);

    /**
     * @param player player viewing the inventory
     *               正在查看物品栏的玩家
     * @return a map of button that will be paginated and spread across pages
     *         将被分页并散布到各页面的按钮映射
     */
    public abstract Map<Integer, Button> getAllPagesButtons(Player player);

}
