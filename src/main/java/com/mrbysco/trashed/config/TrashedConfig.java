package com.mrbysco.trashed.config;

import com.mrbysco.trashed.Trashed;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

public class TrashedConfig {
	public static class Server {
		public final IntValue itemTrashQuantity;

		Server(ModConfigSpec.Builder builder) {
			builder.comment("Trashing settings")
					.push("trashing");

			itemTrashQuantity = builder
					.comment("The quantity of items the Trash Can destroys every cycle [Default: 1]")
					.defineInRange("itemTrashQuantity", 1, 1, 64);

			builder.pop();
		}
	}

	public static final ModConfigSpec serverSpec;
	public static final Server SERVER;

	static {
		final Pair<Server, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Server::new);
		serverSpec = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		Trashed.LOGGER.debug("Loaded Trashed config file {}", configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
		Trashed.LOGGER.warn("Trashed config just got changed on the file system!");
	}
}
