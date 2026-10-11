package com.ejafi.egmd.dimension;

sealed interface OverworldMove {
    record Arrived() implements OverworldMove {}

    record Canceled() implements OverworldMove {}
}
