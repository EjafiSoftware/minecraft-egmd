package com.ejafi.egmd;

import net.datafaker.Faker;
import net.minecraft.core.BlockPos;

public final class BlockPosFaker {
    public static final int MIN_BUILD_Y = -64;
    public static final int MAX_BUILD_Y = 320;
    public static final int HORIZONTAL_LIMIT = 1_000_000;

    private static final Faker FAKER = new Faker();

    private int x = FAKER.number().numberBetween(-HORIZONTAL_LIMIT, HORIZONTAL_LIMIT);
    private int y = FAKER.number().numberBetween(MIN_BUILD_Y, MAX_BUILD_Y);
    private int z = FAKER.number().numberBetween(-HORIZONTAL_LIMIT, HORIZONTAL_LIMIT);

    public BlockPosFaker withX(int x) {
        this.x = x;
        return this;
    }

    public BlockPosFaker withY(int y) {
        this.y = y;
        return this;
    }

    public BlockPosFaker withZ(int z) {
        this.z = z;
        return this;
    }

    public BlockPos generate() {
        return new BlockPos(x, y, z);
    }
}
