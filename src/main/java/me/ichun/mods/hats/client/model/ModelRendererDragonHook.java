package me.ichun.mods.hats.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.head.HeadInfo;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.util.ResourceLocation;

public class ModelRendererDragonHook extends ModelRenderer
{
    private static final RenderType RENDER_TYPE_RESET = RenderType.getEyes(new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png"));

    public EnderDragonRenderer.EnderDragonModel parentModel;
    public int renderCount;
    public float lastPartialTick;

    public ModelRendererDragonHook(EnderDragonRenderer.EnderDragonModel model)
    {
        super(model);

        parentModel = model;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(MatrixStack stack, IVertexBuilder bufferInUnused, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        if(parentModel.dragonInstance == null)
        {
            return;
        }

        if(lastPartialTick != parentModel.partialTicks) // new render
        {
            lastPartialTick = parentModel.partialTicks;
            renderCount = 0;
        }
        renderCount++;

        boolean render = renderCount == 3;
        if(renderCount == 2 && !(parentModel.dragonInstance.deathTicks > 0))
        {
            render = true;
        }

        if(!render)
        {
            return;
        }

        HeadInfo helper = HeadHandler.getHelper(parentModel.dragonInstance.getClass());
        if(helper == null || helper.noTopInfo)
        {
            return;
        }

        helper.headModel = new ModelRenderer[] { parentModel.head };

        LivingEntity living = parentModel.dragonInstance;

        int headCount = helper.getHeadCount(living);

        for(int i = 0; i < headCount; i++)
        {
            float hatScale = helper.getHatScale(living, stack, lastPartialTick, i);

            if(hatScale <= 0F)
            {
                continue;
            }

            stack.push();

            ModelRenderer head = helper.headModel[0];
            if(!head.cubeList.isEmpty())
            {
                ModelRenderer.ModelBox box = head.cubeList.get(1);
                float dX = Math.abs(box.posX2 - box.posX1);
                float dZ = Math.abs(box.posZ2 - box.posZ1);
                float offX = ((dX / 2F) + box.posX1) / 16F;
                float offY = box.posY1 / 16F; //just the top as posY1 is offset down from rotation point.
                float offZ = ((dZ / 2F) + box.posZ1) / 16F;

                float hori = Math.max(dX, dZ);
                float scale = hori / 8F;

                stack.translate(offX, offY, offZ);

                stack.scale(scale, scale, scale);

                int overlay = LivingRenderer.getPackedOverlay(living, 0.0F);
                Project project = HatResourceHandler.HATS.get(Screen.hasControlDown() ? "Headphones" : "Skyrim Hat");
                if(project != null)
                {
                    project.getModel().render(stack, null, packedLightIn, overlay, 1F, 1F, 1F, 1F);
                }
            }

            //                float[] headPoint = helper.getHatOffset(living, stack, lastPartialTick, i);
            //                stack.translate(-headPoint[0], -headPoint[1], -headPoint[2]);
            //
            //                stack.rotate(Vector3f.YP.rotationDegrees(helper.getHatYaw(living, stack, lastPartialTick, i)));
            //                stack.rotate(Vector3f.XP.rotationDegrees(helper.getHatPitch(living, stack, lastPartialTick, i)));
            //
            //                stack.scale(hatScale, hatScale, hatScale);
            //
            //                //                int overlay = LivingRenderer.getPackedOverlay(living, 0.0F);
            //                //                ArrayList<String> allKeys = new ArrayList<>(HatResourceHandler.HATS.keySet());
            //                //                Project project = HatResourceHandler.HATS.get(allKeys.get((new Random()).nextInt(allKeys.size())));
            //                //                if(project != null)
            //                //                {
            //                //                    project.getModel().render(stack, null, packedLightIn, overlay, 1F, 1F, 1F, 1F);
            //                //                }

            IRenderTypeBuffer.Impl bufferIn = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            bufferIn.getBuffer(RENDER_TYPE_RESET);
            stack.pop();
        }
    }
}
