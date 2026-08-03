package dev.revere.alley.feature.title.model;

import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.division.Division;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Emmy
 * @project Alley
 * @since 21/04/2025
 */
@Getter
@Setter
public class TitleRecord {
    private final Kit kit;
    private final String customName;
    private String prefix;
    private Division requiredDivision;
    private boolean enabled;
    private int slot;
    private boolean purchasable;

    public TitleRecord(Kit kit, String prefix, Division requiredDivision, boolean enabled, int slot, boolean purchasable) {
        this.kit = kit;
        this.customName = null;
        this.prefix = prefix;
        this.requiredDivision = requiredDivision;
        this.enabled = enabled;
        this.slot = slot;
        this.purchasable = purchasable;
    }

    /** Custom title constructor (no kit). */
    public TitleRecord(String name, String prefix, Division requiredDivision, boolean enabled, int slot, boolean purchasable) {
        this.kit = null;
        this.customName = name;
        this.prefix = prefix;
        this.requiredDivision = requiredDivision;
        this.enabled = enabled;
        this.slot = slot;
        this.purchasable = purchasable;
    }

    public TitleRecord(Kit kit, String prefix, Division requiredDivision) {
        this(kit, prefix, requiredDivision, true, 0, false);
    }

    /** Returns the identifying name (kit name or custom name). */
    public String getName() {
        return customName != null ? customName : (kit != null ? kit.getName() : "Unknown");
    }
}
