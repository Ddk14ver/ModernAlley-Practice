package dev.revere.alley.feature.music;

import lombok.Getter;
import org.bukkit.Material;

/**
 * Represents a music disc in the game.
 * 表示游戏中的音乐唱片。
 * Each music disc has a material type, a title, and a unique ID.
 * 每个音乐唱片都有一个材质类型、标题和唯一ID。
 * This enum is used to manage and identify different music discs.
 * 此枚举用于管理和识别不同的音乐唱片。
 *
 * @author Emmy
 * @project Alley
 * @date 27/10/2024 - 08:43
 */
@Getter
public enum MusicDisc {
    GOLD_RECORD(Material.MUSIC_DISC_13, "13", "music_disc.13", 178),
    GREEN_RECORD(Material.MUSIC_DISC_CAT, "Cat", "music_disc.cat", 185),
    RECORD_3(Material.MUSIC_DISC_BLOCKS, "Blocks", "music_disc.blocks", 345),
    RECORD_4(Material.MUSIC_DISC_CHIRP, "Chirp", "music_disc.chirp", 185),
    RECORD_5(Material.MUSIC_DISC_FAR, "Far", "music_disc.far", 174),
    RECORD_6(Material.MUSIC_DISC_MALL, "Mall", "music_disc.mall", 197),
    RECORD_7(Material.MUSIC_DISC_MELLOHI, "Mellohi", "music_disc.mellohi", 96),
    RECORD_8(Material.MUSIC_DISC_STAL, "Stal", "music_disc.stal", 150),
    RECORD_9(Material.MUSIC_DISC_STRAD, "Strad", "music_disc.strad", 188),
    RECORD_10(Material.MUSIC_DISC_WARD, "Ward", "music_disc.ward", 251),
    RECORD_11(Material.MUSIC_DISC_11, "11", "music_disc.11", 71),
    RECORD_12(Material.MUSIC_DISC_WAIT, "Wait", "music_disc.wait", 238),

    // 1.16+ discs
    // 1.16+ 唱片
    PIGSTEP(Material.MUSIC_DISC_PIGSTEP, "Pigstep", "music_disc.pigstep", 148),

    // 1.18+ discs
    // 1.18+ 唱片
    OTHERSIDE(Material.MUSIC_DISC_OTHERSIDE, "Otherside", "music_disc.otherside", 195),

    // 1.19+ discs
    // 1.19+ 唱片
    FIVE(Material.MUSIC_DISC_5, "5", "music_disc.5", 178),

    // 1.20+ discs
    // 1.20+ 唱片
    RELIC(Material.MUSIC_DISC_RELIC, "Relic", "music_disc.relic", 218),

    // 1.21+ discs
    // 1.21+ 唱片
    CREATOR(Material.MUSIC_DISC_CREATOR, "Creator", "music_disc.creator", 176),
    CREATOR_MUSIC_BOX(Material.MUSIC_DISC_CREATOR_MUSIC_BOX, "Creator (Box)", "music_disc.creator_music_box", 73),
    PRECIPICE(Material.MUSIC_DISC_PRECIPICE, "Precipice", "music_disc.precipice", 299);

    private final Material material;
    private final String title;
    private final String soundName;
    private final int duration;

    /**
     * Constructor for the MusicDisc enum.
     * MusicDisc 枚举的构造函数。
     *
     * @param material  The material type of the music disc.
     *                  音乐唱片的材质类型。
     * @param title     The title of the music disc.
     *                  音乐唱片的标题。
     * @param soundName The unique sound identifier for the music disc.
     *                  音乐唱片的唯一声音标识符。
     * @param duration  The duration of the music disc in seconds.
     *                  音乐唱片的时长（秒）。
     */
    MusicDisc(Material material, String title, String soundName, int duration) {
        this.material = material;
        this.title = title;
        this.soundName = soundName;
        this.duration = duration;
    }
}