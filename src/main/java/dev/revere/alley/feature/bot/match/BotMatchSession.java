package dev.revere.alley.feature.bot.match;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.PotionUtil;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.arena.internal.types.StandAloneArena;
import dev.revere.alley.feature.bot.BotAiMode;
import dev.revere.alley.feature.bot.BotDifficultyProfile;
import dev.revere.alley.feature.bot.internal.BotServiceImpl;
import dev.revere.alley.feature.combat.CombatService;
import dev.revere.alley.feature.hotbar.HotbarService;
import dev.revere.alley.feature.hotbar.HotbarType;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingBreakArenaBlocksImpl;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingBuildImpl;
import dev.revere.alley.feature.knockback.KnockbackManager;
import dev.revere.alley.feature.knockback.listener.PotionMotionListener;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.MatchState;
import dev.revere.alley.feature.match.combat.legacy.LegacyProjectileData;
import dev.revere.alley.feature.match.internal.MatchServiceImpl;
import dev.revere.alley.feature.match.internal.types.DefaultMatch;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.match.snapshot.SnapshotService;
import dev.revere.alley.feature.queue.Queue;
import dev.revere.alley.feature.queue.QueueService;
import dev.revere.alley.feature.kit.setting.types.combat.KitSettingOldSwordBlocking;
import dev.revere.alley.feature.music.MusicService;
import dev.revere.alley.feature.party.Party;
import dev.revere.alley.feature.party.PartyService;
import dev.revere.alley.feature.spawn.SpawnService;
import dev.revere.alley.feature.visibility.VisibilityService;
import dev.revere.alley.library.assemble.AssembleService;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public final class BotMatchSession {
    public static final String BOT_ENTITY_TAG = "alley_bot_entity";

    private static final int OPENING_BUFF_DELAY_TICKS = 3;
    private static final int DOWNWARD_HEAL_WINDUP_TICKS = 2;
    private static final int TURN_HEAL_WINDUP_TICKS = 8;
    private static final int HEAL_RECOVERY_TICKS = 2;
    private static final int HEAL_ACTION_TIMEOUT_TICKS = 20;
    private static final int GOLDEN_APPLE_USE_TICKS = 32;
    private static final int BUFF_POTION_USE_TICKS = 32;
    private static final int BOW_CHARGE_TICKS = 20;
    private static final int BOW_USE_DURATION_TICKS = 72_000;
    private static final int GOLDEN_APPLE_REUSE_DELAY_TICKS = 60;
    private static final int BUFF_POTION_REFRESH_WINDOW_TICKS = 100;
    private static final int BUFF_POTION_CHECK_INTERVAL_TICKS = 20;
    private static final double EATING_SPEED_MULTIPLIER = 0.2D;
    private static final int DEATH_EFFECT_DELAY_TICKS = 2;
    private static final int DEATH_ANIMATION_TICKS = 20;
    private static final double SPRINT_SPEED_MULTIPLIER = 1.3D;
    private static final double ATTACK_INPUT_KB_MULTIPLIER = 0.6D;
    private static final int POST_HIT_INPUT_DAMPING_TICKS = 6;

    private final BotServiceImpl service;
    private final Player player;
    private final Kit kit;
    private final Arena arena;
    private final BotDifficultyProfile difficulty;
    private final BotAiMode aiMode;
    private final int countdownTicks;
    private final int timeLimitTicks;
    private final int returnDelayTicks;
    private final Map<Location, BlockState> changedBlocks = new LinkedHashMap<>();
    private final List<Projectile> spawnedProjectiles = new java.util.ArrayList<>();

    private NPC npc;
    private Player bot;
    private BukkitTask task;
    private BukkitTask returnTask;
    private BotGomokuGame gomokuGame;
    private DefaultMatch matchContext;
    private Location botSpawnLocation;
    private Projectile activeRodProjectile;
    private boolean running;
    private boolean ended;
    private boolean forcingBotDeath;
    private boolean openingPotionsConsumed;
    private int ticks;
    private int runningTicks;
    private int nextBowTick;
    private int nextRodTick;
    private int nextLavaTick;
    private int nextHealTick;
    private int nextBuffPotionCheckTick;
    private int removeRodTick;
    private int healingPotionSlot = -1;
    private int healingThrowTick;
    private int healingActionDeadlineTick;
    private int healingRecoveryTick;
    private int buffPotionSlot = -1;
    private int buffPotionFinishTick;
    private List<PotionEffect> buffPotionEffects = List.of();
    private int goldenAppleSlot = -1;
    private int goldenAppleFinishTick;
    private int nextGoldenAppleTick;
    private int bowSlot = -1;
    private int bowReleaseTick;
    private ItemStack chargingBow;
    private int activeItemUseDuration;
    private boolean turnHealing;
    private ItemStack healingPotion;
    private Vector healingEscapeDirection;
    private double attackProgress;
    private long lastCombatSwingTick = Long.MIN_VALUE;
    private long pendingMeleeKnockbackTick = Long.MIN_VALUE;
    private long lastIncomingMeleeTick = Long.MIN_VALUE;
    private float originalWalkSpeed;

    public BotMatchSession(BotServiceImpl service, Player player, Kit kit, Arena arena,
                           BotDifficultyProfile difficulty, FileConfiguration config) {
        this.service = service;
        this.player = player;
        this.kit = kit;
        this.arena = arena;
        this.difficulty = difficulty;
        this.aiMode = kit.getBotAiMode() == null ? BotAiMode.MELEE : kit.getBotAiMode();
        this.countdownTicks = Math.max(0, config.getInt("countdown-seconds", 3)) * 20;
        this.timeLimitTicks = Math.max(30, config.getInt("match-time-limit-seconds", 300)) * 20;
        this.returnDelayTicks = Math.max(0, config.getInt("return-to-lobby-delay-seconds", 3)) * 20;
    }

    public boolean start() {
        this.originalWalkSpeed = player.getWalkSpeed();
        PlayerUtil.reset(player, true, true);
        Profile humanProfile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        AlleyPlugin.getInstance().getService(MusicService.class).stopMusic(player);

        String botName = "Bot_" + difficulty.getId();
        if (botName.length() > 16) botName = botName.substring(0, 16);
        this.npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, botName);
        this.npc.data().set(NPC.Metadata.SHOULD_SAVE, false);
        this.npc.data().set(NPC.Metadata.RESPAWN_DELAY, 100);
        this.npc.data().set("alley-bot", true);
        this.npc.setProtected(true);
        if (!this.npc.spawn(arena.getPos2())) return false;
        if (!(this.npc.getEntity() instanceof Player spawnedBot)) return false;
        this.bot = spawnedBot;
        this.bot.addScoreboardTag(BOT_ENTITY_TAG);

        Profile botProfile = new Profile(bot.getUniqueId(), botName);
        botProfile.setOnline(true);
        AlleyPlugin.getInstance().getService(ProfileService.class).getProfiles().put(bot.getUniqueId(), botProfile);
        this.service.registerBot(this, bot);

        this.botSpawnLocation = arena.getPos2().clone();
        this.bot.setGravity(false);
        this.bot.teleport(this.botSpawnLocation);

        createMatchContext(humanProfile, botProfile);
        this.matchContext.startMatch();
        this.matchContext.getRunnable().setStage((this.countdownTicks / 20) + 1);
        refreshPlayerScoreboard();

        if (this.aiMode == BotAiMode.GOMOKU) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
            bot.setGameMode(GameMode.ADVENTURE);
            bot.setHealth(bot.getMaxHealth());
        } else {
            setupBot();
            applyCombatSystems();
            Bukkit.getScheduler().runTaskLater(AlleyPlugin.getInstance(), () -> {
                if (!this.ended && this.npc != null && this.npc.isSpawned()) syncBotEquipment();
            }, 2L);
        }
        this.task = Bukkit.getScheduler().runTaskTimer(AlleyPlugin.getInstance(), this::tick, 1L, 1L);
        return true;
    }

    private void createMatchContext(Profile humanProfile, Profile botProfile) {
        Queue queue = AlleyPlugin.getInstance().getService(QueueService.class).getQueues().stream()
                .filter(candidate -> candidate.getKit().equals(this.kit))
                .filter(candidate -> !candidate.isRanked() && !candidate.isDuos())
                .findFirst()
                .orElse(null);

        GameParticipant<MatchGamePlayer> human = new GameParticipant<>(
                new MatchGamePlayer(this.player.getUniqueId(), this.player.getName()));
        GameParticipant<MatchGamePlayer> computer = new GameParticipant<>(
                new MatchGamePlayer(this.bot.getUniqueId(), botProfile.getName()));

        this.matchContext = new DefaultMatch(queue, this.kit, this.arena, false, human, computer);
        this.matchContext.setAffectStatistics(false);
        this.matchContext.setTeamMatch(false);
        AlleyPlugin.getInstance().getService(MatchService.class).addMatch(this.matchContext);

        humanProfile.setState(ProfileState.PLAYING);
        humanProfile.setMatch(this.matchContext);
        botProfile.setState(ProfileState.PLAYING);
        botProfile.setMatch(this.matchContext);
    }

    private void refreshPlayerScoreboard() {
        Bukkit.getScheduler().runTask(AlleyPlugin.getInstance(), () -> {
            if (!this.player.isOnline() || this.ended) return;
            AssembleService assemble = AlleyPlugin.getInstance().getService(AssembleService.class);
            assemble.removeBoard(this.player);
            assemble.createBoard(this.player);
        });
    }

    private void setupBot() {
        bot.getInventory().clear();
        bot.getInventory().setContents(cloneItems(kit.getItems()));
        bot.getInventory().setArmorContents(cloneItems(kit.getArmor()));
        bot.setHealth(bot.getMaxHealth());
        bot.setFoodLevel(20);
        bot.setSaturation(5.0F);
        bot.setGameMode(GameMode.SURVIVAL);
        bot.setSprinting(true);
        kit.applyPotionEffects(bot);
        this.matchContext.applyColorKit(bot);
        selectCombatItem();
        syncBotEquipment();

        if (this.aiMode != BotAiMode.GOMOKU) {
            npc.getNavigator().getDefaultParameters()
                    .baseSpeed(1.0F)
                    .speedModifier((float) getCombatMovementSpeed())
                    .range(128.0F)
                    .distanceMargin(0.0D)
                    .straightLineTargetingDistance(128.0F)
                    .stuckAction(null)
                    .updatePathRate(1);
            npc.data().setPersistent("disable-default-stuck-action", true);
            npc.data().setPersistent("collidable", false);
        }
    }

    private void applyCombatSystems() {
        KnockbackManager knockbackManager = AlleyPlugin.getInstance().getService(KnockbackManager.class);
        knockbackManager.applyKnockback(player, kit);
        knockbackManager.applyKnockback(bot, kit);

        MatchService matchService = AlleyPlugin.getInstance().getService(MatchService.class);
        if (matchService instanceof MatchServiceImpl impl && impl.getLegacyCombatService() != null) {
            impl.getLegacyCombatService().applyKit(player, kit);
            impl.getLegacyCombatService().applyKit(bot, kit);
        }
    }

    private void tick() {
        if (ended || !player.isOnline() || bot == null || !npc.isSpawned()) {
            if (!ended) finish(false);
            return;
        }

        ticks++;
        if (!running) {
            tickCountdown();
            return;
        }
        runningTicks++;
        if (runningTicks >= timeLimitTicks) {
            finish(false);
            return;
        }
        if (player.isDead()) return;
        if (isBelowArena(player)) {
            finish(false);
            return;
        }
        if (isBelowArena(bot)) {
            finish(true);
            return;
        }

        if (this.aiMode == BotAiMode.GOMOKU) {
            if (this.gomokuGame != null) this.gomokuGame.tick();
            return;
        }

        if (this.buffPotionSlot >= 0) {
            tickBuffPotionUse();
            return;
        }
        if (!this.openingPotionsConsumed) {
            if (this.runningTicks < OPENING_BUFF_DELAY_TICKS) return;
            if (beginBuffPotionUse()) return;
        } else if (this.ticks >= this.nextBuffPotionCheckTick) {
            if (beginBuffPotionUse()) return;
        }
        if (this.aiMode == BotAiMode.POTPVP && tryHeal()) return;
        if (this.aiMode == BotAiMode.BUILDUHC && tryBuildUhcAction()) return;
        updateNavigation();
        tryAttack();
    }

    private void tickCountdown() {
        if (this.bot != null && this.botSpawnLocation != null) {
            this.bot.setVelocity(new Vector());
            this.bot.setFallDistance(0.0F);
            if (!this.bot.getWorld().equals(this.botSpawnLocation.getWorld())
                    || this.bot.getLocation().distanceSquared(this.botSpawnLocation) > 0.01D) {
                this.bot.teleport(this.botSpawnLocation);
            }
        }
        if (this.matchContext == null || this.matchContext.getState() != MatchState.RUNNING) return;

        running = true;
        this.runningTicks = 0;
        bot.teleport(this.botSpawnLocation);
        bot.setVelocity(new Vector());
        bot.setFallDistance(0.0F);
        bot.setGravity(true);
        npc.setProtected(false);
        npc.data().setPersistent("protected", false);
        player.setWalkSpeed(originalWalkSpeed <= 0.0F ? 0.2F : originalWalkSpeed);
        if (this.aiMode == BotAiMode.GOMOKU) {
            this.gomokuGame = new BotGomokuGame(this);
            this.gomokuGame.start();
        } else {
            updateNavigationTarget();
        }
    }

    private boolean beginBuffPotionUse() {
        if (this.aiMode == BotAiMode.GOMOKU) return false;
        this.openingPotionsConsumed = true;
        this.nextBuffPotionCheckTick = this.ticks + BUFF_POTION_CHECK_INTERVAL_TICKS;

        ItemStack[] contents = bot.getInventory().getStorageContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item == null || !isPotionItem(item.getType())) continue;

            List<PotionEffect> effects = PotionUtil.getPotionEffects(item);
            if (effects.isEmpty() || effects.stream().anyMatch(effect -> !isOpeningBuff(effect))) continue;
            if (!canRefreshPotionEffects(effects)) continue;

            this.buffPotionSlot = holdItem(slot);
            this.buffPotionFinishTick = this.ticks + BUFF_POTION_USE_TICKS;
            this.buffPotionEffects = List.copyOf(effects);
            bot.setSprinting(false);
            setNavigationSpeed(difficulty.getMovementSpeed() * EATING_SPEED_MULTIPLIER);
            startUsingHeldItem(BUFF_POTION_USE_TICKS);
            bot.updateInventory();
            return true;
        }
        return false;
    }

    private void tickBuffPotionUse() {
        ItemStack heldItem = bot.getInventory().getItem(this.buffPotionSlot);
        if (heldItem == null || !isPotionItem(heldItem.getType())) {
            finishBuffPotionUse(false);
            return;
        }

        bot.setSprinting(false);
        setNavigationSpeed(difficulty.getMovementSpeed() * EATING_SPEED_MULTIPLIER);
        if (this.ticks < this.buffPotionFinishTick) {
            keepUsingHeldItem(Math.min(BUFF_POTION_USE_TICKS,
                    this.buffPotionFinishTick - this.ticks + 1));
            return;
        }
        finishBuffPotionUse(true);
    }

    private void finishBuffPotionUse(boolean completeUse) {
        stopUsingHeldItem();
        if (completeUse) {
            this.buffPotionEffects.forEach(effect -> bot.addPotionEffect(effect, true));
            consumeOne(this.buffPotionSlot);
            bot.getWorld().playSound(bot.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0F, 1.0F);
        }
        this.buffPotionSlot = -1;
        this.buffPotionFinishTick = 0;
        this.buffPotionEffects = List.of();
        this.nextBuffPotionCheckTick = this.ticks + 1;
        syncHeldItem();
        bot.updateInventory();
        selectCombatItem();
        restoreCombatNavigation();
    }

    private boolean canRefreshPotionEffects(List<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            PotionEffect active = bot.getPotionEffect(effect.getType());
            if (active == null) continue;
            if (active.getDuration() == PotionEffect.INFINITE_DURATION
                    || active.getDuration() > BUFF_POTION_REFRESH_WINDOW_TICKS) return false;
        }
        return true;
    }

    private boolean isPotionItem(Material material) {
        return material == Material.POTION
                || material == Material.SPLASH_POTION
                || material == Material.LINGERING_POTION;
    }

    private boolean isOpeningBuff(PotionEffect effect) {
        return switch (effect.getType().getKey().getKey()) {
            case "speed", "haste", "strength", "jump_boost", "resistance", "fire_resistance",
                    "water_breathing", "invisibility", "night_vision", "health_boost", "absorption",
                    "saturation", "luck", "slow_falling", "conduit_power", "dolphins_grace",
                    "hero_of_the_village" -> true;
            default -> false;
        };
    }

    private void updateNavigation() {
        setNavigationSpeed(getCombatMovementSpeed());
        if (!npc.getNavigator().isNavigating()) {
            updateNavigationTarget();
        }
        Location target = player.getEyeLocation().clone();
        double error = difficulty.getAimError();
        target.add(random(error), random(error * 0.4), random(error));
        npc.faceLocation(target);
        bot.setSprinting(true);

        if (difficulty.isStrafe() && ticks % 12 == 0 && bot.isOnGround()
                && bot.getLocation().distanceSquared(player.getLocation()) < 25.0) {
            Vector direction = player.getLocation().toVector().subtract(bot.getLocation().toVector()).setY(0);
            if (direction.lengthSquared() < 1.0E-6) return;
            direction.normalize();
            Vector side = new Vector(-direction.getZ(), 0.0, direction.getX())
                    .multiply(ThreadLocalRandom.current().nextBoolean() ? 0.11 : -0.11);
            bot.setVelocity(bot.getVelocity().add(side));
        }
    }

    private void updateNavigationTarget() {
        npc.getNavigator().setStraightLineTarget(player, false);
    }

    private void setNavigationSpeed(double speedModifier) {
        npc.getNavigator().getDefaultParameters().baseSpeed(1.0F).speedModifier((float) speedModifier);
        if (npc.getNavigator().isNavigating()) {
            npc.getNavigator().getLocalParameters().baseSpeed(1.0F).speedModifier((float) speedModifier);
        }
    }

    private void restoreCombatNavigation() {
        if (npc == null || !npc.isSpawned()) return;
        npc.getNavigator().cancelNavigation();
        setNavigationSpeed(getCombatMovementSpeed());
        bot.setSprinting(true);
        updateNavigationTarget();
    }

    private double getCombatMovementSpeed() {
        return difficulty.getMovementSpeed() * SPRINT_SPEED_MULTIPLIER;
    }

    private void tryAttack() {
        if (runningTicks < difficulty.getReactionTicks()) return;
        if (!isWithinRange(5.0D) || !hasClearLineOfSight()) return;

        attackProgress = Math.min(1.0, attackProgress + difficulty.getCps() / 20.0);
        if (attackProgress < 1.0) return;
        attackProgress -= 1.0;

        this.lastCombatSwingTick = Bukkit.getCurrentTick();
        bot.swingMainHand();
        if (!isWithinAttackRange()) return;
        bot.attack(player);
        dampPostHitCombatVelocity();

        if (difficulty.isWTap()) {
            bot.setSprinting(false);
            Bukkit.getScheduler().runTaskLater(AlleyPlugin.getInstance(), () -> {
                if (!ended && bot != null) bot.setSprinting(true);
            }, 1L);
        }
    }

    private boolean isWithinAttackRange() {
        return isWithinRange(difficulty.getAttackRange());
    }

    public void markIncomingMeleeKnockback() {
        long currentTick = Bukkit.getCurrentTick();
        this.pendingMeleeKnockbackTick = currentTick;
        this.lastIncomingMeleeTick = currentTick;
    }

    private void dampPostHitCombatVelocity() {
        long damageAge = Bukkit.getCurrentTick() - this.lastIncomingMeleeTick;
        if (damageAge < 0L || damageAge > POST_HIT_INPUT_DAMPING_TICKS) return;

        Vector velocity = bot.getVelocity();
        velocity.setX(velocity.getX() * ATTACK_INPUT_KB_MULTIPLIER);
        velocity.setZ(velocity.getZ() * ATTACK_INPUT_KB_MULTIPLIER);
        bot.setVelocity(velocity);
    }

    public Vector applyCombatInputKnockbackReduction(Vector velocity) {
        long currentTick = Bukkit.getCurrentTick();
        long damageAge = currentTick - this.pendingMeleeKnockbackTick;
        long swingAge = currentTick - this.lastCombatSwingTick;
        this.pendingMeleeKnockbackTick = Long.MIN_VALUE;
        long clickWindowTicks = Math.max(1L,
                (long) Math.ceil(20.0D / Math.max(1.0D, this.difficulty.getCps())));
        if (damageAge < 0L || damageAge > 1L
                || swingAge < 0L || swingAge > clickWindowTicks) return velocity;

        Vector reduced = velocity.clone();
        reduced.setX(reduced.getX() * ATTACK_INPUT_KB_MULTIPLIER);
        reduced.setZ(reduced.getZ() * ATTACK_INPUT_KB_MULTIPLIER);
        return reduced;
    }

    private boolean isWithinRange(double reach) {
        BoundingBox box = player.getBoundingBox();
        Vector eye = bot.getEyeLocation().toVector();
        double x = clamp(eye.getX(), box.getMinX(), box.getMaxX());
        double y = clamp(eye.getY(), box.getMinY(), box.getMaxY());
        double z = clamp(eye.getZ(), box.getMinZ(), box.getMaxZ());
        return eye.distanceSquared(new Vector(x, y, z)) <= reach * reach;
    }

    private boolean hasClearLineOfSight() {
        if (bot.hasLineOfSight(player)) return true;

        Location start = bot.getEyeLocation();
        Vector direction = player.getEyeLocation().toVector().subtract(start.toVector());
        double distance = direction.length();
        return distance < 1.0E-6D || bot.getWorld().rayTraceBlocks(
                start, direction.normalize(), distance, FluidCollisionMode.NEVER, true) == null;
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private boolean tryHeal() {
        if (this.healingPotionSlot >= 0) {
            tickHealingAction();
            return true;
        }
        if (this.healingRecoveryTick > 0) {
            if (this.ticks < this.healingRecoveryTick) return true;
            this.healingRecoveryTick = 0;
            selectCombatItem();
        }
        if (difficulty.getHealHealth() <= 0.0 || bot.getHealth() > difficulty.getHealHealth()
                || ticks < nextHealTick) return false;
        ItemStack[] contents = bot.getInventory().getStorageContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (!PotionUtil.isSplashHealthPotion(item)) continue;

            beginHealingAction(slot);
            return true;
        }
        return false;
    }

    private void beginHealingAction(int slot) {
        this.healingPotionSlot = holdItem(slot);
        ItemStack heldPotion = bot.getInventory().getItem(this.healingPotionSlot);
        this.healingPotion = heldPotion == null ? null : heldPotion.clone();
        if (this.healingPotion != null) this.healingPotion.setAmount(1);

        this.turnHealing = ThreadLocalRandom.current().nextBoolean();
        this.healingEscapeDirection = bot.getLocation().toVector()
                .subtract(player.getLocation().toVector()).setY(0.0D);
        if (this.healingEscapeDirection.lengthSquared() < 1.0E-6D) {
            this.healingEscapeDirection = bot.getLocation().getDirection().multiply(-1.0D).setY(0.0D);
        }
        this.healingEscapeDirection.normalize();
        this.healingThrowTick = this.ticks + (this.turnHealing
                ? TURN_HEAL_WINDUP_TICKS : DOWNWARD_HEAL_WINDUP_TICKS);
        this.healingActionDeadlineTick = this.ticks + HEAL_ACTION_TIMEOUT_TICKS;
        this.npc.getNavigator().cancelNavigation();
        this.bot.updateInventory();
        updateHealingPose();
    }

    private void tickHealingAction() {
        if (this.ticks > this.healingActionDeadlineTick) {
            cancelHealingAction();
            return;
        }
        ItemStack heldItem = bot.getInventory().getItem(this.healingPotionSlot);
        if (this.healingPotion == null || !PotionUtil.isSplashHealthPotion(heldItem)) {
            cancelHealingAction();
            return;
        }

        updateHealingPose();
        if (this.ticks < this.healingThrowTick) return;

        Vector velocity;
        if (this.turnHealing) {
            velocity = this.healingEscapeDirection.clone().setY(-0.32D).normalize().multiply(0.85D);
        } else {
            Location landing = bot.getLocation().clone()
                    .add(this.healingEscapeDirection.clone().multiply(0.65D))
                    .add(0.0D, 0.05D, 0.0D);
            velocity = landing.toVector().subtract(bot.getEyeLocation().toVector())
                    .normalize().multiply(0.85D);
        }
        npc.faceLocation(bot.getEyeLocation().clone().add(velocity.clone().multiply(5.0D)));

        KnockbackManager knockbackManager = AlleyPlugin.getInstance().getService(KnockbackManager.class);
        PotionMotionListener potionMotionListener = knockbackManager.getPotionMotionListener();
        ThrownPotion potion;
        if (potionMotionListener != null) potionMotionListener.beginCustomLaunch(bot);
        try {
            potion = bot.launchProjectile(ThrownPotion.class, velocity);
        } catch (RuntimeException exception) {
            cancelHealingAction();
            return;
        } finally {
            if (potionMotionListener != null) potionMotionListener.endCustomLaunch(bot);
        }
        potion.setItem(this.healingPotion);
        if (potionMotionListener != null) potionMotionListener.applyCustomLaunch(potion, bot);
        this.spawnedProjectiles.add(potion);
        consumeOne(this.healingPotionSlot);
        syncHeldItem();
        bot.updateInventory();

        this.nextHealTick = this.ticks + 30;
        this.healingRecoveryTick = this.ticks + HEAL_RECOVERY_TICKS;
        this.healingPotionSlot = -1;
        this.healingPotion = null;
        this.healingEscapeDirection = null;
        restoreCombatNavigation();
    }

    private void updateHealingPose() {
        Location eye = bot.getEyeLocation();
        if (this.turnHealing) {
            npc.faceLocation(eye.clone().add(this.healingEscapeDirection.clone().multiply(5.0D)));
            Vector velocity = bot.getVelocity();
            velocity.setX(this.healingEscapeDirection.getX() * 0.24D);
            velocity.setZ(this.healingEscapeDirection.getZ() * 0.24D);
            bot.setSprinting(true);
            bot.setVelocity(velocity);
            return;
        }

        Location lookAt = bot.getLocation().clone()
                .add(this.healingEscapeDirection.clone().multiply(0.65D))
                .add(0.0D, 0.05D, 0.0D);
        npc.faceLocation(lookAt);
    }

    private void cancelHealingAction() {
        this.healingPotionSlot = -1;
        this.healingPotion = null;
        this.healingEscapeDirection = null;
        this.healingRecoveryTick = 0;
        selectCombatItem();
        restoreCombatNavigation();
    }

    private boolean tryBuildUhcAction() {
        if (this.goldenAppleSlot >= 0) {
            tickGoldenAppleUse();
            return true;
        }
        if (this.bowSlot >= 0) {
            tickBowCharge();
            return true;
        }
        if (this.activeRodProjectile != null && this.ticks >= this.removeRodTick) {
            if (this.activeRodProjectile.isValid()) this.activeRodProjectile.remove();
            this.activeRodProjectile = null;
            selectCombatItem();
        }
        if (this.activeRodProjectile != null) return true;

        if (tryGoldenApple()) return true;
        double distanceSquared = bot.getLocation().distanceSquared(player.getLocation());
        if (difficulty.isLava() && distanceSquared <= 9.0
                && this.ticks >= this.nextLavaTick && placeTemporaryLava()) return true;
        if (difficulty.isBow() && distanceSquared >= 49.0
                && this.ticks >= this.nextBowTick && beginBowCharge()) return true;
        return difficulty.isRod() && distanceSquared >= 12.25 && distanceSquared <= 100.0
                && this.ticks >= this.nextRodTick && castRod();
    }

    private boolean placeTemporaryLava() {
        int lavaSlot = findSlot(Material.LAVA_BUCKET);
        Block target = player.getLocation().getBlock();
        if (lavaSlot < 0 || !target.getType().isAir()
                || !target.getRelative(BlockFace.DOWN).getType().isSolid()) return false;

        BlockState original = target.getState();
        recordPlacedBlock(original);
        holdItem(lavaSlot);
        target.setType(Material.LAVA, false);
        this.nextLavaTick = this.ticks + 120;
        this.nextBowTick = this.ticks + 15;
        this.nextRodTick = this.ticks + 15;
        Bukkit.getScheduler().runTaskLater(AlleyPlugin.getInstance(), () -> {
            if (!this.ended && target.getType() == Material.LAVA) original.update(true, false);
            if (!this.ended) selectCombatItem();
        }, 12L);
        return true;
    }

    private boolean tryGoldenApple() {
        if (bot.getHealth() > difficulty.getHealHealth() || this.ticks < this.nextGoldenAppleTick) return false;
        int slot = findSlot(Material.GOLDEN_APPLE);
        if (slot < 0) return false;

        this.goldenAppleSlot = holdItem(slot);
        this.goldenAppleFinishTick = this.ticks + GOLDEN_APPLE_USE_TICKS;
        this.nextBowTick = this.ticks + 20;
        this.nextRodTick = this.ticks + 20;
        bot.setSprinting(false);
        setNavigationSpeed(difficulty.getMovementSpeed() * EATING_SPEED_MULTIPLIER);
        startUsingHeldItem(GOLDEN_APPLE_USE_TICKS);
        bot.updateInventory();
        return true;
    }

    private void tickGoldenAppleUse() {
        ItemStack heldItem = bot.getInventory().getItem(this.goldenAppleSlot);
        if (heldItem == null || heldItem.getType() != Material.GOLDEN_APPLE) {
            finishGoldenAppleUse(false);
            return;
        }

        bot.setSprinting(false);
        setNavigationSpeed(difficulty.getMovementSpeed() * EATING_SPEED_MULTIPLIER);
        if (this.ticks < this.goldenAppleFinishTick) {
            keepUsingHeldItem(Math.min(GOLDEN_APPLE_USE_TICKS,
                    this.goldenAppleFinishTick - this.ticks + 1));
            return;
        }

        finishGoldenAppleUse(true);
    }

    private void finishGoldenAppleUse(boolean completeUse) {
        if (completeUse && bot.hasActiveItem()) {
            bot.completeUsingActiveItem();
        }
        stopUsingHeldItem();
        if (completeUse) {
            this.nextGoldenAppleTick = this.ticks + GOLDEN_APPLE_REUSE_DELAY_TICKS;
        }
        this.goldenAppleSlot = -1;
        this.goldenAppleFinishTick = 0;
        syncHeldItem();
        bot.updateInventory();
        selectCombatItem();
        restoreCombatNavigation();
    }

    private void startUsingHeldItem(int remainingTicks) {
        bot.startUsingItem(EquipmentSlot.HAND);
        this.activeItemUseDuration = bot.hasActiveItem()
                ? Math.max(0, bot.getActiveItemRemainingTime()) : 0;
        keepUsingHeldItem(remainingTicks);
        if (this.npc != null) this.npc.data().set(NPC.Metadata.USING_HELD_ITEM, true);
    }

    private void keepUsingHeldItem(int remainingTicks) {
        if (!bot.hasActiveItem() || this.activeItemUseDuration <= 0) return;
        bot.setActiveItemRemainingTime(Math.min(this.activeItemUseDuration, Math.max(1, remainingTicks)));
    }

    private void stopUsingHeldItem() {
        if (bot.hasActiveItem()) bot.clearActiveItem();
        this.activeItemUseDuration = 0;
        if (this.npc != null) this.npc.data().set(NPC.Metadata.USING_HELD_ITEM, false);
    }

    private boolean beginBowCharge() {
        int bowSlot = findSlot(Material.BOW);
        if (bowSlot < 0 || findSlot(Material.ARROW) < 0) return false;

        ItemStack bow = bot.getInventory().getItem(bowSlot);
        this.bowSlot = holdItem(bowSlot);
        this.bowReleaseTick = this.ticks + BOW_CHARGE_TICKS;
        this.chargingBow = bow == null ? null : bow.clone();
        bot.setSprinting(false);
        setNavigationSpeed(difficulty.getMovementSpeed() * EATING_SPEED_MULTIPLIER);
        startUsingHeldItem(BOW_USE_DURATION_TICKS);
        bot.updateInventory();
        return true;
    }

    private void tickBowCharge() {
        ItemStack heldItem = bot.getInventory().getItem(this.bowSlot);
        if (heldItem == null || heldItem.getType() != Material.BOW || findSlot(Material.ARROW) < 0) {
            finishBowCharge(false);
            return;
        }

        bot.setSprinting(false);
        setNavigationSpeed(difficulty.getMovementSpeed() * EATING_SPEED_MULTIPLIER);
        npc.faceLocation(player.getEyeLocation());
        if (this.ticks < this.bowReleaseTick) return;
        finishBowCharge(true);
    }

    private void finishBowCharge(boolean releaseArrow) {
        stopUsingHeldItem();
        if (!releaseArrow) {
            clearBowCharge();
            return;
        }

        int arrowSlot = findSlot(Material.ARROW);
        if (arrowSlot < 0) {
            clearBowCharge();
            return;
        }
        Location origin = bot.getEyeLocation();
        Location target = player.getEyeLocation().clone();
        double distance = origin.distance(target);
        target.add(0.0, Math.min(2.0, distance * 0.035), 0.0);
        Vector velocity = target.toVector().subtract(origin.toVector()).normalize().multiply(3.0);
        Arrow arrow = bot.launchProjectile(Arrow.class, velocity);
        this.spawnedProjectiles.add(arrow);
        int punch = this.chargingBow == null ? 0 : this.chargingBow.getEnchantmentLevel(Enchantment.PUNCH);
        int power = this.chargingBow == null ? 0 : this.chargingBow.getEnchantmentLevel(Enchantment.POWER);
        arrow.setCritical(true);
        arrow.setDamage(2.0 + power * 0.5);
        if (kit.isSettingEnabled(KitSettingOldSwordBlocking.class)) {
            LegacyProjectileData.markArrow(arrow, punch, power, 1.0F);
        } else {
            LegacyProjectileData.storeBowPunch(arrow, punch);
        }
        consumeOne(arrowSlot);
        bot.getWorld().playSound(bot.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0F, 1.0F);
        this.nextBowTick = this.ticks + 35;
        this.nextRodTick = this.ticks + 15;
        clearBowCharge();
    }

    private void clearBowCharge() {
        this.bowSlot = -1;
        this.bowReleaseTick = 0;
        this.chargingBow = null;
        syncHeldItem();
        bot.updateInventory();
        selectCombatItem();
        restoreCombatNavigation();
    }

    private boolean castRod() {
        int rodSlot = findSlot(Material.FISHING_ROD);
        if (rodSlot < 0 || this.activeRodProjectile != null) return false;

        holdItem(rodSlot);
        Vector velocity = player.getEyeLocation().toVector().subtract(bot.getEyeLocation().toVector())
                .normalize().multiply(1.5);
        try {
            this.activeRodProjectile = bot.launchProjectile(FishHook.class, velocity);
        } catch (IllegalArgumentException unsupportedHook) {
            this.activeRodProjectile = bot.launchProjectile(Snowball.class, velocity);
        }
        this.spawnedProjectiles.add(this.activeRodProjectile);
        this.removeRodTick = this.ticks + 20;
        this.nextRodTick = this.ticks + 32;
        this.nextBowTick = this.ticks + 12;
        return true;
    }

    private int findSlot(Material material) {
        ItemStack[] contents = bot.getInventory().getStorageContents();
        for (int slot = 0; slot < contents.length; slot++) {
            if (contents[slot] != null && contents[slot].getType() == material) return slot;
        }
        return -1;
    }

    private void consumeOne(int slot) {
        ItemStack item = bot.getInventory().getItem(slot);
        if (item == null) return;
        if (item.getAmount() <= 1) bot.getInventory().setItem(slot, null);
        else item.setAmount(item.getAmount() - 1);
    }

    private int holdItem(int slot) {
        if (slot <= 8) {
            bot.getInventory().setHeldItemSlot(slot);
            syncHeldItem();
            return slot;
        }
        int hotbarSlot = 1;
        ItemStack hotbarItem = bot.getInventory().getItem(hotbarSlot);
        bot.getInventory().setItem(hotbarSlot, bot.getInventory().getItem(slot));
        bot.getInventory().setItem(slot, hotbarItem);
        bot.getInventory().setHeldItemSlot(hotbarSlot);
        syncHeldItem();
        return hotbarSlot;
    }

    private void syncBotEquipment() {
        EntityEquipment equipment = bot.getEquipment();
        if (equipment == null) return;

        ItemStack[] armor = bot.getInventory().getArmorContents();
        ItemStack boots = armor.length > 0 ? armor[0] : null;
        ItemStack leggings = armor.length > 1 ? armor[1] : null;
        ItemStack chestplate = armor.length > 2 ? armor[2] : null;
        ItemStack helmet = armor.length > 3 ? armor[3] : null;
        equipment.setBoots(boots);
        equipment.setLeggings(leggings);
        equipment.setChestplate(chestplate);
        equipment.setHelmet(helmet);
        equipment.setItemInOffHand(bot.getInventory().getItemInOffHand());

        Equipment trait = npc.getOrAddTrait(Equipment.class);
        trait.set(Equipment.EquipmentSlot.BOOTS, boots);
        trait.set(Equipment.EquipmentSlot.LEGGINGS, leggings);
        trait.set(Equipment.EquipmentSlot.CHESTPLATE, chestplate);
        trait.set(Equipment.EquipmentSlot.HELMET, helmet);
        trait.set(Equipment.EquipmentSlot.OFF_HAND, bot.getInventory().getItemInOffHand());
        syncHeldItem();
        bot.updateInventory();
    }

    private void syncHeldItem() {
        ItemStack heldItem = bot.getInventory().getItemInMainHand();
        EntityEquipment equipment = bot.getEquipment();
        if (equipment != null) {
            equipment.setItemInMainHand(heldItem);
        }
        if (npc != null) {
            npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, heldItem);
        }
    }

    public boolean handleGomokuPlacement(Player player) {
        return this.gomokuGame != null && this.gomokuGame.handlePlacement(player);
    }

    public void surrenderGomoku() {
        if (this.gomokuGame != null) this.gomokuGame.surrender();
    }

    public String getGomokuPlayerColorName() {
        return this.gomokuGame == null ? "&7Waiting" : this.gomokuGame.getPlayerColorName();
    }

    public String getGomokuCurrentPlayerName() {
        return this.gomokuGame == null ? "Waiting" : this.gomokuGame.getCurrentPlayerName();
    }

    public int getGomokuRemainingTurnSeconds() {
        return this.gomokuGame == null ? 30 : this.gomokuGame.getRemainingTurnSeconds();
    }

    public int getGomokuPlacedStones() {
        return this.gomokuGame == null ? 0 : this.gomokuGame.getPlacedStones();
    }

    private void selectCombatItem() {
        ItemStack[] storage = bot.getInventory().getStorageContents();
        for (int slot = 0; slot < storage.length; slot++) {
            ItemStack item = storage[slot];
            if (item == null) continue;
            String name = item.getType().name();
            if (name.endsWith("_SWORD") || name.endsWith("_AXE")) {
                holdItem(slot);
                return;
            }
        }
    }

    public void finish(boolean playerWon) {
        if (playerWon && beginNaturalBotDeath()) return;
        finish(playerWon, false);
    }

    private boolean beginNaturalBotDeath() {
        if (this.forcingBotDeath || this.bot == null || this.bot.isDead() || this.bot.getHealth() <= 0.0D) {
            return false;
        }

        this.forcingBotDeath = true;
        this.running = false;
        if (this.task != null) this.task.cancel();
        if (this.npc != null) this.npc.getNavigator().cancelNavigation();
        this.bot.playHurtAnimation(0.0F);
        this.bot.setHealth(0.0D);
        Bukkit.getScheduler().runTaskLater(AlleyPlugin.getInstance(), () -> {
            if (!this.ended) finish(true, false);
        }, 2L);
        return true;
    }

    public void handleNaturalDeath(Player victim) {
        if (victim == null) return;
        boolean playerWon = this.bot != null && victim.getUniqueId().equals(this.bot.getUniqueId());
        finish(playerWon, true);
    }

    private void finish(boolean playerWon, boolean naturalDeath) {
        if (ended) return;
        ended = true;
        running = false;
        if (task != null) task.cancel();
        if (bot != null) stopUsingHeldItem();
        if (npc != null) {
            npc.getNavigator().cancelNavigation();
            if (!naturalDeath || !playerWon) npc.setProtected(true);
        }
        if (!naturalDeath || !playerWon) freezeBotForResults();

        if (playerWon && !naturalDeath && bot != null) {
            bot.playHurtAnimation(0.0F);
        }
        concludeMatchContext(playerWon);
        if (playerWon && !naturalDeath && bot != null) {
            Bukkit.getScheduler().runTaskLater(AlleyPlugin.getInstance(), () -> {
                if (this.npc != null && this.npc.isSpawned() && this.bot != null) {
                    this.bot.playEffect(EntityEffect.DEATH);
                }
            }, DEATH_EFFECT_DELAY_TICKS);
            Bukkit.getScheduler().runTaskLater(AlleyPlugin.getInstance(), this::removeBotNpc,
                    DEATH_EFFECT_DELAY_TICKS + DEATH_ANIMATION_TICKS);
        } else if (playerWon) {
            Bukkit.getScheduler().runTaskLater(AlleyPlugin.getInstance(), this::removeBotNpc,
                    DEATH_ANIMATION_TICKS);
        }

        returnTask = Bukkit.getScheduler().runTaskLater(
                AlleyPlugin.getInstance(), this::returnToLobby,
                playerWon ? Math.max(returnDelayTicks, DEATH_EFFECT_DELAY_TICKS + DEATH_ANIMATION_TICKS)
                        : returnDelayTicks);
    }

    private void freezeBotForResults() {
        if (bot == null) return;
        if (botSpawnLocation != null && (bot.getWorld() != botSpawnLocation.getWorld() || isBelowArena(bot))) {
            bot.teleport(botSpawnLocation);
        }
        bot.setGravity(false);
        bot.setVelocity(new Vector());
        bot.setFallDistance(0.0F);
    }

    private void concludeMatchContext(boolean playerWon) {
        if (this.matchContext == null || this.bot == null) return;

        MatchGamePlayer human = this.matchContext.getGamePlayer(this.player);
        MatchGamePlayer computer = this.matchContext.getGamePlayer(this.bot);
        if (human == null || computer == null) return;

        MatchGamePlayer winner = playerWon ? human : computer;
        MatchGamePlayer loser = playerWon ? computer : human;
        loser.setDead(true);
        if (playerWon) {
            this.matchContext.announceDeath(this.bot, this.player, EntityDamageEvent.DamageCause.CUSTOM);
        } else {
            winner.getData().incrementKills();
        }

        this.matchContext.playDeathCosmetics(
                playerWon ? this.bot : this.player,
                playerWon ? this.player : this.bot);
        this.matchContext.setState(MatchState.ENDING_ROUND);
        this.matchContext.handleRoundEnd();

        int snapshotCount = this.matchContext.getSnapshots().size();
        this.matchContext.createSnapshot(this.bot);
        SnapshotService snapshots = AlleyPlugin.getInstance().getService(SnapshotService.class);
        for (int index = snapshotCount; index < this.matchContext.getSnapshots().size(); index++) {
            snapshots.addSnapshot(this.matchContext.getSnapshots().get(index));
        }
        this.matchContext.setState(MatchState.ENDING_MATCH);
        if (this.matchContext.getRunnable() != null) this.matchContext.getRunnable().cancel();
    }

    private void returnToLobby() {
        clearCombatSystems();
        restoreBlocks();
        cleanupBot();
        if (this.matchContext != null) {
            this.matchContext.endMatch();
        } else {
            restorePlayerToLobby();
            deleteTemporaryArena();
        }
        service.complete(this);
    }

    public void abortStart() {
        if (task != null) task.cancel();
        ended = true;
        discardMatchContext();
        clearCombatSystems();
        cleanupBot();
        restoreBlocks();
        restorePlayerToLobby();
        deleteTemporaryArena();
        service.complete(this);
    }

    public void shutdown() {
        if (task != null) task.cancel();
        if (returnTask != null) returnTask.cancel();
        ended = true;
        discardMatchContext();
        clearCombatSystems();
        cleanupBot();
        restoreBlocks();
        deleteTemporaryArena();
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        if (profile != null) profile.setState(ProfileState.LOBBY);
        service.complete(this);
    }

    private void discardMatchContext() {
        if (this.matchContext == null) return;
        if (this.matchContext.getRunnable() != null) this.matchContext.getRunnable().cancel();
        AlleyPlugin.getInstance().getService(MatchService.class).removeMatch(this.matchContext);
        this.matchContext = null;
    }

    private void restorePlayerToLobby() {
        if (!player.isOnline()) return;

        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        if (profile == null) return;

        profile.setState(ProfileState.LOBBY);
        profile.setMatch(null);
        PlayerUtil.reset(player, true, true);
        player.setWalkSpeed(originalWalkSpeed <= 0.0F ? 0.2F : originalWalkSpeed);
        Party party = AlleyPlugin.getInstance().getService(PartyService.class).getParty(player);
        profile.setParty(party);
        AlleyPlugin.getInstance().getService(SpawnService.class).teleportToSpawn(player);
        AlleyPlugin.getInstance().getService(HotbarService.class)
                .applyHotbarItems(player, party == null ? HotbarType.LOBBY : HotbarType.PARTY);
        AlleyPlugin.getInstance().getService(VisibilityService.class).updateVisibility(player);
        AlleyPlugin.getInstance().getService(MusicService.class).startMusic(player);
    }

    private void cleanupBot() {
        if (this.gomokuGame != null) this.gomokuGame.shutdown();
        if (this.activeRodProjectile != null && this.activeRodProjectile.isValid()) {
            this.activeRodProjectile.remove();
        }
        for (Projectile projectile : this.spawnedProjectiles) {
            if (projectile != null && projectile.isValid()) projectile.remove();
        }
        this.spawnedProjectiles.clear();
        UUID botId = bot == null ? null : bot.getUniqueId();
        if (bot != null) AlleyPlugin.getInstance().getService(CombatService.class).removeLastAttacker(bot, true);
        removeBotNpc();
        if (botId != null) {
            AlleyPlugin.getInstance().getService(ProfileService.class).getProfiles().remove(botId);
        }
    }

    private void removeBotNpc() {
        if (this.npc == null) return;
        if (this.npc.isSpawned()) this.npc.despawn();
        this.npc.destroy();
        this.npc = null;
    }

    private void clearCombatSystems() {
        KnockbackManager manager = AlleyPlugin.getInstance().getService(KnockbackManager.class);
        manager.clearKnockback(player);
        if (bot != null) manager.clearKnockback(bot);

        MatchService matchService = AlleyPlugin.getInstance().getService(MatchService.class);
        if (matchService instanceof MatchServiceImpl impl && impl.getLegacyCombatService() != null) {
            impl.getLegacyCombatService().removeAll(player);
            if (bot != null) impl.getLegacyCombatService().removeAll(bot);
        }
    }

    public void recordPlacedBlock(BlockState originalState) {
        changedBlocks.putIfAbsent(originalState.getLocation(), originalState);
    }

    public boolean canBreak(Block block) {
        Location location = block.getLocation();
        if (changedBlocks.containsKey(location)) return true;
        if (!kit.isSettingEnabled(KitSettingBreakArenaBlocksImpl.class)) return false;
        changedBlocks.put(location, block.getState());
        return true;
    }

    public boolean canBuild() {
        return kit.isSettingEnabled(KitSettingBuildImpl.class);
    }

    private void restoreBlocks() {
        changedBlocks.values().stream().toList().reversed().forEach(state -> state.update(true, false));
        changedBlocks.clear();
    }

    private void deleteTemporaryArena() {
        if (arena instanceof StandAloneArena standAloneArena) {
            AlleyPlugin.getInstance().getService(ArenaService.class).deleteTemporaryArena(standAloneArena);
        }
    }

    private boolean isBelowArena(Player target) {
        double minimumY = arena.getMinimum() == null ? arena.getPos1().getY() - 20.0 : arena.getMinimum().getY() - 10.0;
        return target.getLocation().getY() < minimumY;
    }

    private ItemStack[] cloneItems(ItemStack[] items) {
        if (items == null) return new ItemStack[0];
        ItemStack[] clone = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) clone[i] = items[i] == null ? null : items[i].clone();
        return clone;
    }

    private double random(double amount) {
        return amount <= 0.0 ? 0.0 : ThreadLocalRandom.current().nextDouble(-amount, amount);
    }

    public UUID getPlayerId() {
        return player.getUniqueId();
    }

    public UUID getBotId() {
        return bot == null ? null : bot.getUniqueId();
    }
}
