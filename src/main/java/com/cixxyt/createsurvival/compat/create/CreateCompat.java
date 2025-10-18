package com.cixxyt.createsurvival.compat.create;

import com.cixxyt.createsurvival.registry.ModRecipes;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Objects;

/**
 * Placeholder for future Create API hooks.  The addon already assumes Create is installed, but the
 * helper keeps the call site consistent with our other optional integrations.
 */
public final class CreateCompat {
    private CreateCompat() {}

    public static void registerIntegration(IEventBus eventBus) {
        Objects.requireNonNull(eventBus, "The Create integration expects a live mod event bus.");

        // Even though the caller already checked the mod list, we defensively keep this helper free
        // of Create references until it is actually invoked.  That way a future refactor that moves
        // the guard will not trip a class-loading exception on standalone installs.
        registerCreateRecipes();
    }

    private static void registerCreateRecipes() {
        // The recipe bridge is intentionally pulled into its own method so we can expand the
        // integration with listeners or registries later without cluttering the public surface.
        // For now the only Create-specific data we own are the bespoke filling rules that teach the
        // Spout how to interact with the clockwork canteen.
        ModRecipes.registerRecipes();
    }
}
