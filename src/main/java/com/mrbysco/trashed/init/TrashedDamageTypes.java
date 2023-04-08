package com.mrbysco.trashed.init;

import com.mrbysco.trashed.Trashed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class TrashedDamageTypes {
	public static final ResourceKey<DamageType> TRASHED = register("trashed");

	private static ResourceKey<DamageType> register(String name) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Trashed.MOD_ID, name));
	}
}
