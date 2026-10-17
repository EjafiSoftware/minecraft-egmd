package com.ejafi.egmd.dimension;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.datafaker.Faker;

public final class RestrictionsFaker {
    private static final Faker FAKER = new Faker();

    private RestrictionsFaker() {}

    public static Set<Restriction> generate() {
        return Arrays.stream(Restriction.values())
                .filter(restriction -> FAKER.bool().bool())
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Set<Restriction> generateIncluding(Restriction required) {
        return Stream.concat(generate().stream(), Stream.of(required)).collect(Collectors.toUnmodifiableSet());
    }

    public static Set<Restriction> generateExcluding(Restriction excluded) {
        return generate().stream()
                .filter(restriction -> restriction != excluded)
                .collect(Collectors.toUnmodifiableSet());
    }
}
