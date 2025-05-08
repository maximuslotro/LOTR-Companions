package net.richardsprojects.lotrcompanions.eventhandlers;

import lotr.common.entity.npc.*;
import lotr.common.util.CoinUtils;
import net.minecraft.entity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.richardsprojects.lotrcompanions.npcs.*;

/**
 * For {@link net.minecraftforge.eventbus.api.Event} that are fired on the MinecraftForge.EVENT_BUS
 * */
public class ForgeEntityEvents {

    @SubscribeEvent
    public static void hireGondorSoldier(PlayerInteractEvent.EntityInteract event) {
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

        int coins = CoinUtils.totalValueInPlayerInventory(event.getPlayer().inventory);
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
}
