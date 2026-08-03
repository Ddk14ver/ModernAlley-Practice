package dev.revere.alley.feature.bot.match;

import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.match.internal.types.GomokuItems;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class BotGomokuGame {
    private static final int SIZE = 15;
    private static final int SPACING = 7;
    private static final int EMPTY = 0;
    private static final int BLACK = 1;
    private static final int WHITE = 2;

    private final BotMatchSession session;
    private final int[][] board = new int[SIZE][SIZE];
    private final boolean humanBlack = ThreadLocalRandom.current().nextBoolean();
    private boolean blackTurn = true;
    private boolean humanTurn;
    private boolean finished;
    private int ticks;
    private int turnTicks = 30 * 20;
    private int placed;
    private int lastX = -1;
    private int lastZ = -1;
    private Cell preview;

    BotGomokuGame(BotMatchSession session) {
        this.session = session;
    }

    void start() {
        this.humanTurn = this.humanBlack;
        setupHuman();
        setupBot();
        announceTurn();
    }

    void tick() {
        if (this.finished) return;
        this.ticks++;
        this.turnTicks--;

        if (this.humanTurn && this.ticks % 3 == 0) updatePreview();
        if (this.turnTicks <= 0) {
            switchTurn();
            return;
        }
        if (!this.humanTurn && this.ticks >= aiMoveTick()) {
            Cell move = chooseMove();
            if (move == null) {
                this.session.finish(false);
                return;
            }
            place(move.x(), move.z(), false);
        }
    }

    boolean handlePlacement(Player player) {
        if (this.finished || !this.humanTurn || !player.equals(this.session.getPlayer())) {
            player.sendMessage(CC.translate("&cIt is not your turn."));
            return false;
        }
        Cell target = getTargetCell(player);
        if (target == null) {
            player.sendMessage(CC.translate("&cAim at an empty position on the Gomoku board."));
            return false;
        }
        return place(target.x(), target.z(), true);
    }

    void surrender() {
        if (!this.finished) {
            this.finished = true;
            clearPreview();
            this.session.finish(false);
        }
    }

    void shutdown() {
        this.finished = true;
        clearPreview();
    }

    String getPlayerColorName() {
        return this.humanBlack ? "&8Black" : "&fWhite";
    }

    String getCurrentPlayerName() {
        return this.humanTurn ? this.session.getPlayer().getName() : this.session.getBot().getName();
    }

    int getRemainingTurnSeconds() {
        return Math.max(0, (this.turnTicks + 19) / 20);
    }

    int getPlacedStones() {
        return this.placed;
    }

    private void setupHuman() {
        Player player = this.session.getPlayer();
        PlayerUtil.reset(player, true, true);
        player.getInventory().setItem(4, new ItemBuilder(Material.ENDER_PEARL)
                .name("&6&lPlace Stone")
                .lore("&7Right-click while aiming at", "&7an empty board position.")
                .build());
        player.getInventory().setItem(8, GomokuItems.createSurrenderPotion());
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.teleport(viewLocation(0.0));
        player.updateInventory();
    }

    private void setupBot() {
        Player bot = this.session.getBot();
        bot.getInventory().clear();
        bot.getInventory().setArmorContents(new ItemStack[4]);
        bot.setGameMode(GameMode.ADVENTURE);
        bot.setAllowFlight(true);
        bot.setFlying(true);
        bot.teleport(viewLocation(3.0));
    }

    private Location viewLocation(double xOffset) {
        Location center = boardCenter().clone().add(xOffset, 60.0, 0.0);
        center.setYaw(0.0F);
        center.setPitch(90.0F);
        return center;
    }

    private int aiMoveTick() {
        int delay = switch (this.session.getDifficulty().getId().toLowerCase()) {
            case "easy" -> 32;
            case "hard" -> 8;
            default -> 18;
        };
        return delay;
    }

    private Cell chooseMove() {
        if (this.placed == 0) return new Cell(SIZE / 2, SIZE / 2);

        List<ScoredCell> choices = new ArrayList<>();
        int botColor = this.humanBlack ? WHITE : BLACK;
        int humanColor = opposite(botColor);
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                if (this.board[x][z] != EMPTY || !hasNeighbour(x, z)) continue;
                int score;
                if (wouldWin(x, z, botColor)) score = 1_000_000;
                else if (wouldWin(x, z, humanColor)) score = 900_000;
                else score = potential(x, z, botColor) * 2 + potential(x, z, humanColor);
                score -= Math.abs(x - SIZE / 2) + Math.abs(z - SIZE / 2);
                choices.add(new ScoredCell(new Cell(x, z), score));
            }
        }
        if (choices.isEmpty()) return null;
        choices.sort((first, second) -> Integer.compare(second.score(), first.score()));

        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (this.session.getDifficulty().getId().equalsIgnoreCase("easy") && random.nextDouble() < 0.55) {
            return choices.get(random.nextInt(Math.min(choices.size(), 16))).cell();
        }
        if (!this.session.getDifficulty().getId().equalsIgnoreCase("hard") && choices.size() > 1
                && random.nextDouble() < 0.25) {
            return choices.get(random.nextInt(Math.min(choices.size(), 4))).cell();
        }
        return choices.get(0).cell();
    }

    private boolean place(int x, int z, boolean humanMove) {
        if (!inside(x, z) || this.board[x][z] != EMPTY || this.humanTurn != humanMove) return false;
        clearPreview();
        finalizeLastStone();

        int color = this.blackTurn ? BLACK : WHITE;
        this.board[x][z] = color;
        this.placed++;
        this.lastX = x;
        this.lastZ = z;
        setPiece(x, z, this.blackTurn ? Material.NETHER_BRICKS : Material.SANDSTONE);
        playSound();

        if (hasFive(x, z, color)) {
            this.finished = true;
            finalizeLastStone();
            this.session.finish(humanMove);
        } else if (this.placed >= SIZE * SIZE) {
            this.finished = true;
            this.session.finish(false);
        } else {
            switchTurn();
        }
        return true;
    }

    private void switchTurn() {
        clearPreview();
        this.blackTurn = !this.blackTurn;
        this.humanTurn = this.blackTurn == this.humanBlack;
        this.turnTicks = 30 * 20;
        this.ticks = 0;
        announceTurn();
    }

    private void announceTurn() {
        String color = this.blackTurn ? "&8&lBLACK" : "&f&lWHITE";
        Player player = this.session.getPlayer();
        if (this.humanTurn) {
            player.sendTitle(CC.translate("&6&lYOUR TURN"),
                    CC.translate(color + " &7- Right-click the pearl to place."), 5, 30, 10);
        } else {
            player.sendTitle(CC.translate("&e&lBOT TURN"), CC.translate(color), 5, 25, 5);
        }
    }

    private void updatePreview() {
        Cell target = getTargetCell(this.session.getPlayer());
        if (target == null) {
            clearPreview();
            return;
        }
        if (target.equals(this.preview)) return;
        clearPreview();
        Material material = this.blackTurn ? Material.NETHER_BRICKS : Material.SANDSTONE;
        for (Location location : pieceLocations(target.x(), target.z())) {
            this.session.getPlayer().sendBlockChange(location, material.createBlockData());
        }
        this.preview = target;
    }

    private void clearPreview() {
        if (this.preview == null) return;
        for (Location location : pieceLocations(this.preview.x(), this.preview.z())) {
            this.session.getPlayer().sendBlockChange(location, location.getBlock().getBlockData());
        }
        this.preview = null;
    }

    private Cell getTargetCell(Player player) {
        RayTraceResult result = player.rayTraceBlocks(200.0, FluidCollisionMode.NEVER);
        if (result == null || result.getHitBlock() == null) return null;

        Location center = boardCenter();
        Vector hit = result.getHitPosition();
        int x = (int) Math.round((hit.getX() - center.getBlockX()) / SPACING) + SIZE / 2;
        int z = (int) Math.round((hit.getZ() - center.getBlockZ()) / SPACING) + SIZE / 2;
        if (!inside(x, z) || this.board[x][z] != EMPTY
                || Math.abs(result.getHitBlock().getY() - boardY()) > 3) return null;
        return new Cell(x, z);
    }

    private void setPiece(int x, int z, Material material) {
        for (Location location : pieceLocations(x, z)) {
            Block block = location.getBlock();
            this.session.recordPlacedBlock(block.getState());
            block.setType(material, false);
        }
    }

    private void finalizeLastStone() {
        if (this.lastX < 0) return;
        setPiece(this.lastX, this.lastZ,
                this.board[this.lastX][this.lastZ] == BLACK ? Material.COAL_BLOCK : Material.QUARTZ_BLOCK);
    }

    private List<Location> pieceLocations(int x, int z) {
        Location center = boardCenter();
        int blockX = center.getBlockX() + (x - SIZE / 2) * SPACING;
        int blockZ = center.getBlockZ() + (z - SIZE / 2) * SPACING;
        List<Location> locations = new ArrayList<>(21);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                locations.add(new Location(center.getWorld(), blockX + dx, boardY(), blockZ + dz));
            }
        }
        return locations;
    }

    private Location boardCenter() {
        Location center = this.session.getArena().getCenter();
        if (center != null) return center;
        Location first = this.session.getArena().getPos1();
        Location second = this.session.getArena().getPos2();
        return new Location(first.getWorld(),
                (first.getX() + second.getX()) * 0.5,
                (first.getY() + second.getY()) * 0.5,
                (first.getZ() + second.getZ()) * 0.5);
    }

    private int boardY() {
        return boardCenter().getBlockY() - 1;
    }

    private boolean wouldWin(int x, int z, int color) {
        this.board[x][z] = color;
        boolean win = hasFive(x, z, color);
        this.board[x][z] = EMPTY;
        return win;
    }

    private boolean hasFive(int x, int z, int color) {
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (int[] direction : directions) {
            if (1 + count(x, z, direction[0], direction[1], color)
                    + count(x, z, -direction[0], -direction[1], color) >= 5) return true;
        }
        return false;
    }

    private int potential(int x, int z, int color) {
        int best = 0;
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (int[] direction : directions) {
            int length = 1 + count(x, z, direction[0], direction[1], color)
                    + count(x, z, -direction[0], -direction[1], color);
            best = Math.max(best, length * length * 10);
        }
        return best;
    }

    private int count(int x, int z, int dx, int dz, int color) {
        int count = 0;
        for (int step = 1; step < 5; step++) {
            int nextX = x + dx * step;
            int nextZ = z + dz * step;
            if (!inside(nextX, nextZ) || this.board[nextX][nextZ] != color) break;
            count++;
        }
        return count;
    }

    private boolean hasNeighbour(int x, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if ((dx != 0 || dz != 0) && inside(x + dx, z + dz)
                        && this.board[x + dx][z + dz] != EMPTY) return true;
            }
        }
        return false;
    }

    private void playSound() {
        this.session.getPlayer().playSound(this.session.getPlayer().getLocation(),
                Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 2.0F);
    }

    private boolean inside(int x, int z) {
        return x >= 0 && x < SIZE && z >= 0 && z < SIZE;
    }

    private int opposite(int color) {
        return color == BLACK ? WHITE : BLACK;
    }

    private record Cell(int x, int z) {
    }

    private record ScoredCell(Cell cell, int score) {
    }
}
