package dev.revere.alley.feature.music.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.time.TimeUtil;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.music.MusicDisc;
import dev.revere.alley.feature.music.MusicService;
import dev.revere.alley.feature.music.MusicSession;
import dev.revere.alley.feature.spawn.SpawnService;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 音乐服务实现类，提供音乐播放、停止和管理功能。
 * @author Emmy & Remi
 * @project alley-practice
 * @since 19/07/2025
 */
@Service(provides = MusicService.class, priority = 175)
public class MusicServiceImpl implements MusicService {
    private final ProfileService profileService;
    private final SpawnService spawnService;
    private final Map<UUID, MusicSession> activeSessions = new ConcurrentHashMap<>();
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    private MusicDisc[] allDiscs;

    /**
     * DI Constructor for the MusicService class.
     * MusicService 类的依赖注入构造函数。
     *
     * @param profileService The profile service to be used by this music service.
     *                       此音乐服务使用的个人资料服务。
     * @param spawnService   The spawn service to be used by this music service.
     *                       此音乐服务使用的出生点服务。
     */
    public MusicServiceImpl(ProfileService profileService, SpawnService spawnService) {
        this.profileService = profileService;
        this.spawnService = spawnService;
    }

    @Override
    public void initialize(AlleyContext context) {
        this.allDiscs = MusicDisc.values();
    }

    @Override
    public void startMusic(Player player) {
        stopMusic(player);

        Profile profile = this.profileService.getProfile(player.getUniqueId());
        if (profile == null || !profile.getProfileData().getSettingData().isLobbyMusicEnabled()) {
            return;
        }

        MusicDisc disc = getRandomSelectedMusicDisc(profile);
        Location jukeboxLocation = spawnService.getLocation();
        player.playSound(jukeboxLocation, disc.getSoundName(), SoundCategory.RECORDS, 3.0f, 1.0f);

        String formattedDuration = TimeUtil.formatTimeFromSeconds(disc.getDuration());

        List<String> message = AlleyPlugin.getInstance().getService(LocaleService.class).getStringList(GlobalMessagesLocaleImpl.MUSIC_DISC_NOW_PLAYING);
        for (String string : message) {
            string = string
                    .replace("{disc}", disc.getTitle())
                    .replace("{duration}", formattedDuration)
            ;
            player.sendMessage(CC.translate(string));
        }

        MusicSession session = new MusicSession(disc, jukeboxLocation);
        MusicTask task = new MusicTask(player, this, profileService);
        session.setTask(task.runTaskTimer(AlleyPlugin.getInstance(), 20L, 20L));

        activeSessions.put(player.getUniqueId(), session);
    }

    @Override
    public void stopMusic(Player player) {
        MusicSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            player.stopSound(session.getDisc().getSoundName(), SoundCategory.RECORDS);
            session.getTask().cancel();
        }
    }

    @Override
    public MusicDisc getRandomMusicDisc() {
        if (allDiscs == null || allDiscs.length == 0) {
            return null;
        }
        return allDiscs[random.nextInt(allDiscs.length)];
    }

    @Override
    public Set<MusicDisc> getSelectedMusicDiscs(Profile profile) {
        return profile.getProfileData().getMusicData().getSelectedDiscs().stream()
                .map(name -> {
                    try {
                        return MusicDisc.valueOf(name);
                    } catch (IllegalArgumentException e) {
                        Logger.logException("Invalid music disc: " + name + " for " + profile.getUuid(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public MusicDisc getRandomSelectedMusicDisc(Profile profile) {
        Set<MusicDisc> selectedDiscsSet = this.getSelectedMusicDiscs(profile);
        if (selectedDiscsSet.isEmpty()) {
            return this.getRandomMusicDisc();
        }

        List<MusicDisc> selectedDiscs = new ArrayList<>(selectedDiscsSet);
        return selectedDiscs.get(this.random.nextInt(selectedDiscs.size()));
    }

    @Override
    public Optional<MusicSession> getMusicState(UUID playerUuid) {
        return Optional.ofNullable(activeSessions.get(playerUuid));
    }

    @Override
    public List<MusicDisc> getMusicDiscs() {
        return Arrays.asList(this.allDiscs);
    }

    MusicSession getSession(UUID playerUuid) {
        return activeSessions.get(playerUuid);
    }

    void sendPlaySoundPacket(Player player, MusicDisc disc, Location location) {
        player.playSound(location, disc.getSoundName(), SoundCategory.RECORDS, 3.0f, 1.0f);
    }

    void sendStopSoundPacket(Player player, MusicDisc disc) {
        player.stopSound(disc.getSoundName(), SoundCategory.RECORDS);
    }
}