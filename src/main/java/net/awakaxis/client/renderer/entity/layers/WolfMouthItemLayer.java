package net.awakaxis.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import net.awakaxis.client.duck.WolfRenderStateDuck;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;

public class WolfMouthItemLayer extends RenderLayer<WolfRenderState, WolfModel> {

    public WolfMouthItemLayer(RenderLayerParent<WolfRenderState, WolfModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i,
            WolfRenderState entityRenderState, float f, float g) {
        WolfRenderStateDuck renderStateDuck = (WolfRenderStateDuck) (Object) entityRenderState;
        boolean bl = renderStateDuck.getMouthItem() == Items.BONE
                || renderStateDuck.getMouthItem().components().getOrDefault(DataComponents.FOOD, null) != null;
        poseStack.pushPose();
        getParentModel().root().getChild("head").translateAndRotate(poseStack);
        getParentModel().root().getChild("head").getChild("real_head").translateAndRotate(poseStack);
        Matrix4f affine = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        poseStack.translate(1f / 16f, 1.85f / 16f, -(5f / 16f));
        if (bl) {
            poseStack.translate(0f, 0f, 1.5f / 16f);
        }

        affine.rotate(Axis.XP.rotationDegrees(90f));
        normal.rotate(Axis.XP.rotationDegrees(90f));

        if (bl) {
            affine.rotate(Axis.ZN.rotationDegrees(-45f));
            normal.rotate(Axis.ZN.rotationDegrees(-45f));
        }

        poseStack.scale(.5f, .5f, .5f);

        renderStateDuck.getMouthItemRenderState().submit(poseStack,
                submitNodeCollector, i, OverlayTexture.NO_OVERLAY,
                entityRenderState.outlineColor);
        poseStack.popPose();
    }

}
