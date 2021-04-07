package me.ichun.mods.hats.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.client.model.ModelHatLauncher;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.hats.advancement.Advancements;
import me.ichun.mods.hats.common.item.ItemHatLauncher;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.model.item.IModel;
import me.ichun.mods.ichunutil.client.model.item.ItemModelRenderer;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.iChunUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemTransformVec3f;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class ItemRenderHatLauncher extends ItemStackTileEntityRenderer
        implements IModel
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("hats", "textures/model/hat_launcher.png");
    private static final ItemCameraTransforms ITEM_CAMERA_TRANSFORMS = new ItemCameraTransforms(
            new ItemTransformVec3f(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(-0.025F, 0.1875F, 0.03125F), new Vector3f(1.0F, 1.0F, 1.0F)),//tp left
            new ItemTransformVec3f(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(-0.025F, 0.1875F, 0.03125F), new Vector3f(1.0F, 1.0F, 1.0F)),//tp right
            new ItemTransformVec3f(new Vector3f(10F, 5F, -12.5F), new Vector3f(0.1F, 0F, 0.05F), new Vector3f(1.0F, 1.0F, 1.0F)),//fp left
            new ItemTransformVec3f(new Vector3f(10F, 5F, -12.5F), new Vector3f(0.1F, 0F, 0.05F), new Vector3f(1.0F, 1.0F, 1.0F)),//fp right
            new ItemTransformVec3f(new Vector3f(0F, 0F, -63.0F), new Vector3f(-0.10F, 0.66F, 0.25F), new Vector3f(1.5F, 1.5F, 1.5F)),//head
            new ItemTransformVec3f(new Vector3f(45F, 45F, -20.0F), new Vector3f(0.25F, -0.15F, 0.0F), new Vector3f(0.9F, 0.9F, 0.9F)),//gui
            new ItemTransformVec3f(new Vector3f(45F, 45F, -20.0F), new Vector3f(0.0F, 0.15F, 0.0F), new Vector3f(0.4F, 0.4F, 0.4F)),//ground
            new ItemTransformVec3f(new Vector3f(0F, 90F, 0F), new Vector3f(0.3F, 0.0F, -0.05F), new Vector3f(0.9F, 0.9F, 0.9F))//fixed
    );
    public static final ItemStack MAGMA_BLOCK = new ItemStack(Blocks.MAGMA_BLOCK);
    public static final ItemRenderHatLauncher INSTANCE = new ItemRenderHatLauncher();
    public static final Random RAND = new Random();

    //Stuff to do in relation to getting the current perspective and the current player holding it
    private ItemCameraTransforms.TransformType currentPerspective;
    private AbstractClientPlayerEntity lastPlayer;
    private boolean isAdvancementRender;

    private ModelHatLauncher launcherModel;

    private ItemRenderHatLauncher()
    {
        launcherModel = new ModelHatLauncher();
    }

    @Override
    public void func_239207_a_(ItemStack is, ItemCameraTransforms.TransformType transformType, MatrixStack stack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        setToOrigin(stack);

        if(isAdvancementRender)
        {
            stack.scale(-1.0F, 1.0F, 1.0F);

            RenderHelper.drawTexture(stack, Advancements.DAMAGE_TO_TEXTURE_MAP.get(is.getDamage()), -0.5D, -0.5D, 1D, 1D, 0D);
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        boolean leftHand = ItemModelRenderer.isLeftHand(currentPerspective);
        launcherModel.gasL.showModel = launcherModel.headL.showModel = leftHand;
        launcherModel.gasR.showModel = launcherModel.headR.showModel = !leftHand;

        stack.push();
        //translate to gas thingy
        stack.translate(0F, 0F, -0.14F);
        stack.scale(0.15F, 0.15F, 0.15F);

        //rotate to follow hand
        stack.rotate(Vector3f.ZP.rotation(leftHand ? -0.7853981633974483F : 0.7853981633974483F));

        //translate to into the glass
        stack.translate((leftHand ? 1.75F : -1.75F) + (float)Math.sin(Math.toRadians((iChunUtil.eventHandlerClient.ticks + iChunUtil.eventHandlerClient.partialTick) / 41F * 180F)) * (leftHand ? 0.35F : -0.35F), 0F, 0F);

        //rotate the cube
        stack.rotate(Vector3f.YP.rotationDegrees((float)Math.sin(Math.toRadians((iChunUtil.eventHandlerClient.ticks + iChunUtil.eventHandlerClient.partialTick) / 37F * 180F)) * 180F));
        stack.rotate(Vector3f.XP.rotationDegrees((float)Math.sin(Math.toRadians((iChunUtil.eventHandlerClient.ticks + iChunUtil.eventHandlerClient.partialTick) / 87F * 180F)) * 180F));
        mc.getItemRenderer().renderItem(MAGMA_BLOCK, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, stack, bufferIn);

        stack.pop();

        launcherModel.render(stack, ItemRenderer.getEntityGlintVertexBuilder(bufferIn, RenderType.getEntityTranslucentCull(TEXTURE), false, is.hasEffect()), combinedLightIn, combinedOverlayIn, 1F, 1F, 1F, 1F);

        if(lastPlayer != null) //only render the hat and the head when it's a player holding it
        {
            stack.push();
            launcherModel.barrel.translateRotate(stack);
            (leftHand ? launcherModel.headL : launcherModel.headR).render(stack, bufferIn.getBuffer(RenderType.getEntityCutout(lastPlayer.getLocationSkin())), combinedLightIn, combinedOverlayIn, 1F, 1F, 1F, 1F);
            stack.pop();

            HatsSavedData.HatPart part = HatHandler.getHatPart(is);
            part.read(is.getOrCreateTag().getCompound(ItemHatLauncher.STACK_HAT_PART_TAG));

            if(!part.name.isEmpty() && !(lastPlayer != mc.player && part.name.equals(":random")) && part.isShowing && part.count > 0)
            {
                if(lastPlayer == mc.player && part.name.equals(":random"))
                {
                    List<HatsSavedData.HatPart> source = HatHandler.getHatSource(mc.player).stream().filter(hatPart -> !(hatPart.count <= 0)).collect(Collectors.toList());

                    RAND.setSeed((1342L + iChunUtil.eventHandlerClient.ticks) / Hats.configClient.hatLauncherRandomHatSpeed);

                    if(source.size() > 0)
                    {
                        part = source.get(RAND.nextInt(source.size()));
                    }
                    else
                    {
                        part = null;
                    }
                }

                if(part != null)
                {
                    HatInfo hatInfo = HatResourceHandler.getInfoAndSetToPart(part);
                    if(hatInfo != null)
                    {
                        stack.push();
                        stack.translate(0F, 0F, -0.0625F);
                        stack.scale(0.1876F, 0.1876F, 0.1876F);

                        stack.rotate(Vector3f.ZP.rotation(leftHand ? -0.7853981633974483F : 0.7853981633974483F));

                        stack.translate(0F, -1.1875F, 0F);

                        stack.rotate(Vector3f.YP.rotationDegrees(180F));

                        hatInfo.render(stack, bufferIn, combinedLightIn, combinedOverlayIn, true);
                        stack.pop();
                    }
                }
            }
        }

        //reset these vars. they should be set per render.
        lastPlayer = null;
        currentPerspective = null;
    }

    @Override
    public ItemCameraTransforms getCameraTransforms()
    {
        return isAdvancementRender ? ItemCameraTransforms.DEFAULT : ITEM_CAMERA_TRANSFORMS;
    }

    @Override
    public void handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat)
    {
        currentPerspective = cameraTransformType;
    }

    @Override
    public void handleItemState(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity)
    {
        isAdvancementRender = stack.getDamage() > 0;
        if(entity instanceof AbstractClientPlayerEntity)
        {
            lastPlayer = (AbstractClientPlayerEntity)entity;
        }
    }
}
