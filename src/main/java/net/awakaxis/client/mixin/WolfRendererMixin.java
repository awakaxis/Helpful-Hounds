package net.awakaxis.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.awakaxis.client.duck.WolfRenderStateDuck;
import net.awakaxis.client.renderer.entity.layers.WolfMouthItemLayer;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Mixin(WolfRenderer.class)
public abstract class WolfRendererMixin extends LivingEntityRenderer<Wolf, WolfRenderState, WolfModel> {

    @Unique
    private ItemModelResolver RESOLVER;

    protected WolfRendererMixin(Context context, WolfModel entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "<init>", at = @At("TAIl"))
    private void onInit(EntityRendererProvider.Context context, CallbackInfo ci) {
        this.RESOLVER = context.getItemModelResolver();
        addLayer(new WolfMouthItemLayer(this));
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void addMouthItemState(Wolf wolf, WolfRenderState wolfRenderState,
            float f, CallbackInfo ci) {
        WolfRenderStateDuck renderStateDuck = (WolfRenderStateDuck) (Object) wolfRenderState;
        ItemStack mouthItem = wolf.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
        this.RESOLVER.updateForTopItem(itemStackRenderState, mouthItem,
                ItemDisplayContext.FIXED,
                wolf.level(), null, 0);

        renderStateDuck.setMouthItemRenderState(itemStackRenderState);
        renderStateDuck.setMouthItem(mouthItem.getItem());
    }

}
