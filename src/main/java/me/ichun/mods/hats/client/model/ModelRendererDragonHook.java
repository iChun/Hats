package me.ichun.mods.hats.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.hats.client.layer.LayerHat;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.head.HeadInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
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

        if(Hats.eventHandlerClient.renderCount >= Hats.configClient.maxHatRenders)
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

        if(living.getPersistentData().contains(HatHandler.NBT_HAT_KEY) && living.getPersistentData().getCompound(HatHandler.NBT_HAT_KEY).contains(HatHandler.NBT_HAT_SET_KEY))
        {
            //we have hat data
            String hatDetails = HatHandler.getHatDetails(living);
            if(!hatDetails.isEmpty()) // if it's empty, we don't actually have the details yet, or actually there's no hat.
            {
                if(LayerHat.renderHat(helper, null, stack, packedLightIn, packedOverlayIn, living, lastPartialTick, hatDetails))
                {
                    IRenderTypeBuffer.Impl bufferIn = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
                    bufferIn.getBuffer(RENDER_TYPE_RESET);
                }
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
            else if(living.getRNG().nextDouble() < Hats.configClient.hatChance && Hats.eventHandlerClient.connectionAge > 100) //assign a random hat, client-only after all. 5 second connection cooldown
            {
                HatHandler.assignHat(living);
            }
        }
    }
}
