package dev.revere.alley.library.assemble.enums;

import lombok.Getter;

@Getter
public enum AssembleStyle {
    KOHI(true, 15),
    VIPER(true, -1),
    MODERN(false, 1),
    CUSTOM(false, 0);

    private boolean descending;
    private int startNumber;

    /**
     * Assemble Style.
     * Assemble 样式。
     *
     * @param descending  whether the positions are going down or up.
     *        位置是向下还是向上排列。
     * @param startNumber from where to loop from.
     *        循环起始编号。
     */
    AssembleStyle(boolean descending, int startNumber) {
        this.descending = descending;
        this.startNumber = startNumber;
    }

    public AssembleStyle reverse() {
        return descending(!this.descending);
    }

    public AssembleStyle descending(boolean descending) {
        this.descending = descending;
        return this;
    }

    public AssembleStyle startNumber(int startNumber) {
        this.startNumber = startNumber;
        return this;
    }
}
