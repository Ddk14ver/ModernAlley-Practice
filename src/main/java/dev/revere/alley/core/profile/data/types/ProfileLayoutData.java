package dev.revere.alley.core.profile.data.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.layout.data.LayoutData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Emmy
 * @project Alley
 * @since 02/05/2025
 */
@Getter
@Setter
public class ProfileLayoutData {
    public static final int MAX_LAYOUTS = 4;

    private Map<String, List<LayoutData>> layouts;

    public ProfileLayoutData() {
        this.layouts = AlleyPlugin.getInstance().getService(KitService.class).getKits().stream()
                .collect(Collectors.toMap(Kit::getName, kit -> {
                    List<LayoutData> slots = new ArrayList<>(MAX_LAYOUTS);
                    for (int i = 0; i < MAX_LAYOUTS; i++) slots.add(null);
                    return slots;
                }));
    }

    /**
     * Creates or updates a layout at the given slot index (0-3).
     */
    public void setLayoutAt(String kitName, int index, LayoutData layout) {
        List<LayoutData> slots = this.layouts.computeIfAbsent(kitName, k -> {
            List<LayoutData> l = new ArrayList<>(MAX_LAYOUTS);
            for (int i = 0; i < MAX_LAYOUTS; i++) l.add(null);
            return l;
        });
        while (slots.size() <= index) slots.add(null);
        slots.set(index, layout);
    }

    /** Removes the layout at the given slot. */
    public void removeLayoutAt(String kitName, int index) {
        List<LayoutData> slots = this.layouts.get(kitName);
        if (slots != null && index < slots.size()) slots.set(index, null);
    }

    /**
     * Adds a new layout to the list (backward compat — appends to end).
     */
    public void addLayout(String kitName, String name, String displayName, ItemStack[] items) {
        LayoutData newLayout = new LayoutData(name, displayName, items);
        List<LayoutData> slots = this.layouts.computeIfAbsent(kitName, k -> new ArrayList<>());
        slots.add(newLayout);
    }

    /**
     * Accessor method to get the layout by name.
     */
    public LayoutData getLayout(String kitName, String layoutName) {
        if (this.layouts.containsKey(kitName)) {
            for (LayoutData layout : this.layouts.get(kitName)) {
                if (layout != null && layout.getName().equalsIgnoreCase(layoutName)) {
                    return layout;
                }
            }
        }
        return null;
    }

    /** Returns the list of non-null layouts for a kit. */
    public List<LayoutData> getNonNullLayouts(String kitName) {
        List<LayoutData> result = new ArrayList<>();
        List<LayoutData> slots = this.layouts.get(kitName);
        if (slots != null) {
            for (LayoutData ld : slots) {
                if (ld != null) result.add(ld);
            }
        }
        return result;
    }
}
