package com.ejafi.egmd.pearl;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PearlBlockGameTests {
    private static final BlockPos PEARL_POS = new BlockPos(3, 1, 3);

    private PearlBlockGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void get_drops_returns_pearl_block_when_tool_has_silk_touch(GameTestHelper helper) {
        ItemStack pickaxe = createEnchantedPickaxe(helper, Enchantments.SILK_TOUCH);

        List<Item> drops = mineWith(helper, pickaxe);

        helper.assertValueEqual(drops, List.of(PearlBlocks.PEARL_BLOCK.asItem()), "drops");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void get_drops_returns_ender_pearls_when_tool_lacks_silk_touch(GameTestHelper helper) {
        List<Item> drops = mineWith(helper, new ItemStack(Items.DIAMOND_PICKAXE));

        helper.assertValueEqual(drops.stream().distinct().toList(), List.of(Items.ENDER_PEARL), "drops");
        helper.succeed();
    }

    private static ItemStack createEnchantedPickaxe(GameTestHelper helper, ResourceKey<Enchantment> enchantment) {
        var pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.enchant(
                helper.getLevel()
                        .registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(enchantment),
                1);
        return pickaxe;
    }

    private static List<Item> mineWith(GameTestHelper helper, ItemStack tool) {
        return Block.getDrops(
                        PearlBlocks.PEARL_BLOCK.get().defaultBlockState(),
                        helper.getLevel(),
                        helper.absolutePos(PEARL_POS),
                        null,
                        null,
                        tool)
                .stream()
                .map(ItemStack::getItem)
                .toList();
    }
}
