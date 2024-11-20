package net.richardsprojects.lotrcompanions.npcs.ai;

import lotr.common.entity.npc.ExtendedHirableEntity;
import lotr.common.entity.npc.NPCEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class EatGoal extends Goal {
    protected final NPCEntity entity;

    protected final ExtendedHirableEntity companion;
    ItemStack food = ItemStack.EMPTY;
    boolean started = false;
    int timeLeft = -1;

    public EatGoal(NPCEntity entity, ExtendedHirableEntity unit) {
        this.entity = entity;
        companion = unit;
    }

    public boolean canUse() {
        if (entity.getHealth() < entity.getMaxHealth() && !companion.isInventoryOpen() && !entity.getNPCCombatUpdater().isCombatStance()) {
            food = companion.checkFood();
            return !food.isEmpty();
        }
        return false;
    }

    public void start() {
        started = true;
        timeLeft = food.getUseDuration() + 1;
        entity.setItemInHand(Hand.OFF_HAND, food);
        entity.startUsingItem(Hand.OFF_HAND);
    }

    public void stop() {
        entity.setItemSlot(EquipmentSlotType.OFFHAND, ItemStack.EMPTY);
        started = false;
        timeLeft = -1;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    public void tick () {
        if (started && timeLeft > -1) {
            --timeLeft;
            if (timeLeft == 0) {
                stop();
            }
        } else {
            if (entity.getHealth() < entity.getMaxHealth()) {
                food = companion.checkFood();
                if (!food.isEmpty()) {
                    if (!started) start();
                }
            }
        }
    }
}