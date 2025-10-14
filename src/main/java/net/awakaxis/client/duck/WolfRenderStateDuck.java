package net.awakaxis.client.duck;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.Item;

public interface WolfRenderStateDuck {
    ItemStackRenderState getMouthItemRenderState();

    Item getMouthItem();

    void setMouthItemRenderState(ItemStackRenderState itemStackRenderState);

    void setMouthItem(Item resourceLocation);
}
