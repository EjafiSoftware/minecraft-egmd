package com.ejafi.egmd.portal;

final class RegisteredPortalBlocks {
    private RegisteredPortalBlocks() {}

    static MiningPortalBlock getOverworldPortalBlock() {
        return PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD).get();
    }
}
