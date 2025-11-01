package net.awakaxis.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

public class CollectDroppedItemGoal extends Goal {
    private final TamableAnimal self;
    private ItemEntity target;

    public CollectDroppedItemGoal(TamableAnimal tamableAnimal) {
        this.self = tamableAnimal;
        setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = self.getOwner();
        List<ItemEntity> itemEntities = self.level()
                .getEntitiesOfClass(ItemEntity.class, self.getBoundingBox().inflate(10));

        if (owner == null && self.isFood(self.getItemBySlot(EquipmentSlot.MAINHAND))) {
            return false;
        }
        for (ItemEntity itemEntity : itemEntities) {
            if (itemEntity.getDeltaMovement().length() > .15) {
                return false;
            }

            if (itemEntity.getOwner() == self) {
                return false;
            }

            if (self.isFood(itemEntity.getItem())) {
                this.target = itemEntity;
                return true;
            }

            if ((owner != null && itemEntity.getOwner() == owner) && itemEntity.distanceTo(self) < 1.5) {
                this.target = itemEntity;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return !self.getNavigation().isDone();
    }

    @Override
    public void start() {
        self.getNavigation().stop();
        self.getNavigation()
                .moveTo(self.getNavigation().createPath(this.target.position().x(), this.target.position().y(),
                        this.target.position().z(), 0),
                        1.0);
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }
}
