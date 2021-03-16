package me.ichun.mods.hats.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelHatLauncher<T extends Entity> extends EntityModel<T> {
    public ModelRenderer grip;
    public ModelRenderer barrel;
    public ModelRenderer gasR;
    public ModelRenderer gasL;
    public ModelRenderer glass1;
    public ModelRenderer glass2;
    public ModelRenderer glass3;
    public ModelRenderer glass4;
    public ModelRenderer headR;
    public ModelRenderer headL;

    public ModelHatLauncher() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.headL = new ModelRenderer(64, 64, 0, 0);
        this.headL.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.headL.addBox(-4.0F, -6.75F, -3.0F, 8.0F, 8.0F, 8.0F, -3.25F, -3.25F, -3.25F);
        this.setRotateAngle(headL, 0.0F, 3.141592653589793F, -1.5707963267948966F);
        this.gasL = new ModelRenderer(this, 30, 0);
        this.gasL.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.gasL.addBox(-6.0F, -1.0F, -3.2F, 4.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
        this.setRotateAngle(gasL, 0.0F, 0.0F, 1.5707963267948966F);
        this.glass2 = new ModelRenderer(this, 32, 0);
        this.glass2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.glass2.addBox(-2.0F, -2.75F, -14.0F, 4.0F, 1.0F, 10.0F, 0.0F, -0.25F, 0.0F);
        this.grip = new ModelRenderer(this, 50, 0);
        this.grip.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.grip.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, 0.0F, 0.0F, 0.0F);
        this.gasR = new ModelRenderer(this, 30, 0);
        this.gasR.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.gasR.addBox(-6.0F, -1.0F, -3.2F, 4.0F, 2.0F, 2.0F, 0.0F, 0.0F, 0.0F);
        this.glass3 = new ModelRenderer(this, 32, 0);
        this.glass3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.glass3.addBox(-2.0F, -2.75F, -14.0F, 4.0F, 1.0F, 10.0F, 0.0F, -0.25F, 0.0F);
        this.setRotateAngle(glass3, 0.0F, 0.0F, -1.5707963267948966F);
        this.glass4 = new ModelRenderer(this, 32, 0);
        this.glass4.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.glass4.addBox(-2.0F, -2.75F, -14.0F, 4.0F, 1.0F, 10.0F, 0.0F, -0.25F, 0.0F);
        this.setRotateAngle(glass4, 0.0F, 0.0F, 1.5707963267948966F);
        this.barrel = new ModelRenderer(this, 32, 11);
        this.barrel.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.barrel.addBox(-2.0F, -2.0F, -4.5F, 4.0F, 4.0F, 7.0F, 0.0F, 0.0F, 0.0F);
        this.setRotateAngle(barrel, 0.0F, 0.0F, 0.7853981633974483F);
        this.glass1 = new ModelRenderer(this, 32, 0);
        this.glass1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.glass1.addBox(-2.0F, 1.75F, -14.0F, 4.0F, 1.0F, 10.0F, 0.0F, -0.25F, 0.0F);
        this.headR = new ModelRenderer(64, 64, 0, 0);
        this.headR.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.headR.addBox(-4.0F, -6.75F, -3.0F, 8.0F, 8.0F, 8.0F, -3.25F, -3.25F, -3.25F);
        this.setRotateAngle(headR, 0.0F, 3.141592653589793F, 0.0F);
        this.barrel.addChild(this.headL);
        this.barrel.addChild(this.gasL);
        this.barrel.addChild(this.glass2);
        this.barrel.addChild(this.gasR);
        this.barrel.addChild(this.glass3);
        this.barrel.addChild(this.glass4);
        this.barrel.addChild(this.glass1);
        this.barrel.addChild(this.headR);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        ImmutableList.of(this.grip, this.barrel).forEach((modelRenderer) -> {
            modelRenderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        });
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
