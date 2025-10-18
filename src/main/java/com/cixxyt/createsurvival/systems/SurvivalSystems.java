package com.cixxyt.createsurvival.systems;

import net.minecraftforge.common.MinecraftForge;

/**
 * Central bootstrap for the survival subsystems we are authoring in-house.  Having a dedicated
 * registrar keeps the mod entry point tidy and demonstrates how to wire multiple event listeners in
 * a readable, professor-approved fashion.
 */
public final class SurvivalSystems {
    private SurvivalSystems() {
    }

    /**
     * Registers each gameplay manager with Forge's global event bus.  We call this once during
     * mod construction, mirroring how registry objects subscribe to their respective callbacks.
     */
    public static void init() {
        // Each system exposes a lightweight listener object so we only subscribe the handlers we
        // actually need.  Instantiating them here makes the lifecycle explicit for readers following
        // along at home.
        MinecraftForge.EVENT_BUS.register(new ThirstSystem.PlayerHooks());
        MinecraftForge.EVENT_BUS.register(new TemperatureSystem.PlayerHooks());
    }
}
