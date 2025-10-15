package net.awakaxis.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

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
                && ((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() < 0.04f) {
            self.level().playSound(null, self.blockPosition(),
                    ((WolfAccessor) self).helpfulhounds$getSoundVariant().value().growlSound().value(),
                    SoundSource.NEUTRAL);
            return;
        } else if (((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() < 0.45f) {
            self.level().playSound(null, self.blockPosition(),
                    ((WolfAccessor) self).helpfulhounds$getSoundVariant().value().growlSound().value(),
                    SoundSource.NEUTRAL);
            return;
        }
        helpfulhounds$dropItem(self.getItemBySlot(EquipmentSlot.MAINHAND), true, true);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void helpfulhounds$tick(CallbackInfo ci) {
        Wolf self = (Wolf) (Object) this;

        if (self.level() instanceof ServerLevel serverLevel) {
            AABB box = self.getBoundingBox().inflate(.5);

            List<Entity> entities = serverLevel.getEntities(self, box);

            // pick up any item on the ground nearby if MAINHAND empty or if dropped by
            // owner
            for (Entity entity : entities) {
                if (entity instanceof ItemEntity itemEntity && itemEntity.getOwner() != self
                        && !itemEntity.hasPickUpDelay() && !entity.isRemoved()
                        && (self.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()
                                || itemEntity.getOwner() == self.getOwner())) {
                    if (self.hasItemInSlot(EquipmentSlot.MAINHAND)) {
                        helpfulhounds$dropItem(self.getItemBySlot(EquipmentSlot.MAINHAND), true, true);
                    }

                    self.onItemPickup(itemEntity);
                    self.setGuaranteedDrop(EquipmentSlot.MAINHAND);
                    self.setItemSlot(EquipmentSlot.MAINHAND, itemEntity.getItem());
                    serverLevel.playSound(null, self.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL);
                    entity.discard();
                    return;
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
}
