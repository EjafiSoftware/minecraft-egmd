package com.ejafi.egmd.portal;

sealed interface PortalSite {
    PortalFootprint footprint();

    record Open(PortalFootprint footprint) implements PortalSite {}

    record Forced(PortalFootprint footprint) implements PortalSite {}
}
