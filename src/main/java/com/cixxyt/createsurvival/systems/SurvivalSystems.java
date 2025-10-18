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
        // Rather than rely on anonymous inner listeners, we hook the systems' static consumers
        // directly.  This makes the wiring crystal clear to students reading the call-site while
        // avoiding accidental casts between unrelated listener classes (the culprit behind a crash
        // report we received during QA).
        MinecraftForge.EVENT_BUS.addListener(ThirstSystem::handlePlayerClone);
        MinecraftForge.EVENT_BUS.addListener(ThirstSystem::handlePlayerTick);
        MinecraftForge.EVENT_BUS.addListener(TemperatureSystem::handlePlayerClone);
        MinecraftForge.EVENT_BUS.addListener(TemperatureSystem::handlePlayerTick);
    }
}
