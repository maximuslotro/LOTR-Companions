package net.richardsprojects.lotrcompanions;

import com.github.maximuslotro.lotrrextended.ExtendedLog;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.richardsprojects.lotrcompanions.client.ContainerScreenHelper;
import net.richardsprojects.lotrcompanions.client.eventhandlers.RenderHealthbars;
import net.richardsprojects.lotrcompanions.client.render.HiredBreeGuardRenderer;
import net.richardsprojects.lotrcompanions.client.render.HiredGondorSoldierRenderer;
import net.richardsprojects.lotrcompanions.container.CompanionsContainers;
import net.richardsprojects.lotrcompanions.core.PacketHandler;
import net.richardsprojects.lotrcompanions.eventhandlers.LOTRFastTravelEventHandler;
import net.richardsprojects.lotrcompanions.npcs.LOTRCNpcs;
import net.richardsprojects.lotrcompanions.eventhandlers.ForgeEntityEvents;
import net.richardsprojects.lotrcompanions.item.LOTRCItems;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@Mod(LOTRCompanions.MOD_ID)
public class LOTRCompanions {

    // configurable values
    public static final boolean LOW_HEALTH_FOOD = true;
    public static final boolean FRIENDLY_FIRE_PLAYER = true;
    public static final boolean FALL_DAMAGE = true;
    public static final int BASE_HEALTH = 30;

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "lotrcompanions";

    public static final boolean FRIENDLY_FIRE_COMPANIONS = false;

    public static UUID usersUUID = null;

    // TODO: Prevent hired units from targeting players
    public static final boolean HIRED_UNITS_PARITICIPATE_PVP = false;

    public static IEventBus eventBus;
    public LOTRCompanions() {
    	// register Listeners that use the Forge Event Bus
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ForgeEntityEvents.class);
        MinecraftForge.EVENT_BUS.register(LOTRFastTravelEventHandler.class);

        // register Listeners that use the Mod Event Bus
        eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        LOTRCItems.ITEMS.register(eventBus);
        eventBus.register(this);

        CompanionsContainers.register();
        eventBus.addListener(this::registerScreens);

        // register client event handlers only on clients
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> this::setupClient);

        PacketHandler.register();
    }

    private void registerScreens(final FMLClientSetupEvent event) {
        ContainerScreenHelper.registerScreens();
    }

    private void setupClient() {
        MinecraftForge.EVENT_BUS.register(RenderHealthbars.class);

        String userUUIDString = Minecraft.getInstance().getUser().getUuid();
        if (!userUUIDString.equals("")) userUUIDString = LOTRCompanions.addDashesToUUID(userUUIDString);
        try {
            usersUUID = UUID.fromString(userUUIDString);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    private static String addDashesToUUID(String uuid) {
        return uuid.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        );
    }

    @SubscribeEvent
    public void setupClientRendering(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(LOTRCNpcs.HIRED_GONDOR_SOLDIER.get(), HiredGondorSoldierRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(LOTRCNpcs.HIRED_BREE_GUARD.get(), HiredBreeGuardRenderer::new);
    }

}
