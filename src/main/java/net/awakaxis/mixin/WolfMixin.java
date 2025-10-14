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
    private void helpfulhounds$dropItem() {
        Wolf self = (Wolf) (Object) this;
        ItemStack itemStack = self.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!itemStack.isEmpty()) {
            self.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            ItemEntity dropped = new ItemEntity(self.level(), self.getX() + self.getLookAngle().x,
                    self.getY() + 1, self.getZ() + self.getLookAngle().z, itemStack);
            dropped.setDefaultPickUpDelay();
            dropped.setThrower(self);
            self.playSound(SoundEvents.FOX_SPIT, .65f, .15f);
            self.level().addFreshEntity(dropped);
        }
    }

    private void helpfulhounds$tryTakeItem(Player player) {
        Wolf self = (Wolf) (Object) this;
        if (self.getOwner() != null && self.getOwner().equals(player)) {
            if (((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() >= 0.1f) {
                helpfulhounds$dropItem();
                return;
            }
        }
        if (((EntityAccessor) self).helpfulhounds$getRandom().nextFloat() >= 0.8f) {
            helpfulhounds$dropItem();
            return;
        }
        self.playSound(((WolfAccessor) self).helpfulhounds$getSoundVariant().value().growlSound().value());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void helpfulhounds$tick(CallbackInfo ci) {
        Wolf self = (Wolf) (Object) this;

        if (self.level() instanceof ServerLevel serverLevel) {
            AABB box = self.getBoundingBox().inflate(.5);

            List<Entity> entities = serverLevel.getEntities(self, box);

            for (Entity entity : entities) {
                if (entity instanceof ItemEntity itemEntity && itemEntity.getOwner() != self
                        && !itemEntity.hasPickUpDelay() && !entity.isRemoved()) {
                    helpfulhounds$dropItem();

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
