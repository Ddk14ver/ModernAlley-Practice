package dev.revere.alley.feature.match.internal.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.MatchState;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

final class GomokuGame {
    static final int BOARD_SIZE = 15;
    static final int CELL_SPACING = 7;
    static final int TURN_SECONDS = 30;

    private static final int EMPTY = 0;
    private static final int BLACK = 1;
    private static final int WHITE = 2;

    private final Match match;
    private final Consumer<GameParticipant<MatchGamePlayer>> finishHandler;
    private final int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    private final List<UUID> blackOrder = new ArrayList<>();
    private final List<UUID> whiteOrder = new ArrayList<>();
    private final List<UUID> freeForAllOrder = new ArrayList<>();
    private final Map<Location, BlockData> originalBlocks = new LinkedHashMap<>();
    private final Map<UUID, Cell> previews = new HashMap<>();

    private GameParticipant<MatchGamePlayer> blackParticipant;
    private GameParticipant<MatchGamePlayer> whiteParticipant;
    private UUID currentPlayerId;
    private boolean freeForAll;
    private boolean blackTurn = true;
    private int blackCursor;
    private int whiteCursor;
    private int freeForAllCursor;
    private int remainingTurnSeconds = TURN_SECONDS;
    private int placedStones;
    private int lastX = -1;
    private int lastY = -1;
    private boolean finished;
    private BukkitTask turnTask;
    private BukkitTask previewTask;

    GomokuGame(Match match, Consumer<GameParticipant<MatchGamePlayer>> finishHandler) {
        this.match = match;
        this.finishHandler = finishHandler;
    }

    void setupPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        player.getInventory().setItem(4, createPlacementPearl());
        player.getInventory().setItem(8, GomokuItems.createSurrenderPotion());
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.teleport(player.getLocation().clone().add(0.0, 60.0, 0.0));
        player.updateInventory();
    }

    void startTwoSides(GameParticipant<MatchGamePlayer> participantA,
                       GameParticipant<MatchGamePlayer> participantB) {
        if (isStarted()) return;

        boolean participantAIsBlack = ThreadLocalRandom.current().nextBoolean();
        this.blackParticipant = participantAIsBlack ? participantA : participantB;
        this.whiteParticipant = participantAIsBlack ? participantB : participantA;
        fillOrder(this.blackParticipant, this.blackOrder);
        fillOrder(this.whiteParticipant, this.whiteOrder);
        this.currentPlayerId = nextAvailablePlayer(this.blackOrder, TurnGroup.BLACK);
        startTasksOrFinish(this.whiteParticipant);
    }

    void startFreeForAll() {
        if (isStarted()) return;

        this.freeForAll = true;
        for (GameParticipant<MatchGamePlayer> participant : this.match.getParticipants()) {
            fillOrder(participant, this.freeForAllOrder);
        }
        Collections.shuffle(this.freeForAllOrder);
        this.currentPlayerId = nextAvailablePlayer(this.freeForAllOrder, TurnGroup.FREE_FOR_ALL);
        startTasksOrFinish(null);
    }

    private boolean isStarted() {
        return this.turnTask != null || this.previewTask != null || this.finished;
    }

    private void startTasksOrFinish(GameParticipant<MatchGamePlayer> fallbackWinner) {
        if (this.currentPlayerId == null) {
            if (fallbackWinner != null) finishGame(fallbackWinner);
            return;
        }

        announceTurn();
        AlleyPlugin plugin = AlleyPlugin.getInstance();
        this.turnTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickTurn, 20L, 20L);
        this.previewTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickPreview, 1L, 3L);
    }

    boolean tryPlaceFromView(Player player) {
        if (this.finished || this.match.getState() != MatchState.RUNNING) return false;
        if (!isCurrentPlayer(player)) {
            player.sendMessage(CC.translate("&cIt is not your turn."));
            return false;
        }

        Cell cell = getTargetCell(player);
        if (cell == null) {
            player.sendMessage(CC.translate("&cAim at an empty position on the Gomoku board."));
            return false;
        }
        return placeStone(cell.x(), cell.y());
    }

    boolean isCurrentPlayer(Player player) {
        return this.currentPlayerId != null && this.currentPlayerId.equals(player.getUniqueId());
    }

    void handleUnavailablePlayer(Player player) {
        if (isCurrentPlayer(player) && !this.finished && this.match.getState() == MatchState.RUNNING) {
            advanceTurn();
        }
    }

    String getCurrentPlayerName() {
        Player current = this.currentPlayerId == null ? null : Bukkit.getPlayer(this.currentPlayerId);
        return current == null ? "None" : current.getName();
    }

    String getCurrentColorName() {
        return this.blackTurn ? "Black" : "White";
    }

    String getPlayerColorName(UUID playerId) {
        if (this.freeForAll) return "&7Alternating";
        return this.blackParticipant != null && this.blackParticipant.containsPlayer(playerId)
                ? "&8Black" : "&fWhite";
    }

    int getRemainingTurnSeconds() {
        return this.remainingTurnSeconds;
    }

    int getPlacedStones() {
        return this.placedStones;
    }

    private void fillOrder(GameParticipant<MatchGamePlayer> participant, List<UUID> output) {
        participant.getPlayers().forEach(gamePlayer -> output.add(gamePlayer.getUuid()));
        Collections.shuffle(output);
    }

    private void tickTurn() {
        if (this.finished || this.match.getState() != MatchState.RUNNING) return;
        this.remainingTurnSeconds--;
        if (this.remainingTurnSeconds <= 0) {
            this.match.sendMessage(CC.translate("&eGomoku &8| &f" + getCurrentPlayerName()
                    + " &7ran out of time. The turn was skipped."));
            advanceTurn();
            return;
        }

        if (this.remainingTurnSeconds <= 5) {
            forEachOnlinePlayer(player -> player.playSound(
                    player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.0f));
        }
    }

    private void tickPreview() {
        if (this.finished || this.match.getState() != MatchState.RUNNING || this.currentPlayerId == null) return;
        Player current = Bukkit.getPlayer(this.currentPlayerId);
        if (current == null || !current.isOnline()) return;

        Cell target = getTargetCell(current);
        Cell previous = this.previews.get(this.currentPlayerId);
        if (target == null) {
            clearPreview(current);
            return;
        }
        if (target.equals(previous)) return;

        clearPreview(current);
        Material material = this.blackTurn ? Material.NETHER_BRICKS : Material.SANDSTONE;
        for (Location location : getPieceLocations(target.x(), target.y())) {
            current.sendBlockChange(location, material.createBlockData());
        }
        this.previews.put(this.currentPlayerId, target);
    }

    private Cell getTargetCell(Player player) {
        RayTraceResult result = player.rayTraceBlocks(200.0, FluidCollisionMode.NEVER);
        if (result == null || result.getHitBlock() == null) return null;

        Location center = getBoardCenter();
        if (center == null || result.getHitBlock().getWorld() != center.getWorld()) return null;
        if (Math.abs(result.getHitBlock().getY() - getBoardY()) > 3) return null;

        Vector hit = result.getHitPosition();
        int gridX = (int) Math.round((hit.getX() - center.getBlockX()) / CELL_SPACING) + BOARD_SIZE / 2;
        int gridY = (int) Math.round((hit.getZ() - center.getBlockZ()) / CELL_SPACING) + BOARD_SIZE / 2;
        if (!isInsideBoard(gridX, gridY) || this.board[gridX][gridY] != EMPTY) return null;

        int cellX = center.getBlockX() + (gridX - BOARD_SIZE / 2) * CELL_SPACING;
        int cellZ = center.getBlockZ() + (gridY - BOARD_SIZE / 2) * CELL_SPACING;
        if (Math.abs(hit.getX() - cellX) > 3.5 || Math.abs(hit.getZ() - cellZ) > 3.5) return null;
        return new Cell(gridX, gridY);
    }

    private boolean placeStone(int x, int y) {
        if (!isInsideBoard(x, y) || this.board[x][y] != EMPTY) return false;

        Player current = Bukkit.getPlayer(this.currentPlayerId);
        if (current != null) clearPreview(current);
        finalizeLastStone();

        int color = this.blackTurn ? BLACK : WHITE;
        this.board[x][y] = color;
        this.placedStones++;
        this.lastX = x;
        this.lastY = y;
        setPieceBlocks(x, y, this.blackTurn ? Material.NETHER_BRICKS : Material.SANDSTONE);
        forEachOnlinePlayer(player -> player.playSound(
                player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f));

        if (hasFive(x, y, color) || this.placedStones == BOARD_SIZE * BOARD_SIZE) {
            finishGame(getCurrentParticipant());
        } else {
            advanceTurn();
        }
        return true;
    }

    private void advanceTurn() {
        if (this.currentPlayerId != null) {
            Player previous = Bukkit.getPlayer(this.currentPlayerId);
            if (previous != null) clearPreview(previous);
        }

        this.blackTurn = !this.blackTurn;
        if (this.freeForAll) {
            this.currentPlayerId = nextAvailablePlayer(this.freeForAllOrder, TurnGroup.FREE_FOR_ALL);
        } else if (this.blackTurn) {
            this.currentPlayerId = nextAvailablePlayer(this.blackOrder, TurnGroup.BLACK);
        } else {
            this.currentPlayerId = nextAvailablePlayer(this.whiteOrder, TurnGroup.WHITE);
        }
        this.remainingTurnSeconds = TURN_SECONDS;

        if (this.currentPlayerId == null) {
            if (!this.freeForAll) {
                finishGame(this.blackTurn ? this.whiteParticipant : this.blackParticipant);
            }
            return;
        }
        announceTurn();
    }

    private UUID nextAvailablePlayer(List<UUID> order, TurnGroup group) {
        if (order.isEmpty()) return null;
        int cursor = switch (group) {
            case BLACK -> this.blackCursor;
            case WHITE -> this.whiteCursor;
            case FREE_FOR_ALL -> this.freeForAllCursor;
        };

        for (int checked = 0; checked < order.size(); checked++) {
            UUID candidate = order.get(cursor);
            cursor = (cursor + 1) % order.size();
            Player player = Bukkit.getPlayer(candidate);
            MatchGamePlayer gamePlayer = findGamePlayer(candidate);
            if (player != null && player.isOnline() && gamePlayer != null
                    && !gamePlayer.isDead() && !gamePlayer.isDisconnected()) {
                switch (group) {
                    case BLACK -> this.blackCursor = cursor;
                    case WHITE -> this.whiteCursor = cursor;
                    case FREE_FOR_ALL -> this.freeForAllCursor = cursor;
                }
                return candidate;
            }
        }
        return null;
    }

    private MatchGamePlayer findGamePlayer(UUID uuid) {
        return this.match.getParticipants().stream()
                .flatMap(participant -> participant.getPlayers().stream())
                .filter(gamePlayer -> gamePlayer.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    private GameParticipant<MatchGamePlayer> getCurrentParticipant() {
        if (this.currentPlayerId == null) return null;
        return this.match.getParticipants().stream()
                .filter(participant -> participant.containsPlayer(this.currentPlayerId))
                .findFirst()
                .orElse(null);
    }

    private void announceTurn() {
        Player current = this.currentPlayerId == null ? null : Bukkit.getPlayer(this.currentPlayerId);
        if (current == null) return;

        String color = this.blackTurn ? "&8&lBLACK" : "&f&lWHITE";
        this.match.sendMessage(CC.translate("&eGomoku &8| " + color + " &7turn: &f" + current.getName()));
        current.sendTitle(CC.translate("&6&lYOUR TURN"),
                CC.translate(color + " &7- Right-click the pearl to place."), 5, 30, 10);
        current.playSound(current.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        ensurePlacementPearl(current);
    }

    private void finishGame(GameParticipant<MatchGamePlayer> winningParticipant) {
        if (this.finished || winningParticipant == null) return;
        this.finished = true;
        cancelTasks();
        clearAllPreviews();
        finalizeLastStone();
        this.finishHandler.accept(winningParticipant);
    }

    private boolean hasFive(int x, int y, int color) {
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (int[] direction : directions) {
            int count = 1
                    + countDirection(x, y, direction[0], direction[1], color)
                    + countDirection(x, y, -direction[0], -direction[1], color);
            if (count >= 5) return true;
        }
        return false;
    }

    private int countDirection(int x, int y, int dx, int dy, int color) {
        int count = 0;
        for (int step = 1; step < 5; step++) {
            int nextX = x + dx * step;
            int nextY = y + dy * step;
            if (!isInsideBoard(nextX, nextY) || this.board[nextX][nextY] != color) break;
            count++;
        }
        return count;
    }

    private boolean isInsideBoard(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    private void finalizeLastStone() {
        if (this.lastX < 0 || this.lastY < 0) return;
        Material material = this.board[this.lastX][this.lastY] == BLACK
                ? Material.COAL_BLOCK : Material.QUARTZ_BLOCK;
        setPieceBlocks(this.lastX, this.lastY, material);
    }

    private void setPieceBlocks(int x, int y, Material material) {
        for (Location location : getPieceLocations(x, y)) {
            Block block = location.getBlock();
            this.originalBlocks.putIfAbsent(location.clone(), block.getBlockData().clone());
            block.setType(material, false);
        }
    }

    private List<Location> getPieceLocations(int x, int y) {
        Location center = getBoardCenter();
        if (center == null) return Collections.emptyList();

        int centerX = center.getBlockX() + (x - BOARD_SIZE / 2) * CELL_SPACING;
        int centerZ = center.getBlockZ() + (y - BOARD_SIZE / 2) * CELL_SPACING;
        List<Location> locations = new ArrayList<>(21);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                locations.add(new Location(center.getWorld(), centerX + dx, getBoardY(), centerZ + dz));
            }
        }
        return locations;
    }

    private Location getBoardCenter() {
        Location center = this.match.getArena().getCenter();
        if (center != null) return center;
        Location pos1 = this.match.getArena().getPos1();
        Location pos2 = this.match.getArena().getPos2();
        if (pos1 == null || pos2 == null || pos1.getWorld() != pos2.getWorld()) return null;
        return new Location(
                pos1.getWorld(),
                (pos1.getX() + pos2.getX()) * 0.5,
                (pos1.getY() + pos2.getY()) * 0.5,
                (pos1.getZ() + pos2.getZ()) * 0.5
        );
    }

    private int getBoardY() {
        Location center = getBoardCenter();
        return center == null ? 0 : center.getBlockY() - 1;
    }

    private void clearPreview(Player player) {
        Cell previous = this.previews.remove(player.getUniqueId());
        if (previous == null) return;
        for (Location location : getPieceLocations(previous.x(), previous.y())) {
            player.sendBlockChange(location, location.getBlock().getBlockData());
        }
    }

    private void clearAllPreviews() {
        new ArrayList<>(this.previews.keySet()).forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) clearPreview(player);
            else this.previews.remove(uuid);
        });
    }

    private void ensurePlacementPearl(Player player) {
        if (!player.getInventory().contains(Material.ENDER_PEARL)) {
            player.getInventory().setItem(4, createPlacementPearl());
        }
    }

    private ItemStack createPlacementPearl() {
        return new ItemBuilder(Material.ENDER_PEARL)
                .name("&6&lPlace Stone")
                .lore("&7Right-click while aiming at", "&7an empty board position.")
                .build();
    }

    private void forEachOnlinePlayer(Consumer<Player> action) {
        this.match.getParticipants().forEach(participant -> participant.getPlayers().forEach(gamePlayer -> {
            Player player = Bukkit.getPlayer(gamePlayer.getUuid());
            if (player != null) action.accept(player);
        }));
    }

    void shutdown() {
        cancelTasks();
        clearAllPreviews();
        this.originalBlocks.forEach((location, blockData) -> {
            World world = location.getWorld();
            if (world != null) location.getBlock().setBlockData(blockData, false);
        });
        this.originalBlocks.clear();
    }

    private void cancelTasks() {
        if (this.turnTask != null) {
            this.turnTask.cancel();
            this.turnTask = null;
        }
        if (this.previewTask != null) {
            this.previewTask.cancel();
            this.previewTask = null;
        }
    }

    private enum TurnGroup {
        BLACK,
        WHITE,
        FREE_FOR_ALL
    }

    private record Cell(int x, int y) {
    }
}
