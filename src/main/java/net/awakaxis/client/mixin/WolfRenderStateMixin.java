package net.awakaxis.client.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.awakaxis.client.duck.WolfRenderStateDuck;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.Item;

@Mixin(WolfRenderState.class)
public abstract class WolfRenderStateMixin implements WolfRenderStateDuck {

    private ItemStackRenderState helpfulhounds$mouthItem;
    private Item helpfulhounds$mouthItemName;

    @Override
    public ItemStackRenderState getMouthItemRenderState() {
        return helpfulhounds$mouthItem;
    }

    @Override
    public Item getMouthItem() {
        return helpfulhounds$mouthItemName;
    }

    @Override
    public void setMouthItemRenderState(ItemStackRenderState itemStackRenderState) {
        helpfulhounds$mouthItem = itemStackRenderState;
    }

    @Override
    public void setMouthItem(Item item) {
        helpfulhounds$mouthItemName = item;
    }

}
