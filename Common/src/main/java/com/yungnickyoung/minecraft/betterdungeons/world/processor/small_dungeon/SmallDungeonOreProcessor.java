package com.yungnickyoung.minecraft.betterdungeons.world.processor.small_dungeon;

import com.mojang.serialization.MapCodec;
import com.yungnickyoung.minecraft.betterdungeons.BetterDungeonsCommon;
import com.yungnickyoung.minecraft.betterdungeons.module.StructureProcessorTypeModule;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

/**
 * Replaces ore in props with cobblestone if ores are disabled in the config.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmallDungeonOreProcessor extends StructureProcessor {
    public static final SmallDungeonOreProcessor INSTANCE = new SmallDungeonOreProcessor();
    public static final MapCodec<SmallDungeonOreProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private static final Predicate<BlockState> isOre = blockState ->
            blockState.is(BlockTags.GOLD_ORES) ||
            blockState.is(BlockTags.IRON_ORES) ||
            blockState.is(BlockTags.DIAMOND_ORES) ||
            blockState.is(BlockTags.REDSTONE_ORES) ||
            blockState.is(BlockTags.LAPIS_ORES) ||
            blockState.is(BlockTags.COAL_ORES) ||
            blockState.is(BlockTags.EMERALD_ORES) ||
            blockState.is(BlockTags.COPPER_ORES);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader,
                                                             BlockPos jigsawPiecePos,
                                                             BlockPos jigsawPieceBottomCenterPos,
                                                             StructureTemplate.StructureBlockInfo blockInfoLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings structurePlacementData) {
        if (isOre.test(blockInfoGlobal.state())) {
            if (!BetterDungeonsCommon.CONFIG.smallDungeons.enableOreProps) {
                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), null);
            }
        }
        return blockInfoGlobal;
    }

    protected StructureProcessorType<?> getType() {
        return StructureProcessorTypeModule.SMALL_DUNGEON_ORE_PROCESSOR;
    }
}
