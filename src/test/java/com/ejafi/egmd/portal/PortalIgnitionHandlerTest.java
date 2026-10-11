package com.ejafi.egmd.portal;

import static org.assertj.core.api.Assertions.assertThat;

import net.datafaker.Faker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class PortalIgnitionHandlerTest {
    private static final Faker FAKER = new Faker();

    @Test
    void isIgniter_acceptsItem_whenItIsFlintAndSteel() {
        var stack = new ItemStack(Items.FLINT_AND_STEEL);

        boolean igniter = PortalIgnitionHandler.isIgniter(stack);

        assertThat(igniter).isTrue();
    }

    @Test
    void isIgniter_rejectsItem_whenItCannotStartFire() {
        var stack = new ItemStack(FAKER.options().option(Items.STONE, Items.STICK, Items.DIAMOND_PICKAXE));

        boolean igniter = PortalIgnitionHandler.isIgniter(stack);

        assertThat(igniter).isFalse();
    }
}
