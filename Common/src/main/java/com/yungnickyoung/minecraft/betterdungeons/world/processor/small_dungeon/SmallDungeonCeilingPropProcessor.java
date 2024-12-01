package com.yungnickyoung.minecraft.betterdungeons.world.processor.small_dungeon;

import com.mojang.serialization.MapCodec;
import com.yungnickyoung.minecraft.betterdungeons.module.StructureProcessorTypeModule;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmallDungeonCeilingPropProcessor extends StructureProcessor {
    public static final SmallDungeonCeilingPropProcessor INSTANCE = new SmallDungeonCeilingPropProcessor();
    public static final MapCodec<SmallDungeonCeilingPropProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader,
                                                             BlockPos jigsawPiecePos,
                                                             BlockPos jigsawPieceBottomCenterPos,
                                                             StructureTemplate.StructureBlockInfo blockInfoLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings structurePlacementData) {
        if (blockInfoGlobal.state().is(Blocks.MAGENTA_STAINED_GLASS)) {
            // If ceiling isn't solid, place air since we don't want floating props
            if (!levelReader.getBlockState(blockInfoGlobal.pos().above()).isFaceSturdy(levelReader, blockInfoGlobal.pos().above(), Direction.DOWN)) {
                return new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), null);
            }

            RandomSource random = structurePlacementData.getRandom(blockInfoGlobal.pos());
            float f = random.nextFloat();

            // Choose ceiling prop
            if (f < .2f) blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CHAIN.defaultBlockState(), blockInfoGlobal.nbt());
            else blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), blockInfoGlobal.nbt());
        } else if (blockInfoGlobal.state().is(Blocks.BROWN_STAINED_GLASS)) {
            // If ceiling isn't solid, simply ignore processing since we don't want floating props
            if (!levelReader.getBlockState(blockInfoGlobal.pos().above(2)).isFaceSturdy(levelReader, blockInfoGlobal.pos().above(), Direction.DOWN)) {
                return new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), null);
            }

            RandomSource random = structurePlacementData.getRandom(blockInfoGlobal.pos());
            float f = random.nextFloat();

            // Choose ceiling prop
            if (f < .5f) blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CHAIN.defaultBlockState(), blockInfoGlobal.nbt());
            else blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), blockInfoGlobal.nbt());
        } else if (blockInfoGlobal.state().is(Blocks.CHAIN)) {
            // If ceiling isn't solid, don't place top chains for potential double chains if they would be floating
            if (!levelReader.getBlockState(blockInfoGlobal.pos().above()).isFaceSturdy(levelReader, blockInfoGlobal.pos().above(), Direction.DOWN)) {
                return new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), null);
            }
        }

        return blockInfoGlobal;
    }

    protected StructureProcessorType<?> getType() {
        return StructureProcessorTypeModule.SMALL_DUNGEON_CEILING_PROP_PROCESSOR;
    }
}