package com.ejafi.egmd.datagen.loot;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.pearl.PearlBlocks;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class EgmdBlockLootProvider extends BlockLootSubProvider {
    public EgmdBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        Block pearlBlock = PearlBlocks.PEARL_BLOCK.get();
        Holder<Enchantment> fortune =
                registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        add(
                pearlBlock,
                createSilkTouchDispatchTable(
                        pearlBlock,
                        applyExplosionDecay(
                                pearlBlock,
                                LootItem.lootTableItem(Items.ENDER_PEARL)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F)))
                                        .apply(ApplyBonusCount.addUniformBonusCount(fortune)))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.stream()
                .filter(block ->
                        Egmd.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace()))
                .toList();
    }
}
