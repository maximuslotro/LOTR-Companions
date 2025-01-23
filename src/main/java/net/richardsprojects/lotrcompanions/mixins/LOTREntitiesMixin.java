package net.richardsprojects.lotrcompanions.mixins;

import lotr.common.entity.npc.NPCEntity;
import lotr.common.init.LOTREntities;
import lotr.common.init.LOTREntityClassifications;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.fml.RegistryObject;
import net.richardsprojects.lotrcompanions.npcs.HiredGondorSoldier;
import net.richardsprojects.lotrcompanions.npcs.LOTRCNpcs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(LOTREntities.class)
public class LOTREntitiesMixin {

    @Shadow
    private static final Map<EntityType<? extends LivingEntity>, Supplier<AttributeModifierMap.MutableAttribute>> ENTITY_ATTRIBUTE_FACTORIES = new HashMap<>();

    @Shadow(remap = false)
    private static <T extends Entity> RegistryObject<EntityType<T>> regEntity(String key, EntityType.IFactory<T> factory, EntityClassification classif, float width, float height, Consumer<EntityType.Builder<T>> extraProps, Consumer<EntityType<T>> builtTypeConsumer) {
        return null;
    }

    @Inject(method = "register()V", at = @At("HEAD"), remap=false)
    private static void bootstrapExtendedItems(CallbackInfo ci) {
        // for these specific npcs we have custom Spawn Eggs that we create for spawning them already assigned to the
        // person who spawned them so we can't use the existing implementation from Renewed
        LOTRCNpcs.HIRED_GONDOR_SOLDIER = regNPCWithoutSpawnEgg("hired_gondor_soldier", HiredGondorSoldier::new, HiredGondorSoldier::regAttrs, LOTREntities.EntitySizeHolder.manSize());
    }

    private static <T extends NPCEntity> RegistryObject<EntityType<T>> regNPCWithoutSpawnEgg(String key, EntityType.IFactory<T> factory, Supplier<AttributeModifierMap.MutableAttribute> attribFactory, LOTREntities.EntitySizeHolder size) {
        return regLivingWithoutSpawnEgg(key, factory, attribFactory, LOTREntityClassifications.NPC, size, 10, 3, true, EntitySpawnPlacementRegistry.PlacementType.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::checkMobSpawnRules);
    }

    private static <T extends MobEntity> RegistryObject<EntityType<T>> regLivingWithoutSpawnEgg(String key, EntityType.IFactory<T> factory, Supplier<AttributeModifierMap.MutableAttribute> attribFactory, EntityClassification classif, LOTREntities.EntitySizeHolder size, int trackRange, int updateFreq, boolean velUpdates, EntitySpawnPlacementRegistry.PlacementType placementType, Heightmap.Type heightmapType, EntitySpawnPlacementRegistry.IPlacementPredicate<T> placementPredicate) {
        return regEntity(key, factory, classif, size.width, size.height, (builder) -> {
            builder.setTrackingRange(trackRange);
            builder.setUpdateInterval(updateFreq);
            builder.setShouldReceiveVelocityUpdates(velUpdates);
        }, builtType -> {
            ENTITY_ATTRIBUTE_FACTORIES.put(builtType, attribFactory);
            EntitySpawnPlacementRegistry.register(builtType, placementType, heightmapType, placementPredicate);
        });
    }
}
