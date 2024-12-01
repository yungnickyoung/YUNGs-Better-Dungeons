package com.yungnickyoung.minecraft.betterdungeons.world.processor.small_dungeon;

import com.mojang.serialization.MapCodec;
import com.yungnickyoung.minecraft.betterdungeons.BetterDungeonsCommon;
import com.yungnickyoung.minecraft.betterdungeons.module.StructureProcessorTypeModule;
import com.yungnickyoung.minecraft.betterdungeons.world.DungeonContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Replaces some chests with air, ensuring at least 1 per dungeon but no more than 2.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmallDungeonChestProcessor extends StructureProcessor {
    public static final SmallDungeonChestProcessor INSTANCE = new SmallDungeonChestProcessor();
    public static final MapCodec<SmallDungeonChestProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader,
                                                             BlockPos jigsawPiecePos,
                                                             BlockPos jigsawPieceBottomCenterPos,
                                                             StructureTemplate.StructureBlockInfo blockInfoLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings structurePlacementData) {
        if (blockInfoGlobal.state().getBlock() instanceof ChestBlock) {
            // Fetch thread-local dungeon context
            DungeonContext context = DungeonContext.peek();
            int chestCount = DungeonContext.peek().getChestCount();

            if (chestCount < BetterDungeonsCommon.CONFIG.smallDungeons.chestMinCount) { // Ensure there is at least minimum amount of chests
                context.incrementChestCount();
            } else if (chestCount < BetterDungeonsCommon.CONFIG.smallDungeons.chestMaxCount) { // 20% chance of additional chest, per chest prop
                RandomSource random = structurePlacementData.getRandom(blockInfoGlobal.pos());
                if (random.nextFloat() > .2f) {
                    return new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), null);
                }
                context.incrementChestCount();
            } else { // Can't spawn more than max chests
                return new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), null);
            }
        }
        return blockInfoGlobal;
    }

    protected StructureProcessorType<?> getType() {
        return StructureProcessorTypeModule.SMALL_DUNGEON_CHEST_PROCESSOR;
    }
}
