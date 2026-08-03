package dev.revere.alley.core.profile.data.types;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Emmy
 * @project alley-practice
 * @since 19/07/2025
 */
@Getter
@Setter
public class ProfileMusicData {
    private Set<String> selectedDiscs;

    public ProfileMusicData() {
        this.selectedDiscs = new HashSet<>();
    }

    /**
     * Adds a music disc to the selected discs set.
     * 向已选择的唱片集合中添加一张音乐唱片。
     *
     * @param disc The name of the music disc to add.
     *             要添加的音乐唱片的名称。
     */
    public void addDisc(String disc) {
        this.selectedDiscs.add(disc);
    }

    /**
     * Removes a music disc from the selected discs set.
     * 从已选择的唱片集合中移除一张音乐唱片。
     *
     * @param disc The name of the music disc to remove.
     *             要移除的音乐唱片的名称。
     */
    public void removeDisc(String disc) {
        this.selectedDiscs.remove(disc);
    }

    /**
     * Checks if a music disc is selected.
     * 检查某张音乐唱片是否已被选择。
     *
     * @param disc The name of the music disc to check.
     *             要检查的音乐唱片的名称。
     * @return True if the disc is selected, false otherwise.
     *         如果该唱片已被选择则返回True，否则返回false。
     */
    public boolean isDiscSelected(String disc) {
        return this.selectedDiscs.contains(disc);
    }
}
