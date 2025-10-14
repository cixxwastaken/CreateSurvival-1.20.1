package com.cixxyt.createsurvival;

//import com.cixxyt.createsurvival.registry.ModItems;
//import com.cixxyt.createsurvival.registry.ModBlocks;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CreateSurvivalMod.MODID)
public class CreateSurvivalMod {
    public static final String MODID = "createsurvival";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateSurvivalMod() {
        LOGGER.info("Create Survival Mod Loaded!");
        // Call registry classes here
//        ModItems.register();
//        ModBlocks.register();
    }
}
