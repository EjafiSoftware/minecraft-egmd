package com.ejafi.egmd.portal;

import com.ejafi.egmd.BlockPosFaker;
import net.datafaker.Faker;
import net.minecraft.core.Direction.Axis;

final class PortalFootprintFaker {
    private static final Faker FAKER = new Faker();
    private static final int LOWEST_HOSTABLE_Y = BlockPosFaker.MIN_BUILD_Y + 2;
    private static final int HIGHEST_HOSTABLE_Y = BlockPosFaker.MAX_BUILD_Y - PortalFrame.MIN_HEIGHT - 1;

    private final BlockPosFaker origin =
            new BlockPosFaker().withY(FAKER.number().numberBetween(LOWEST_HOSTABLE_Y, HIGHEST_HOSTABLE_Y + 1));
    private Axis axis = generateAxis();

    static Axis generateAxis() {
        return FAKER.options().option(Axis.X, Axis.Z);
    }

    PortalFootprintFaker withOriginX(int x) {
        origin.withX(x);
        return this;
    }

    PortalFootprintFaker withOriginY(int y) {
        origin.withY(y);
        return this;
    }

    PortalFootprintFaker withOriginZ(int z) {
        origin.withZ(z);
        return this;
    }

    PortalFootprintFaker withAxis(Axis axis) {
        this.axis = axis;
        return this;
    }

    PortalFootprint generate() {
        return new PortalFootprint(origin.generate(), axis);
    }
}
