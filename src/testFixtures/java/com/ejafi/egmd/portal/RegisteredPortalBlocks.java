package com.ejafi.egmd.portal;

final class RegisteredPortalBlocks {
    private RegisteredPortalBlocks() {}

    static DimensionPortalBlock getOverworldMiningPortalBlock() {
        return PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING).get();
    }
}
