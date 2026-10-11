package com.ejafi.egmd.datagen;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.pearl.PearlBlocks;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

final class EgmdBlockLootProvider extends BlockLootSubProvider {
    EgmdBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        Block pearlBlock = PearlBlocks.PEARL_BLOCK.get();
        add(
                pearlBlock,
                LootTable.lootTable()
                        .withPool(applyExplosionCondition(
                                pearlBlock,
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(LootItem.lootTableItem(Items.ENDER_PEARL)
                                                .apply(SetItemCountFunction.setCount(
                                                        UniformGenerator.between(1.0F, 4.0F)))))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.stream()
                .filter(block ->
                        BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(EGMD.MOD_ID))
                .toList();
    }
}
