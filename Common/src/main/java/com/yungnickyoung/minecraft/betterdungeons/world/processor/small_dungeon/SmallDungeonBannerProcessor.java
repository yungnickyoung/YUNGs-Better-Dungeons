package com.yungnickyoung.minecraft.betterdungeons.world.processor.small_dungeon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yungnickyoung.minecraft.betterdungeons.BetterDungeonsCommon;
import com.yungnickyoung.minecraft.betterdungeons.module.StructureProcessorTypeModule;
import com.yungnickyoung.minecraft.betterdungeons.world.DungeonContext;
import com.yungnickyoung.minecraft.betterdungeons.world.DungeonType;
import com.yungnickyoung.minecraft.yungsapi.world.banner.Banner;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Replaces wall banners with a banner corresponding to the dungeon type.
 * Also removes existing banners to ensure the number of banners per structure
 * falls within the desired range.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmallDungeonBannerProcessor extends StructureProcessor {
    public static final MapCodec<SmallDungeonBannerProcessor> CODEC = RecordCodecBuilder.mapCodec(codecBuilder -> codecBuilder
            .group(
                    Codec.STRING
                            .fieldOf("dungeon_type")
                            .forGetter(smallDungeonBannerProcessor -> smallDungeonBannerProcessor.getDungeonType().getName()))
            .apply(codecBuilder, codecBuilder.stable(SmallDungeonBannerProcessor::new)));

    private SmallDungeonBannerProcessor(String dungeonType) {
        this.dungeonType = DungeonType.fromString(dungeonType);
    }

    private final DungeonType dungeonType;

    public DungeonType getDungeonType() {
        return this.dungeonType;
    }

    // All banners
    public static final Banner SMALL_DUNGEON_SKELETON_BANNER = new Banner.Builder()
            .blockState(Blocks.BLACK_WALL_BANNER.defaultBlockState())
            .pattern(BannerPatterns.CURLY_BORDER, DyeColor.WHITE)
            .pattern(BannerPatterns.STRIPE_CENTER, DyeColor.WHITE)
            .pattern(BannerPatterns.STRIPE_BOTTOM, DyeColor.BLACK)
            .pattern(BannerPatterns.STRIPE_TOP, DyeColor.WHITE)
            .pattern(BannerPatterns.CREEPER, DyeColor.WHITE)
            .pattern(BannerPatterns.GRADIENT, DyeColor.BLACK)
            .customName("Vengeful Banner")
            .customColor("dark_gray")
            .build();

    public static final Banner SMALL_DUNGEON_ZOMBIE_BANNER = new Banner.Builder()
            .blockState(Blocks.RED_WALL_BANNER.defaultBlockState())
            .pattern(BannerPatterns.TRIANGLE_BOTTOM, DyeColor.PINK)
            .pattern(BannerPatterns.CIRCLE_MIDDLE, DyeColor.GRAY)
            .pattern(BannerPatterns.GRADIENT, DyeColor.BLACK)
            .pattern(BannerPatterns.TRIANGLES_TOP, DyeColor.WHITE)
            .pattern(BannerPatterns.TRIANGLES_BOTTOM, DyeColor.WHITE)
            .pattern(BannerPatterns.BORDER, DyeColor.GREEN)
            .customName("Foul Banner")
            .customColor("dark_green")
            .build();

    public static final Banner SMALL_DUNGEON_SPIDER_BANNER = new Banner.Builder()
            .blockState(Blocks.RED_WALL_BANNER.defaultBlockState())
            .pattern(BannerPatterns.FLOWER, DyeColor.GRAY)
            .pattern(BannerPatterns.BORDER, DyeColor.GRAY)
            .pattern(BannerPatterns.STRAIGHT_CROSS, DyeColor.GRAY)
            .pattern(BannerPatterns.HALF_HORIZONTAL, DyeColor.GRAY)
            .pattern(BannerPatterns.STRIPE_BOTTOM, DyeColor.GRAY)
            .pattern(BannerPatterns.GRADIENT, DyeColor.BLACK)
            .customName("Haunted Banner")
            .customColor("dark_red")
            .build();

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader,
                                                             BlockPos jigsawPiecePos,
                                                             BlockPos jigsawPieceBottomCenterPos,
                                                             StructureTemplate.StructureBlockInfo blockInfoLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings structurePlacementData) {
        if (blockInfoGlobal.state().getBlock() instanceof AbstractBannerBlock) {
            // Make sure we only operate on the placeholder banners
            if (blockInfoGlobal.state().getBlock() == Blocks.RED_WALL_BANNER && (blockInfoGlobal.nbt().get("patterns") == null || blockInfoGlobal.nbt().getList("patterns", 10).isEmpty())) {
                // Fetch thread-local dungeon context
                DungeonContext context = DungeonContext.peek();

                // Check dungeon context to see if we have reached the max banner count for this structure piece
                if (context.getBannerCount() >= BetterDungeonsCommon.CONFIG.smallDungeons.bannerMaxCount)
                    return new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), null);

                // Chance of a banner spawning
                RandomSource random = structurePlacementData.getRandom(blockInfoGlobal.pos());
                if (random.nextFloat() > .1f) {
                    return new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), null);
                }

                Banner banner = getBannerForType();
                Direction facing = blockInfoGlobal.state().getValue(BlockStateProperties.HORIZONTAL_FACING);
                BlockState newState = banner.getState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
                CompoundTag newNBT = copyNBT(banner.getNbt());

                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), newState, newNBT);
                context.incrementBannerCount();
            }
        }
        return blockInfoGlobal;
    }

    protected StructureProcessorType<?> getType() {
        return StructureProcessorTypeModule.SMALL_DUNGEON_BANNER_PROCESSOR;
    }

    private Banner getBannerForType() {
        return switch (this.dungeonType) {
            case SKELETON -> SMALL_DUNGEON_SKELETON_BANNER;
            case ZOMBIE -> SMALL_DUNGEON_ZOMBIE_BANNER;
            case SPIDER -> SMALL_DUNGEON_SPIDER_BANNER;
            default -> {
                BetterDungeonsCommon.LOGGER.warn("Invalid DungeonType {} for BannerProcessor! This shouldn't happen!", this.dungeonType);
                yield SMALL_DUNGEON_SKELETON_BANNER;
            }
        };
    }

    private CompoundTag copyNBT(CompoundTag other) {
        CompoundTag nbt = new CompoundTag();
        nbt.merge(other);
        return nbt;
    }
}
