package net.richardsprojects.lotrcompanions.entity.ai;

import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.richardsprojects.lotrcompanions.entity.AbstractHiredLOTREntity;

public class CustomWaterAvoidingRandomWalkingGoal extends WaterAvoidingRandomWalkingGoal {
    AbstractHiredLOTREntity companion;

    public CustomWaterAvoidingRandomWalkingGoal(AbstractHiredLOTREntity p_25987_, double p_25988_) {
        super(p_25987_, p_25988_);
        this.companion = p_25987_;
    }

    public boolean canUse() {
        if (!companion.isFollowing()) {
            return false;
        }
        return super.canUse();
    }
}
