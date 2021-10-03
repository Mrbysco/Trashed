package com.mrbysco.trashed.config;

import com.mrbysco.trashed.Trashed;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class TrashedConfig {
    public static class Server {
        public final IntValue itemTrashQuantity;

        Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Trashing settings")
                    .push("trashing");

            itemTrashQuantity = builder
                    .comment("The quantity of items the Trash Can destroys every cycle [Default: 1]")
                    .defineInRange("itemTrashQuantity", 1, 1, 64);

            builder.pop();
        }
    }

    public static final ForgeConfigSpec serverSpec;
    public static final Server SERVER;
    static {
        final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        serverSpec = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        Trashed.LOGGER.debug("Loaded Trashed config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading configEvent) {
        Trashed.LOGGER.fatal("Trashed config just got changed on the file system!");
    }
}
