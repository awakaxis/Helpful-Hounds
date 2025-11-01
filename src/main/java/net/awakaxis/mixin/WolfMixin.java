package net.awakaxis.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.awakaxis.world.entity.ai.goal.CollectDroppedItemGoal;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Wolf.class)
public abstract class WolfMixin {

    @Unique
    private void helpfulhounds$dropItem(ItemStack dropItem, boolean clearMouth, boolean sound) {
        Wolf self = (Wolf) (Object) this;
        if (!dropItem.isEmpty()) {
            if (clearMouth) {
                self.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            ItemEntity dropped = new ItemEntity(self.level(), self.getX() + self.getLookAngle().x,
                    self.getY() + 1, self.getZ() + self.getLookAngle().z, dropItem);
            dropped.setDefaultPickUpDelay();
            dropped.setThrower(self);
            if (sound) {
                self.level().playSound(null, self.blockPosition(), SoundEvents.FOX_SPIT, SoundSource.NEUTRAL, 0.55f,
                        0.15f);
            }
            self.level().addFreshEntity(dropped);
        }
    }

    @Unique
    private void helpfulhounds$tryTakeItem(Player player) {
        Wolf self = (Wolf) (Object) this;
        if (self.getOwner() == null) {
            if (((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() < 0.8f) {
                self.level().playSound(null, self.blockPosition(),
                        ((WolfAccessor) self).helpfulhounds$getSoundVariant().value().growlSound().value(),
                        SoundSource.NEUTRAL);
                if (((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() < 0.33f) {
                    self.setPersistentAngerTarget(player.getUUID());
                    self.startPersistentAngerTimer();
                }
                return;
            }
        } else if (self.getOwner().equals(player)
                && ((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() < .03) {
            self.level().playSound(null, self.blockPosition(),
                    ((WolfAccessor) self).helpfulhounds$getSoundVariant().value().growlSound().value(),
                    SoundSource.NEUTRAL);
            // tamed wolfs have a 50/50 chance to run away when refusing to give their item
            if (((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() < 0.5) {
                Vec3 targetPos = DefaultRandomPos.getPosAway(self, 8, 7, self.getOwner().position());
                self.getNavigation()
                        .moveTo(self.getNavigation().createPath(targetPos.x(), targetPos.y(), targetPos.z(), 0), 1.25);
            }
            return;
        } else if (((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() < 0.45f) {
            self.level().playSound(null, self.blockPosition(),
                    ((WolfAccessor) self).helpfulhounds$getSoundVariant().value().growlSound().value(),
                    SoundSource.NEUTRAL);
            if (((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() < 0.8) {
                Vec3 targetPos = DefaultRandomPos.getPosAway(self, 8, 7, self.getOwner().position());
                self.getNavigation()
                        .moveTo(self.getNavigation().createPath(targetPos.x(), targetPos.y(), targetPos.z(), 0), 1.25);
            }
            return;
        }
        helpfulhounds$dropItem(self.getItemBySlot(EquipmentSlot.MAINHAND), true, true);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void helpfulhounds$tick(CallbackInfo ci) {
        Wolf self = (Wolf) (Object) this;

        if (self.level() instanceof ServerLevel serverLevel) {

            ItemStack mouthItem = self.getItemBySlot(EquipmentSlot.MAINHAND);

            // eat held food for healing
            if (self.isFood(mouthItem) && self.getHealth() < self.getMaxHealth()
                    && ((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() < 0.05f) {
                UseRemainder useRemainder = mouthItem.get(DataComponents.USE_REMAINDER);

                int i = mouthItem.getCount();
                mouthItem.consume(1, self);
                if (useRemainder != null) {
                    mouthItem = useRemainder.convertIntoRemainder(mouthItem, i, self.hasInfiniteMaterials(),
                            (itemStack) -> {
                                helpfulhounds$dropItem(itemStack, false, false);
                            });
                }

                FoodProperties foodProperties = (FoodProperties) mouthItem.get(DataComponents.FOOD);
                float f = foodProperties != null ? (float) foodProperties.nutrition() : 1.0F;
                self.heal(2.0F * f);
                self.level().playSound(null, self.blockPosition(), SoundEvents.GENERIC_EAT.value(),
                        SoundSource.NEUTRAL);
                self.setItemSlot(EquipmentSlot.MAINHAND, mouthItem);
            }

            // wild wolfs drop non food items after a bit
            if (self.getOwner() == null && !self.isFood(mouthItem)
                    && ((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() < 0.0005f) {
                helpfulhounds$dropItem(mouthItem, true, true);
                mouthItem = ItemStack.EMPTY;
            }

            AABB box = self.getBoundingBox().inflate(.5);

            List<Entity> entities = serverLevel.getEntities(self, box);

            for (Entity entity : entities) {
                if (entity instanceof ItemEntity itemEntity) {
                    if (itemEntity.hasPickUpDelay() || itemEntity.isRemoved() || itemEntity.getOwner() == self) {
                        continue;
                    }
                    if (!mouthItem.isEmpty()) {
                        // wild wolfs drop any non food item for food items
                        if (self.getOwner() == null && !self.isFood(mouthItem) && self.isFood(itemEntity.getItem())) {
                            helpfulhounds$dropItem(mouthItem, true, true);
                        }
                        // tamed wolfs drop any item for their owner's dropped item
                        if (self.getOwner() != null && itemEntity.getOwner() == self.getOwner()) {
                            helpfulhounds$dropItem(mouthItem, true, true);
                        }
                        continue;
                    }

                    ItemStack itemStack = itemEntity.getItem().split(1);

                    self.onItemPickup(itemEntity);
                    self.setGuaranteedDrop(EquipmentSlot.MAINHAND);
                    self.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
                    serverLevel.playSound(null, self.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL);
                    itemEntity.setThrower(null);
                    if (itemEntity.getItem().isEmpty()) {
                        itemEntity.discard();
                    }
                    break;
                }
            }
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void helpfulhounds$mobInteract(Player player, InteractionHand interactionHand,
            CallbackInfoReturnable<InteractionResult> cir) {
        Wolf self = (Wolf) (Object) this;
        if (player.isCrouching() && !self.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()
                && player.getItemInHand(interactionHand).isEmpty()) {
            if (!player.level().isClientSide()) {
                helpfulhounds$tryTakeItem(player);
            }
            cir.setReturnValue(InteractionResult.SUCCESS.withoutItem());
        }
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void helpfulhounds$registerGoals(CallbackInfo ci) {
        Wolf self = (Wolf) (Object) this;
        GoalSelector goalSelector = ((MobAccessor) self).helpfulhounds$getGoalSelector();

        goalSelector.addGoal(5, new CollectDroppedItemGoal(self));
    }
}
