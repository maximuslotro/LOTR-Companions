package net.richardsprojects.lotrcompanions.container;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.richardsprojects.lotrcompanions.LOTRCompanions;

public class CompanionsContainers {

    public static final DeferredRegister<ContainerType<?>> CONTAINERS;
    public static final RegistryObject<ContainerType<CompanionEquipmentContainer>> COMPANION_EQUIPMENT_CONTAINER;

    public CompanionsContainers() {

    }

    public static void register() {
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    static {
        CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, LOTRCompanions.MOD_ID);
        COMPANION_EQUIPMENT_CONTAINER = CONTAINERS.register("companion_equipment_container", () -> IForgeContainerType.create(CompanionEquipmentContainer::new));
    }
}

