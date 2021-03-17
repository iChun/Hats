package me.ichun.mods.hats.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.packet.PacketEntityHatEntityDetails;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderHatEntity extends EntityRenderer<EntityHat>
{
    protected RenderHatEntity(EntityRendererManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void render(EntityHat hat, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        if(hat.hatPart.name.isEmpty() && hat.hatPart.count <= 0)
        {
            if(hat.hatPart.count == -1)
            {
                hat.hatPart.count = 0;
                Hats.channel.sendToServer(new PacketEntityHatEntityDetails(hat.getEntityId(), hat.hatPart.write(new CompoundNBT())));
            }
            return;
        }

        if(hat.age < 1)
        {
            return;
        }

        HatInfo hatInfo = HatResourceHandler.getInfoAndSetToPart(hat.hatPart);
        if(hatInfo != null)
        {
            stack.push();
            stack.translate(-hat.getWidth() / 2F, 0, -hat.getWidth() / 2F);

            stack.translate(((hat.hatDims[1] - hat.hatDims[0]) / 32F), 0F, ((hat.hatDims[5] - hat.hatDims[4]) / 32F)); //y2
            float scale = 0.2F + MathHelper.clamp((hat.age - 2 + partialTicks) / 10F, 0.0F, 0.8F);
            stack.scale(-scale, -scale, scale); //flip the models so it renders upright

            float halfHatHeight = ((hat.hatDims[3] - hat.hatDims[2]) / 16F) / 2F;
            stack.translate(0F, -halfHatHeight, 0F);
            stack.rotate(Vector3f.XP.rotationDegrees(hat.lastRotX + (hat.rotX - hat.lastRotX) * partialTicks));
            stack.rotate(Vector3f.YP.rotationDegrees(hat.lastRotY + (hat.rotY - hat.lastRotY) * partialTicks));
            stack.translate(0F, halfHatHeight, 0F);

            stack.translate(0F, -hat.hatDims[3] / 16F, 0F);

            hatInfo.render(stack, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, true);
            stack.pop();
        }

        super.render(hat, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(EntityHat entity)
    {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }

    public static class RenderFactory implements IRenderFactory<EntityHat>
    {
        @Override
        public EntityRenderer<? super EntityHat> createRenderFor(EntityRendererManager manager)
        {
            return new RenderHatEntity(manager);
        }
    }
}
