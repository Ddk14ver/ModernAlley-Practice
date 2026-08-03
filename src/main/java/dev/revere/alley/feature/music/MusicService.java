package dev.revere.alley.feature.music;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.core.profile.Profile;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 音乐服务接口，定义音乐播放相关的操作。
 * @author Emmy
 * @project alley-practice
 * @since 19/07/2025
 */
public interface MusicService extends Service {
    /**
     * Starts a music session for a player.
     * 为玩家启动音乐会话。
     * This will first stop any currently playing music for the player. A random song
     * 这将首先停止玩家当前正在播放的音乐。
     * from the player's selected preferences will then be played if their lobby music setting is enabled.
     * 如果玩家的大厅音乐设置已启用，则会从其选定的偏好中随机播放一首歌曲。
     * The audio is played client-side and appears to emanate from the spawn location.
     * 音频在客户端播放，并显示为从出生点位置发出。
     *
     * @param player The player to start the music for.
     *               要为其启动音乐的玩家。
     */
    void startMusic(Player player);

    /**
     * Fully stops a player's music session.
     * 完全停止玩家的音乐会话。
     * This is a "hard stop" that halts the audio, cancels any associated tracking tasks,
     * 这是一个"硬停止"，会停止音频、取消所有关联的跟踪任务，
     * and completely removes the player's session from memory.
     * 并从内存中完全移除玩家的会话。
     *
     * @param player The player whose music session should be stopped.
     *               要停止音乐会话的玩家。
     */
    void stopMusic(Player player);

    /**
     * Retrieves a list of all available music discs defined in the system.
     * 获取系统中定义的所有可用音乐唱片列表。
     *
     * @return An immutable list of {@link MusicDisc} representing all available music discs.
     *         表示所有可用音乐唱片的不可变列表 {@link MusicDisc}。
     */
    List<MusicDisc> getMusicDiscs();

    /**
     * Selects a random music disc from the entire pool of available discs.
     * 从整个可用唱片池中随机选择一个音乐唱片。
     *
     * @return A random {@link MusicDisc} value.
     *         随机的 {@link MusicDisc} 值。
     */
    MusicDisc getRandomMusicDisc();

    /**
     * Retrieves the set of music discs a player has selected in their profile.
     * 获取玩家在其个人资料中选择的音乐唱片集合。
     * This method safely converts disc names stored in the profile to {@link MusicDisc} objects.
     * 此方法将个人资料中存储的唱片名称安全地转换为 {@link MusicDisc} 对象。
     *
     * @param profile The player's profile containing their music preferences.
     *                包含玩家音乐偏好的个人资料。
     * @return A non-null set of {@link MusicDisc} values representing the selected discs.
     *         表示所选唱片的非空 {@link MusicDisc} 值集合。
     */
    Set<MusicDisc> getSelectedMusicDiscs(Profile profile);

    /**
     * Selects a random music disc from a player's personal list of selected discs.
     * 从玩家的个人选定唱片列表中随机选择一个音乐唱片。
     * If the player has not selected any discs, this will fall back to selecting a
     * 如果玩家没有选择任何唱片，将回退到从全局池中
     * random disc from the global pool.
     * 随机选择一个唱片。
     *
     * @param profile The player's profile.
     *                玩家的个人资料。
     * @return A random {@link MusicDisc} from the player's selection.
     *         从玩家选择中随机返回的 {@link MusicDisc}。
     */
    MusicDisc getRandomSelectedMusicDisc(Profile profile);

    /**
     * Retrieves the current music session state for a specific player.
     * 获取特定玩家当前的音乐会话状态。
     *
     * @param playerUuid The UUID of the player.
     *                   玩家的 UUID。
     * @return An {@link Optional} containing the {@link MusicSession} if one is active for the player, otherwise empty.
     *         如果玩家有活跃的会话，则包含 {@link MusicSession} 的 {@link Optional}，否则为空。
     */
    Optional<MusicSession> getMusicState(UUID playerUuid);
}