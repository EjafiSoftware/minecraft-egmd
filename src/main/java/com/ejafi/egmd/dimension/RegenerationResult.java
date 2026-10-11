package com.ejafi.egmd.dimension;

import java.nio.file.Path;

sealed interface RegenerationResult {
    record NotMiningDimension() implements RegenerationResult {}

    record PlayersStranded(int strandedCount) implements RegenerationResult {}

    record Evacuated(int evacuatedCount, Path storageFolder) implements RegenerationResult {}
}
