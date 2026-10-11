package com.ejafi.egmd.portal;

sealed interface ArrivalTarget {
    PortalFootprint footprint();

    record OnFlatSurface(PortalFootprint footprint) implements ArrivalTarget {}

    record OnTerrain(PortalFootprint footprint) implements ArrivalTarget {}
}
