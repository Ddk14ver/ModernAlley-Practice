package dev.revere.alley.common;

import dev.revere.alley.feature.match.Match;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * @author Emmy
 * @project Alley
 * @date 25/05/2024 - 17:11
 */
@UtilityClass
public class SoundUtil {
    /**
     * Play a custom sound to the player
     * 向玩家播放自定义声音
     *
     * @param player the player to play the sound to
     *               要播放声音的玩家
     * @param sound  the sound to play
     *               要播放的声音
     * @param volume the volume of the sound
     *               声音的音量
     * @param pitch  the pitch of the sound
     *               声音的音调
     */
    public void playCustomSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Play a ban hammer sound to the player
     * 向玩家播放封禁锤音效
     *
     * @param player the player to play the sound to
     *               要播放声音的玩家
     */
    public void playBanHammer(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 2.0F, 1.5F);
    }

    /**
     * Play a sound to the player based on the success boolean
     * 根据成功布尔值向玩家播放声音
     *
     * @param player  the player to play the sound to
     *                要播放声音的玩家
     * @param success the boolean to determine the sound
     *                决定播放何种声音的布尔值
     */
    public void playSound(Player player, boolean success) {
        if (success) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2F, 2F);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 2F, 2F);
        }
    }

    /**
     * Play a fail sound to the player (Note Bass)
     * 向玩家播放失败音效（音符贝斯）
     *
     * @param player the player to play the sound to
     *               要播放声音的玩家
     */
    public void playFail(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 20F, 0.1F);

    }

    /**
     * Play a success sound to the player (Note Pling)
     * 向玩家播放成功音效（音符叮咚）
     *
     * @param player the player to play the sound to
     *               要播放声音的玩家
     */
    public void playSuccess(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 20F, 15F);
    }

    /**
     * Play a click sound to the player (Click)
     * 向玩家播放点击音效
     *
     * @param player the player to play the sound to
     *               要播放声音的玩家
     */
    public void playClick(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 20F, 15F);
    }

    /**
     * Play a neutral sound to the player (Note Sticks)
     * 向玩家播放中性音效（音符击打）
     *
     * @param player the player to play the sound to
     *               要播放声音的玩家
     */
    public void playNeutral(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 20F, 15F);
    }

    /**
     * Play an Explode sound to the player (Explosion)
     * 向玩家播放爆炸音效
     *
     * @param player the player to play the sound to
     *               要播放声音的玩家
     */
    public void playExplode(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0F, 1.5F);
    }

    /**
     * Play a blast sound to the player (Firework Blast)
     * 向玩家播放烟花爆炸音效
     *
     * @param player the player to play the sound to
     *               要播放声音的玩家
     */
    public void playBlast(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 20F, 15F);
    }

    /**
     * Play a sound to all players in the match if enabled
     * 如果启用，向比赛中的所有玩家播放声音
     *
     * @param match   the match to play the sound in
     *                要在其中播放声音的比赛
     * @param sound   the sound to play
     *                要播放的声音
     * @param enabled whether the sound is enabled or not
     *                声音是否启用
     */
    private void playSoundIfEnabled(Match match, Sound sound, boolean enabled) {
        if (match == null) {
            throw new IllegalArgumentException("Match cannot be null");
        }

        if (enabled) {
            match.playSound(sound);
        }
    }
}