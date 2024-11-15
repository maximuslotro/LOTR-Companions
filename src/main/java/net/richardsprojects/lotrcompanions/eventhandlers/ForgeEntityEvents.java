package net.richardsprojects.lotrcompanions.eventhandlers;

import lotr.common.entity.npc.*;
import lotr.common.entity.npc.data.NPCEntitySettings;
import lotr.common.entity.npc.data.NPCEntitySettingsManager;
import lotr.common.init.ExtendedItems;
import net.minecraft.entity.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.richardsprojects.lotrcompanions.container.CompanionContainer;
import net.richardsprojects.lotrcompanions.container.CompanionEquipmentContainer;
import net.richardsprojects.lotrcompanions.npcs.*;
import net.richardsprojects.lotrcompanions.utils.CoinUtils;
import net.richardsprojects.lotrcompanions.utils.TeleportHelper;

import java.util.*;

/**
 * For {@link net.minecraftforge.eventbus.api.Event} that are fired on the MinecraftForge.EVENT_BUS
 * */
public class ForgeEntityEvents {
    @SubscribeEvent
    public static void giveExperience(final LivingDeathEvent event) {
        Entity companion = event.getSource().getEntity();
        if (companion instanceof HiredGondorSoldier || companion instanceof HiredBreeGuard) {
            // base amount of xp off kill alignment bonus
            int xpPoints;
            NPCEntitySettings settings = NPCEntitySettingsManager.getEntityTypeSettings(event.getEntity());
            if (settings != null && settings.getKillAlignmentBonus() > 0) {
                xpPoints = (int) settings.getKillAlignmentBonus();
            } else {
                xpPoints = 1;
            }

            ((ExtendedHirableEntity) companion).giveExperiencePoints(xpPoints);
            ((ExtendedHirableEntity) companion).setMobKills(((ExtendedHirableEntity) companion).getMobKills() + 1);
        }
    }

    @SubscribeEvent
    public static void playerCloseInventory(final PlayerContainerEvent.Close event) {
        if (!((event.getContainer() instanceof CompanionContainer) ||
                (event.getContainer() instanceof CompanionEquipmentContainer))) {
            return;
        }

        Entity entity;
        if (event.getContainer() instanceof CompanionContainer) {
            entity = event.getPlayer().level.getEntity(((CompanionContainer) event.getContainer()).getEntityId());
        } else {
            entity = event.getPlayer().level.getEntity(((CompanionEquipmentContainer) event.getContainer()).getEntityId());
        }

        // make companion no longer stationary
        if (event.getContainer() instanceof CompanionContainer) {
            if (entity instanceof HiredGondorSoldier) {
                ((HiredGondorSoldier) entity).setInventoryOpen(false);
            } else if (entity instanceof HiredBreeGuard) {
                ((HiredBreeGuard) entity).setInventoryOpen(false);
            }
        } else if (event.getContainer() instanceof CompanionEquipmentContainer) {
            if (entity instanceof HiredGondorSoldier) {
                ((HiredGondorSoldier) entity).setEquipmentOpen(false);
            } else if (entity instanceof HiredBreeGuard) {
                ((HiredBreeGuard) entity).setEquipmentOpen(false);
            }
        }
    }

    @SubscribeEvent
    public static void lotrEntityDeathEvent(final LivingDropsEvent event) {
        if (!(event.getEntity() instanceof NPCEntity)) {
           return;
        }

        if (!(event.getEntity() instanceof ManEntity
            || event.getEntity() instanceof ElfEntity
            || event.getEntity() instanceof DwarfEntity
            || event.getEntity() instanceof OrcEntity)) {
            return;
        }

        // verify coins are in the collection - if not add them
        Set<Item> items = new HashSet<>();
        event.getDrops().forEach(e -> items.add(e.getItem().getItem()));

        if (!items.contains(ExtendedItems.SILVER_COIN_ONE.get())) {
            ItemEntity entity = new ItemEntity(event.getEntity().level, event.getEntity().getX(),
                    event.getEntity().getY(), event.getEntity().getZ(),
                    new ItemStack(ExtendedItems.SILVER_COIN_ONE.get(), new Random().nextInt(2) + 1));
            event.getDrops().add(entity);
        }
    }

    @SubscribeEvent
    public static void hireGondorSoldier(PlayerInteractEvent.EntityInteract event) {
        // TODO: Clean up code between hireGondorSoldier and hireBreelandGuard so that they are one method with less
        //  repeated code

        // only allow this event to run on the server
        if (!(event.getWorld() instanceof ServerWorld)) {
            return;
        }

        if (!(event.getTarget() instanceof GondorSoldierEntity)) {
            return;
        }

        if (event.getTarget() instanceof HiredGondorSoldier) {
            return;
        }

        // check that they have a coin in their hand
        if (!CoinUtils.isValidCoin(event.getItemStack())) {
            return;
        }

        int coins = CoinUtils.totalCoins(event.getPlayer().inventory);
        if (coins < 60) {
            event.getPlayer().sendMessage(new StringTextComponent("I require 60 coins in payment to be hired."), event.getPlayer().getUUID());
            return;
        }

        GondorSoldierEntity gondorSoldier = (GondorSoldierEntity) event.getTarget();
        HiredGondorSoldier newEntity = (HiredGondorSoldier) LOTRCNpcs.HIRED_GONDOR_SOLDIER.get().spawn(
                (ServerWorld) event.getWorld(), null,
                event.getPlayer(), new BlockPos(gondorSoldier.getX(), gondorSoldier.getY(), gondorSoldier.getZ()),
                SpawnReason.NATURAL, false, false
        );
        if (newEntity != null) {
            newEntity.tame(event.getPlayer());
            gondorSoldier.remove();
            CoinUtils.removeCoins(event.getPlayer(), event.getPlayer().inventory, 60);
            event.getPlayer().sendMessage(new StringTextComponent("The Gondor Soldier has been hired for 60 coins"), event.getPlayer().getUUID());
        }
    }

    @SubscribeEvent
    public static void hireBreeGuard(PlayerInteractEvent.EntityInteract event) {
        // only allow this event to run on the server
        if (!(event.getWorld() instanceof ServerWorld)) {
            return;
        }

        if (!(event.getTarget() instanceof BreeGuardEntity)) {
            return;
        }

        if (event.getTarget() instanceof HiredBreeGuard) {
            return;
        }

        // check that they have a coin in their hand
        if (!CoinUtils.isValidCoin(event.getItemStack())) {
            return;
        }

        // TODO: Make prices be based upon faction reputation

        int coins = CoinUtils.totalCoins(event.getPlayer().inventory);
        if (coins < 40) {
            event.getPlayer().sendMessage(new StringTextComponent("I require 40 coins in payment to be hired."), event.getPlayer().getUUID());
            return;
        }

        BreeGuardEntity breeGuard = (BreeGuardEntity) event.getTarget();
        HiredBreeGuard newEntity = (HiredBreeGuard) LOTRCNpcs.HIRED_BREE_GUARD.get().spawn(
                (ServerWorld) event.getWorld(), null,
                event.getPlayer(), new BlockPos(breeGuard.getX(), breeGuard.getY(), breeGuard.getZ()),
                SpawnReason.NATURAL, false, false
        );

        // TODO: Update gear to match correctly

        if (newEntity != null) {
            newEntity.tame(event.getPlayer());
            breeGuard.remove();
            CoinUtils.removeCoins(event.getPlayer(), event.getPlayer().inventory, 40);
            event.getPlayer().sendMessage(new StringTextComponent("The Bree-land Guard has been hired for 40 coins"), event.getPlayer().getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof PlayerEntity)) {
            return;
        }

        if (!(event.getEntity().level instanceof ServerWorld)) {
            return;
        }

        ServerWorld world = (ServerWorld) event.getEntity().level;
        BlockPos originalPos = new BlockPos(event.getPrevX(), event.getPrevY(), event.getPrevZ());
        BlockPos targetPos = new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        TeleportHelper.teleportUnitsToPlayer(originalPos, targetPos, world, (PlayerEntity) event.getEntity());
    }

    @SubscribeEvent
    public static void preventFriendlyFireFromPlayerToCompanion(LivingDamageEvent event) {
        if (!HiredUnitHelper.isEntityHiredUnit(event.getEntity())) {
            return;
        }

        UUID owner = HiredUnitHelper.getExtendedHirableEntity(event.getEntity()).getOwnerUUID();

        if (event.getSource() != null && event.getSource().getEntity() != null
                && event.getSource().getEntity() instanceof PlayerEntity) {
            // cancel a damage event if damage comes from owner
            PlayerEntity player = (PlayerEntity) event.getSource().getEntity();
            if (owner.equals(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }
}
