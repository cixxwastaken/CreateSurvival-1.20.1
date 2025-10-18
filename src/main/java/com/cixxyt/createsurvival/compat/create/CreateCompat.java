package com.cixxyt.createsurvival.compat.create;

import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Placeholder for future Create API hooks.  The addon already assumes Create is installed, but the
 * helper keeps the call site consistent with our other optional integrations.
 */
public final class CreateCompat {
    private CreateCompat() {}

    public static void registerIntegration(IEventBus eventBus) {
        // No-op for now; recipes already reference Create components and therefore naturally gate
        // progression.  This method exists primarily to mirror the structure of the other compat
        // helpers and to give future patches a home for Create-specific registries.
    }
}
