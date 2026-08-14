package dev.revere.alley.feature.challenge.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.data.types.ProfileChallengeProgress;
import dev.revere.alley.feature.challenge.ChallengeDefinition;
import dev.revere.alley.feature.challenge.ChallengePeriod;
import dev.revere.alley.feature.challenge.ChallengeService;
import dev.revere.alley.feature.challenge.ChallengeType;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ChallengeMenu extends Menu {
    private static final int[] DAILY_SLOTS = {11, 13, 15};
    private static final int[] WEEKLY_SLOTS = {29, 31, 33};

    private final Profile profile;

    @Override
    public String getTitle(Player player) {
        return getConfig().getString("menu.title", "&6&lChallenges");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        ChallengeService challengeService = AlleyPlugin.getInstance().getService(ChallengeService.class);
        challengeService.synchronizePeriod(this.profile);

        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new PeriodHeaderButton(ChallengePeriod.DAILY));
        buttons.put(22, new PeriodHeaderButton(ChallengePeriod.WEEKLY));

        ChallengeType[] types = ChallengeType.values();
        for (int index = 0; index < types.length; index++) {
            buttons.put(DAILY_SLOTS[index], new ChallengeTaskButton(this.profile, ChallengePeriod.DAILY, types[index]));
            buttons.put(WEEKLY_SLOTS[index], new ChallengeTaskButton(this.profile, ChallengePeriod.WEEKLY, types[index]));
        }

        addGlass(buttons, Material.GRAY_STAINED_GLASS_PANE);
        return buttons;
    }

    @Override
    public int getSize() {
        return 45;
    }

    private FileConfiguration getConfig() {
        return AlleyPlugin.getInstance().getService(ConfigService.class).getChallengesConfig();
    }

    private static String formatReset(long totalSeconds) {
        long days = totalSeconds / 86_400L;
        long hours = (totalSeconds % 86_400L) / 3_600L;
        long minutes = (totalSeconds % 3_600L) / 60L;
        if (days > 0L) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        return hours + "h " + minutes + "m";
    }

    private static class PeriodHeaderButton extends Button {
        private final ChallengePeriod period;

        private PeriodHeaderButton(ChallengePeriod period) {
            this.period = period;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            ChallengeService service = AlleyPlugin.getInstance().getService(ChallengeService.class);
            FileConfiguration config = AlleyPlugin.getInstance().getService(ConfigService.class).getChallengesConfig();
            String path = this.period == ChallengePeriod.DAILY ? "menu.daily-header" : "menu.weekly-header";
            String fallback = this.period == ChallengePeriod.DAILY ? "&e&lDaily Challenges" : "&6&lWeekly Challenges";
            Material material = this.period == ChallengePeriod.DAILY ? Material.CLOCK : Material.NETHER_STAR;

            return new ItemBuilder(material)
                    .name(config.getString(path, fallback))
                    .lore(
                            CC.MENU_BAR,
                            "&fAvailable tasks: &63",
                            "&fResets in: &6" + formatReset(service.getSecondsUntilReset(this.period)),
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }
    }

    private static class ChallengeTaskButton extends Button {
        private final Profile profile;
        private final ChallengePeriod period;
        private final ChallengeType type;

        private ChallengeTaskButton(Profile profile, ChallengePeriod period, ChallengeType type) {
            this.profile = profile;
            this.period = period;
            this.type = type;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            ChallengeService service = AlleyPlugin.getInstance().getService(ChallengeService.class);
            FileConfiguration config = AlleyPlugin.getInstance().getService(ConfigService.class).getChallengesConfig();
            ChallengeDefinition definition = service.getDefinition(this.period, this.type);
            ProfileChallengeProgress progress = service.getProgress(this.profile, this.period, this.type);

            String status;
            if (progress.isCompleted()) {
                status = config.getString("menu.status.completed", "&aChallenge completed and rewarded.");
            } else if (progress.isAccepted()) {
                status = config.getString("menu.status.active", "&eChallenge in progress.");
            } else {
                status = config.getString("menu.status.available", "&aLeft-click to accept this challenge.");
            }

            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add("&fPeriod: &6" + definition.getPeriod().getDisplayName());
            lore.add("&fRequirement: &6" + definition.getRequirement() + " " + definition.getType().getUnitName());
            lore.add("&fProgress: &a" + progress.getProgress() + "&7/&6" + definition.getRequirement());
            lore.add("&fReward: &6" + definition.getRewardCoins() + " coins");
            lore.add("&fResets in: &6" + formatReset(service.getSecondsUntilReset(this.period)));
            lore.add("");
            lore.add(status);
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(Material.PAPER)
                    .name(definition.getDisplayName())
                    .lore(lore)
                    .glow(progress.isCompleted())
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;

            ChallengeService service = AlleyPlugin.getInstance().getService(ChallengeService.class);
            ChallengeDefinition definition = service.getDefinition(this.period, this.type);
            FileConfiguration config = AlleyPlugin.getInstance().getService(ConfigService.class).getChallengesConfig();
            boolean accepted = service.accept(this.profile, this.period, this.type);
            String path = accepted ? "menu.messages.accepted" : "menu.messages.already-accepted";
            String fallback = accepted ? "&aAccepted {task}&a!" : "&cYou have already accepted this challenge.";
            String message = config.getString(path, fallback);
            player.sendMessage(CC.translate(message.replace("{task}", definition.getDisplayName())));

            if (accepted) {
                playSuccess(player);
            } else {
                playFail(player);
            }
        }
    }
}
