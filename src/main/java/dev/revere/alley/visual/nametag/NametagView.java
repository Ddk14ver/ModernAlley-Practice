package dev.revere.alley.visual.nametag;

import lombok.Getter;

import java.util.Objects;

/**
 * 名字标签视图值对象，包含前缀、后缀和可见性设置。
 * @author Remi
 * @project alley-practice
 * @date 27/06/2025
 */
@Getter
public final class NametagView {
    private final String prefix;
    private final String suffix;
    private final NametagVisibility visibility;

    public NametagView(String prefix, String suffix) {
        this(prefix, suffix, NametagVisibility.ALWAYS);
    }

    public NametagView(String prefix, String suffix, NametagVisibility visibility) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.visibility = visibility;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NametagView that = (NametagView) o;
        return Objects.equals(prefix, that.prefix) && Objects.equals(suffix, that.suffix) && visibility == that.visibility;
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, suffix, visibility);
    }
}