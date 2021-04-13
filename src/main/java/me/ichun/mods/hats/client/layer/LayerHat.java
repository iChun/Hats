package me.ichun.mods.hats.client.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.api.common.head.HeadInfo;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.util.EventCalendar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
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
        if(Hats.eventHandlerClient.renderCount >= Hats.configClient.maxHatRenders && !(living instanceof PlayerEntity) || living.removed)
        {
            return;
        }

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

            if(HatHandler.hasBeenRandomlyAllocated(living))
            {
                HatsSavedData.HatPart hatPart = HatHandler.getHatPart(living);
                if(hatPart.isAHat())
                {
                    //we have hat data
                    int overlay = LivingRenderer.getPackedOverlay(living, 0.0F);
                    renderHat(helper, renderer, stack, bufferIn, packedLightIn, overlay, living, partialTicks, hatPart);
                }
            }
            else
            {
                //we don't have the hat data
                if(Hats.eventHandlerClient.serverHasMod)
                {
                    Hats.eventHandlerClient.requestHatDetails(living);
                    HatHandler.assignNoHat(living);
                }
                else if(Minecraft.getInstance().player.ticksExisted > 100) //assign a random hat, client-only after all. 5 second connection cooldown
                {
                    HatHandler.assignHatClient(living);
                }
            }
        }
    }

    public static boolean renderHat(HeadInfo helper, LivingRenderer<?, ?> renderer, MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn, int packedOverlayIn, LivingEntity living, float partialTicks, HatsSavedData.HatPart hatDetails)
    {
        int headCount = helper.getHeadCount(living);

        boolean flag = false;

        for(int i = 0; i < headCount; i++)
        {
            if(living.isInvisible() && helper.affectedByInvisibility(living, -1, i) && !Hats.eventHandlerClient.forceRenderWhenInvisible)
            {
                continue;
            }

            float hatScale = helper.getHatScale(living, stack, partialTicks, i);

            if(hatScale <= 0F)
            {
                continue;
            }
            hatScale *= 1.005F; //to reduce Z-fighting

            stack.push();

            if(!(living instanceof EnderDragonEntity))
            {
                // thepatcat: Creatures only get googly eyes in adulthood. It's science.
                helper.preChildEntHeadRenderCalls(living, stack, renderer);

                float[] joint = helper.getHeadJointOffset(living, stack, partialTicks, -1, i);
                stack.translate(-joint[0], -joint[1], -joint[2]); //to fight Z-fighting

                stack.rotate(Vector3f.ZP.rotationDegrees(helper.getHeadRoll(living, stack, partialTicks, -1, i)));
                stack.rotate(Vector3f.YP.rotationDegrees(helper.getHeadYaw(living, stack, partialTicks, -1, i)));
                stack.rotate(Vector3f.XP.rotationDegrees(helper.getHeadPitch(living, stack, partialTicks, -1, i)));

                helper.postHeadTranslation(living, stack, partialTicks);
            }

            //This stack of code is needed for automating the location for future mobs
/*
                if(living instanceof TropicalFishEntity
                        || living instanceof PufferfishEntity
                ) //interventions.
                    ModelRenderer head = helper.headModel[0];

                {
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
                stack.translate(-headPoint[0], -headPoint[1] - 0.00225F, -headPoint[2]);

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

            if(EventCalendar.isEventDay() && iChunUtil.configClient.easterEgg)
            {
                hatDetails = hatDetails.createCopy();
                hatDetails.eventDay(living.ticksExisted, partialTicks);
            }

            //render the project
            HatInfo hatInfo = HatResourceHandler.getInfoAndSetToPart(hatDetails);
            if(hatInfo != null)
            {
                hatInfo.render(stack, bufferIn, packedLightIn, packedOverlayIn, true);

                Hats.eventHandlerClient.renderCount++;

                flag = true;
            }
            else if(Hats.eventHandlerClient.serverHasMod)
            {
                Hats.eventHandlerClient.showSyncToast();
            }

            stack.pop();
        }
        return flag;
    }
}
