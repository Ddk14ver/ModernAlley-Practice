package dev.revere.alley.visual.tablist;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * 制表符列表适配器接口，定义页眉、页脚和更新的抽象方法。
 * @author Emmy
 * @project Alley
 * @date 07/09/2024 - 15:17
 */
public interface TablistAdapter {

    List<String> getHeader(Player player);

    List<String> getFooter(Player player);

    void update(Player player);
}