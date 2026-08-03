package dev.revere.alley.common;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

/**
 * @author Remi
 * @project alley-practice
 * @date 20/06/2025
 */
public class VoidChunkGenerator extends ChunkGenerator {
    /**
     * Creates the chunk data for the world generator.
     * 为世界生成器创建区块数据。
     *
     * @param world       the world to generate the chunk data for
     *                    要为其生成区块数据的世界
     * @param random      the random seed for chunk generation
     *                    区块生成的随机种子
     * @param x           the chunk's x-coordinate
     *                    区块的X坐标
     * @param z           the chunk's z-coordinate
     *                    区块的Z坐标
     * @param biomeGrid   the biome grid for the chunk
     *                    区块的生物群系网格
     * @return the chunk data for the world (empty world in this case)
     *         世界的区块数据（此情况下为空世界）
     */
    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biomeGrid) {
        ChunkData chunkData = this.createChunkData(world);
        for (int xPos = 0; xPos < 16; xPos++) {
            for (int zPos = 0; zPos < 16; zPos++) {
                biomeGrid.setBiome(xPos, zPos, Biome.PLAINS);
            }
        }

        return chunkData;
    }
}