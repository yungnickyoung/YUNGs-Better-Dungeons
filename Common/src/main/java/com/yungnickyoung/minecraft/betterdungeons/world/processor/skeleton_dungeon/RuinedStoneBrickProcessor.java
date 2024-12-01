package com.yungnickyoung.minecraft.betterdungeons.world.processor.skeleton_dungeon;

import com.mojang.serialization.MapCodec;
import com.yungnickyoung.minecraft.betterdungeons.module.StructureProcessorTypeModule;
import com.yungnickyoung.minecraft.yungsapi.api.world.randomize.BlockStateRandomizer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Replaces yellow glass w/ air in skeleton dungeons when air is already there,
 * giving them a more natural, ruined look that opens up to caves.
 * Replaces them w/ stone bricks otherwise.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RuinedStoneBrickProcessor extends StructureProcessor {
    public static final RuinedStoneBrickProcessor INSTANCE = new RuinedStoneBrickProcessor();
    public static final MapCodec<RuinedStoneBrickProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private static final BlockStateRandomizer STONE_BRICK_SELECTOR = new BlockStateRandomizer(Blocks.STONE_BRICKS.defaultBlockState())
        .addBlock(Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 0.3f)
        .addBlock(Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), 0.2f);

    private static final BlockStateRandomizer STONE_BRICK_SLAB_SELECTOR = new BlockStateRandomizer(Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP))
        .addBlock(Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP), 0.3f);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader,
                                                             BlockPos jigsawPiecePos,
                                                             BlockPos jigsawPieceBottomCenterPos,
                                                             StructureTemplate.StructureBlockInfo blockInfoLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings structurePlacementData) {
        if (blockInfoGlobal.state().getBlock() == Blocks.YELLOW_STAINED_GLASS) {
            if (levelReader.getBlockState(blockInfoGlobal.pos()).isAir()) {
                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), null);
            } else {
                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), STONE_BRICK_SELECTOR.get(structurePlacementData.getRandom(blockInfoGlobal.pos())), null);
            }
        } else if (blockInfoGlobal.state().getBlock() == Blocks.PRISMARINE_BRICK_SLAB) {
            if (levelReader.getBlockState(blockInfoGlobal.pos()).isAir()) {
                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), null);
            } else {
                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), STONE_BRICK_SLAB_SELECTOR.get(structurePlacementData.getRandom(blockInfoGlobal.pos())), blockInfoGlobal.nbt());
            }
        }
        return blockInfoGlobal;
    }

    protected StructureProcessorType<?> getType() {
        return StructureProcessorTypeModule.SKELETON_DUNGEON_RUINED_STONE_BRICKS_PROCESSOR;
    }
}
