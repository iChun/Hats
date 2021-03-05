package me.ichun.mods.hats.client.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.head.HeadInfo;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.fish.PufferfishEntity;
import net.minecraft.entity.passive.fish.TropicalFishEntity;
import net.minecraft.util.math.vector.Vector3f;

@SuppressWarnings("unchecked")
public class LayerHat<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M>
{
    public LayerHat()
    {
        super((IEntityRenderer<T, M>)Minecraft.getInstance().getRenderManager().playerRenderer); // nonnull, we'll just pass the player renderer
    }

    @Override
    public void render(MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn, LivingEntity living, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        HeadInfo helper = HeadHandler.getHelper(living.getClass());
        if(helper != null && !helper.noTopInfo)
        {
            EntityRenderer<?> render = Minecraft.getInstance().getRenderManager().getRenderer(living);
            if(!(render instanceof LivingRenderer))
            {
                return;
            }
            LivingRenderer<?, ?> renderer = (LivingRenderer<?, ?>)render;
            helper.setHeadModel(renderer);
            if(helper.headModel == null)
            {
                return;
            }
            int headCount = helper.getHeadCount(living); //TODO test tabula rendering outside overworld

            for(int i = 0; i < headCount; i++) //TODO figure out why changing dimension fucks up the render
            {
                if(living.isInvisible() && helper.affectedByInvisibility(living, -1, i))
                {
                    return;
                }

                float hatScale = helper.getHatScale(living, stack, partialTicks, i);

                if(hatScale <= 0F)
                {
                    continue;
                }
                hatScale *= 1.005F; //to reduce Z-fighting

                stack.push();

                // thepatcat: Creatures only get googly eyes in adulthood. It's science.
                helper.preChildEntHeadRenderCalls(living, stack, renderer);

                float[] joint = helper.getHeadJointOffset(living, stack, partialTicks, -1, i);
                stack.translate(-joint[0], -joint[1] - 0.005F, -joint[2]); //to fight Z-fighting

                stack.rotate(Vector3f.ZP.rotationDegrees(helper.getHeadRoll(living, stack, partialTicks, -1, i)));
                stack.rotate(Vector3f.YP.rotationDegrees(helper.getHeadYaw(living, stack, partialTicks, -1, i)));
                stack.rotate(Vector3f.XP.rotationDegrees(helper.getHeadPitch(living, stack, partialTicks, -1, i)));

                helper.postHeadTranslation(living, stack, partialTicks);

                //This stack of code is needed for automating the location for future mobs
/*
                if(living instanceof TropicalFishEntity
                        || living instanceof PufferfishEntity
                ) //interventions.
                {
                    ModelRenderer head = helper.headModel[0];

                    if(!head.cubeList.isEmpty())
                    {
                        ModelRenderer.ModelBox box = head.cubeList.get(0);
                        float dX = Math.abs(box.posX2 - box.posX1);
                        float dZ = Math.abs(box.posZ2 - box.posZ1);
                        float offX = ((dX / 2F) + box.posX1) / 16F;
                        float offY = box.posY1 / 16F; //just the top as posY1 is offset down from rotation point.
                        float offZ = ((dZ / 2F) + box.posZ1) / 16F;

                        float hori = Math.max(dX, dZ);
                        float scale = hori / 8F;

                        if(living instanceof TropicalFishEntity)
                        {
                            scale = 2f / 8F;
                            offZ -= 2F/16F;
                        }

                        stack.translate(offX, offY, offZ);
                        if(offX == 0.0F)
                        {
                            offX = -0.0F;
                        }
                        if(offY == 0.0F)
                        {
                            offY = -0.0F;
                        }
                        if(offZ == 0.0F)
                        {
                            offZ = -0.0F;
                        }
                        helper.headTopCenter = new float[] { -offX, -offY, -offZ };

                        stack.rotate(Vector3f.YP.rotationDegrees(helper.getHatYaw(living, stack, partialTicks, i)));
                        stack.rotate(Vector3f.XP.rotationDegrees(helper.getHatPitch(living, stack, partialTicks, i)));

                        helper.headScale = scale;
                        stack.scale(scale, scale, scale);
                    }
                }
                else
*/
                {
                    float[] headPoint = helper.getHatOffsetFromJoint(living, stack, partialTicks, i);
                    stack.translate(-headPoint[0], -headPoint[1], -headPoint[2]);

                    float[] armorOffset = helper.getHeadArmorOffset(living, stack, partialTicks, i);
                    if(armorOffset != null)
                    {
                        stack.translate(-armorOffset[0], -armorOffset[1], -armorOffset[2]);
                    }

                    stack.rotate(Vector3f.YP.rotationDegrees(helper.getHatYaw(living, stack, partialTicks, i)));
                    stack.rotate(Vector3f.XP.rotationDegrees(helper.getHatPitch(living, stack, partialTicks, i)));

                    stack.scale(hatScale, hatScale, hatScale);

                    float armorScale = helper.getHeadArmorScale(living, stack, partialTicks, i);
                    if(armorScale != 1F)
                    {
                        stack.scale(armorScale, armorScale, armorScale);
                    }
                }

                //render the project
                int overlay = LivingRenderer.getPackedOverlay(living, 0.0F);
                Project project = HatResourceHandler.HATS.get(Screen.hasControlDown() ? "Headphones" : "Skyrim Hat");
                if(project != null)
                {
                    project.getModel().render(stack, null, packedLightIn, overlay, 1F, 1F, 1F, 1F);
                }

                stack.pop();
            }
        }
    }
}
