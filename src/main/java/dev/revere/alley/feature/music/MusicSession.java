package dev.revere.alley.feature.music;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

/**
 * 音乐会话类，表示一个正在播放的音乐会话。
 * @author Remi
 * @project alley-practice
 * @date 20/07/2025
 */

@Getter
@Setter
public class MusicSession {
    private final MusicDisc disc;
    private final long startTime;
    private final Location jukeboxLocation;

    private BukkitTask task;

    private int elapsedSeconds = 0;
    private boolean paused = false;

    /**
     * Constructor for the MusicSession class.
     * MusicSession 类的构造函数。
     *
     * @param disc            The music disc being played.
     *                        正在播放的音乐唱片。
     * @param jukeboxLocation The location of the jukebox.
     *                        点唱机的位置。
     */
    public MusicSession(MusicDisc disc, Location jukeboxLocation) {
        this.disc = disc;
        this.startTime = System.currentTimeMillis();
        this.jukeboxLocation = jukeboxLocation;
    }

    /**
     * Checks if the music session has finished playing the disc.
     * 检查音乐会话是否已完成播放唱片。
     *
     * @return true if the disc has finished playing, false otherwise.
     *         如果唱片已播放完毕则返回 true，否则返回 false。
     */
    public boolean isFinished() {
        return elapsedSeconds >= disc.getDuration();
    }
}