package net.richardsprojects.lotrcompanions;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.richardsprojects.lotrcompanions.client.render.HiredGondorSoldierRenderer;
import net.richardsprojects.lotrcompanions.npcs.LOTRCNpcs;
import net.richardsprojects.lotrcompanions.eventhandlers.ForgeEntityEvents;
import net.richardsprojects.lotrcompanions.item.LOTRCItems;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@Mod(LOTRCompanions.MOD_ID)
public class LOTRCompanions {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "lotrcompanions";

    public static UUID usersUUID = null;

    public static IEventBus eventBus;
    public LOTRCompanions() {
    	// register Listeners that use the Forge Event Bus
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ForgeEntityEvents.class);

        // register Listeners that use the Mod Event Bus
        eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        LOTRCItems.ITEMS.register(eventBus);
        eventBus.register(this);
    }

    @SubscribeEvent
    public void setupClientRendering(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(LOTRCNpcs.HIRED_GONDOR_SOLDIER.get(), HiredGondorSoldierRenderer::new);
    }

}
