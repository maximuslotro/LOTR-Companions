package net.richardsprojects.lotrcompanions;

import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.richardsprojects.lotrcompanions.npcs.LOTRCNpcs;
import net.richardsprojects.lotrcompanions.eventhandlers.ForgeEntityEvents;
import net.richardsprojects.lotrcompanions.item.LOTRCItems;

import lotr.client.render.entity.GondorSoldierRenderer;

@Mod(LOTRCompanions.MOD_ID)
public class LOTRCompanions {

    public static final String MOD_ID = "lotrcompanions";

    public static final ResourceLocation gondor_soldier = new ResourceLocation(MOD_ID, "gondor_soldier");

    public LOTRCompanions() {
    	// register Listeners that use the Forge Event Bus
        MinecraftForge.EVENT_BUS.register(ForgeEntityEvents.class);

        // register Listeners that use the Mod Event Bus
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::dataGen);
        modBus.addListener(this::setupClientRendering);
        LOTRCItems.ITEMS.register(modBus);
    }

    public void setupClientRendering(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(LOTRCNpcs.HIRED_GONDOR_SOLDIER.get(), GondorSoldierRenderer::new);
    }

    public void dataGen(GatherDataEvent event) {
    	DataGenerator generator = event.getGenerator();
    	generator.addProvider(new CompanionsHiredUnitProfileGenerator(generator, MOD_ID));
    }
}
