package net.richardsprojects.lotrcompanions;

import com.github.maximuslotro.lotrrextended.ExtendedLog;

import lotr.common.init.LOTREntities;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent.MissingMappings;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;

@SuppressWarnings("deprecation")
public class CompanionsDataIdFixer {

	private static final ResourceLocation GONDOR_SOLDIER = new ResourceLocation("lotr", "hired_gondor_soldier");

	public static void fixEntityMappings(MissingMappings<EntityType<?>> event) {
		for(Mapping<EntityType<?>> mapping : event.getAllMappings()) {
			ResourceLocation entryLocation = mapping.key;
			if (entryLocation.equals(GONDOR_SOLDIER)) {
				mapping.remap(LOTREntities.GONDOR_SOLDIER.get());
				ExtendedLog.infoParms("Remapped entity, old id was=%s, new id is=%s", GONDOR_SOLDIER, LOTREntities.GONDOR_SOLDIER.get().getRegistryName());

				continue;
			}
		}
	}
}
