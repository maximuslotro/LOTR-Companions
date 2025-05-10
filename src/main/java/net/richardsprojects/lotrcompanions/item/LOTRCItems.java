package net.richardsprojects.lotrcompanions.item;

import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.richardsprojects.lotrcompanions.LOTRCompanions;

public class LOTRCItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LOTRCompanions.MOD_ID);

    public static final RegistryObject<ForgeSpawnEggItem> HIRED_GONDOR_SOLDIER_EGG = ITEMS.register("hired_gondor_soldier_spawn_egg", HiredGondorSoldierEggItem::new);
}
