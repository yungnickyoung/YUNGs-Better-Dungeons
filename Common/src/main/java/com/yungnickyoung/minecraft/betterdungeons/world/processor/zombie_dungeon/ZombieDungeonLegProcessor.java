package com.yungnickyoung.minecraft.betterdungeons.world.processor.zombie_dungeon;

import com.mojang.serialization.MapCodec;
import com.yungnickyoung.minecraft.betterdungeons.module.StructureProcessorTypeModule;
import com.yungnickyoung.minecraft.yungsapi.api.world.randomize.BlockStateRandomizer;
import com.yungnickyoung.minecraft.yungsapi.world.structure.processor.ISafeWorldModifier;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * Dynamically generates support legs below zombie dungeons.
 * Magenta stained glass is used to mark the positions where the legs will spawn for simplicity.
 * Purpur slabs are to be replaced with smooth stone slabs if air is present.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ZombieDungeonLegProcessor extends StructureProcessor implements ISafeWorldModifier {
    public static final ZombieDungeonLegProcessor INSTANCE = new ZombieDungeonLegProcessor();
    public static final MapCodec<ZombieDungeonLegProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private static final BlockStateRandomizer LEG_SELECTOR = new BlockStateRandomizer(Blocks.COBBLESTONE.defaultBlockState())
        .addBlock(Blocks.POLISHED_ANDESITE.defaultBlockState(), 0.8f);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader,
                                                             BlockPos jigsawPiecePos,
                                                             BlockPos jigsawPieceBottomCenterPos,
                                                             StructureTemplate.StructureBlockInfo blockInfoLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings structurePlacementData) {
        if (blockInfoGlobal.state().getBlock() == Blocks.MAGENTA_STAINED_GLASS) {
            if (levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(new ChunkPos(blockInfoGlobal.pos()))) {
                return blockInfoGlobal;
            }

            RandomSource random = structurePlacementData.getRandom(blockInfoGlobal.pos());

            // Always replace the glass itself with smooth stone
            Optional<BlockState> blockState = getBlockStateSafe(levelReader, blockInfoGlobal.pos());
            if (blockState.isEmpty() || blockState.get().isAir() || blockState.get().liquid()) {
                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.SMOOTH_STONE.defaultBlockState(), null);
            } else {
                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), blockState.get(), blockInfoGlobal.nbt());
            }

            BlockPos.MutableBlockPos mutable = blockInfoGlobal.pos().mutable().move(Direction.DOWN);
            BlockState currBlockState = levelReader.getBlockState(mutable);

            // Generate vertical pillar down
            while (mutable.getY() > levelReader.getMinBuildHeight()
                    && mutable.getY() < levelReader.getMaxBuildHeight()
                    && (currBlockState.isAir() || !levelReader.getFluidState(mutable).isEmpty())) {
                levelReader.getChunk(mutable).setBlockState(mutable, LEG_SELECTOR.get(random), false);
                mutable.move(Direction.DOWN);
                currBlockState = levelReader.getBlockState(mutable);
            }
        } else if (blockInfoGlobal.state().getBlock() == Blocks.PURPUR_SLAB) {
            Optional<BlockState> blockState = getBlockStateSafe(levelReader, blockInfoGlobal.pos());
            if (blockState.isEmpty() || blockState.get().isAir() || blockState.get().liquid()) {
                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), null);
            } else {
                blockInfoGlobal = null;
            }
        }

        return blockInfoGlobal;
    }

    protected StructureProcessorType<?> getType() {
        return StructureProcessorTypeModule.ZOMBIE_DUNGEON_LEG_PROCESSOR;
    }
}
