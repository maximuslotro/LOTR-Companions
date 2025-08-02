package net.richardsprojects.lotrcompanions;

import net.minecraft.data.DataGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.richardsprojects.lotrcompanions.eventhandlers.ForgeEntityEvents;

@Mod(LOTRCompanions.MOD_ID)
public class LOTRCompanions {

    public static final String MOD_ID = "lotrcompanions";

    public static final ResourceLocation gondor_soldier = new ResourceLocation(MOD_ID, "gondor_soldier");

    public LOTRCompanions() {
    	// register Listeners that use the Forge Event Bus
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.register(ForgeEntityEvents.class);
		forgeBus.addGenericListener(EntityType.class, CompanionsDataIdFixer::fixEntityMappings);

        // register Listeners that use the Mod Event Bus
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::dataGen);
    }

    public void dataGen(GatherDataEvent event) {
    	DataGenerator generator = event.getGenerator();
    	generator.addProvider(new CompanionsHiredUnitProfileGenerator(generator, MOD_ID));
    }
}
