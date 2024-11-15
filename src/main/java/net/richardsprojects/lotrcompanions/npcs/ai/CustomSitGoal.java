package net.richardsprojects.lotrcompanions.npcs.ai;

import java.util.EnumSet;

import lotr.common.entity.npc.ExtendedHirableEntity;
import lotr.common.entity.npc.NPCEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.richardsprojects.lotrcompanions.npcs.HiredBreeGuard;
import net.richardsprojects.lotrcompanions.npcs.HiredGondorSoldier;

public class CustomSitGoal extends Goal {
    private final NPCEntity entity;
    private final ExtendedHirableEntity unit;

    public CustomSitGoal(NPCEntity entity, ExtendedHirableEntity unit) {
        this.entity = entity;
        this.unit = unit;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    public boolean canContinueToUse() {
        return this.unit.isStationary() || this.unit.isInventoryOpen();
    }

    public boolean canUse() {
        if (!this.unit.isTame()) {
            return false;
        } else if (this.entity.isInWaterOrBubble()) {
            return false;
        } else if (!this.entity.isOnGround()) {
            return false;
        } else {
            LivingEntity livingentity = this.unit.getOwner();
            if (livingentity == null) {
                return true;
            } else {
                // TODO: Temp hack until isEquipmentOpen is added to ExtendedHirableEntity in a new update
                boolean equipmentOpen = false;
                if (entity instanceof HiredGondorSoldier) {
                    equipmentOpen = ((HiredGondorSoldier) entity).isEquipmentOpen();
                } else if (entity instanceof HiredBreeGuard) {
                    equipmentOpen = ((HiredBreeGuard) entity).isEquipmentOpen();
                }

                return (!(this.entity.distanceToSqr(livingentity) < 144.0D) || livingentity.getLastHurtByMob() == null) && (this.unit.isStationary() || this.unit.isInventoryOpen() || equipmentOpen);
            }
        }
    }

    public void start() {
        this.entity.getNavigation().stop();
    }

    public void stop() {

    }
}
