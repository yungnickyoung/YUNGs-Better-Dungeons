package com.yungnickyoung.minecraft.betterdungeons.world.processor.zombie_dungeon;

import com.mojang.serialization.Codec;
import com.yungnickyoung.minecraft.betterdungeons.init.BDModProcessors;
import com.yungnickyoung.minecraft.betterdungeons.mixin.accessor.BaseSpawnerAccessor;
import com.yungnickyoung.minecraft.betterdungeons.mixin.accessor.StructureBlockInfoAccessor;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

/**
 * Sets mob spawners to spawn skeletons w/ swords.
 */
public class ZombieTombstoneSpawnerProcessor extends StructureProcessor {
    public static final ZombieTombstoneSpawnerProcessor INSTANCE = new ZombieTombstoneSpawnerProcessor();
    public static final Codec<ZombieTombstoneSpawnerProcessor> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader,
                                                             BlockPos jigsawPiecePos,
                                                             BlockPos jigsawPieceBottomCenterPos,
                                                             StructureTemplate.StructureBlockInfo blockInfoLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings structurePlacementData) {
        if (blockInfoGlobal.state.getBlock() == Blocks.BLACK_STAINED_GLASS) {
            // Create spawner & populate with data
            BaseSpawner spawner = new BaseSpawner() {
                @Override
                public void broadcastEvent(Level level, BlockPos blockPos, int i) {
                    // no-op
                }
            };

            SimpleWeightedRandomList<SpawnData> spawnData = SimpleWeightedRandomList.single(new SpawnData(
                    Util.make(new CompoundTag(), (compoundTag) -> {
                        compoundTag.putString("id", "minecraft:skeleton");
                        ListTag handDropChances = new ListTag();
                        handDropChances.add(FloatTag.valueOf(.2f));
                        handDropChances.add(FloatTag.valueOf(0f));
                        compoundTag.put("HandDropChances", handDropChances);
                        ListTag handItems = new ListTag();
                        ItemStack itemStack = new ItemStack(Items.IRON_SWORD);
                        CompoundTag ironSwordNBT = new CompoundTag();
                        itemStack.save(ironSwordNBT);
                        handItems.add(ironSwordNBT);
                        handItems.add(new CompoundTag());
                        compoundTag.put("HandItems", handItems);
                    }),
                    Optional.empty()));
            ((BaseSpawnerAccessor)spawner).setSpawnPotentials(spawnData);
            spawner.setEntityId(Registry.ENTITY_TYPE.get(new ResourceLocation("minecraft:skeleton")));

            // Save spawner data to NBT
            CompoundTag nbt = new CompoundTag();
            spawner.save(nbt);

            // Update blockstate
            blockInfoGlobal = new StructureTemplate.StructureBlockInfo(
                    blockInfoGlobal.pos,
                    Blocks.SPAWNER.defaultBlockState(),
                    nbt);
        }
        return blockInfoGlobal;
    }

    protected StructureProcessorType<?> getType() {
        return BDModProcessors.ZOMBIE_TOMBSTONE_SPAWNER_PROCESSOR;
    }
}
