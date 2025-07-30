package net.richardsprojects.lotrcompanions;

import java.util.function.Consumer;

import com.github.maximuslotro.lotrrextended.common.datagen.entity.hiredunitprofiles.ExtendedServerHiredUnitProfileGenerator;
import com.github.maximuslotro.lotrrextended.common.enums.ExtendedUnitAttackType;
import com.github.maximuslotro.lotrrextended.common.hiredunits.ExtendedServerHiredUnitProfile;

import lotr.common.init.LOTREntities;
import lotr.common.init.LOTRItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.ItemStack;

public class CompanionsHiredUnitProfileGenerator extends ExtendedServerHiredUnitProfileGenerator{

	public CompanionsHiredUnitProfileGenerator(DataGenerator pDataGenerator, String pModId) {
		super(pDataGenerator, "lotrextended");
	}

	@Override
	public void makeStructureSpawnPools(Consumer<ExtendedServerHiredUnitProfile> pConsumer) {
		this.make(LOTREntities.GONDOR_SOLDIER.get(), ExtendedUnitAttackType.MELEE)
		.setPurchaseInfo(60, false, 60)
		.addHelmetPoolItem(new ItemStack(LOTRItems.GONDOR_HELMET.get()), 1)
		.addChestplatePoolItem(new ItemStack(LOTRItems.GONDOR_CHESTPLATE.get()), 1)
		.addLeggingPoolItem(new ItemStack(LOTRItems.GONDOR_LEGGINGS.get()), 1)
		.addBootsPoolItem(new ItemStack(LOTRItems.GONDOR_BOOTS.get()), 1)
		.addMeleePoolItem(new ItemStack(LOTRItems.GONDOR_SWORD.get()), 1)
		.addShieldItem(new ItemStack(LOTRItems.GONDOR_SHIELD.get()), 1)
		.setIdlePoolFromMeleePool().save(pConsumer, LOTRCompanions.gondor_soldier);
	}
}
