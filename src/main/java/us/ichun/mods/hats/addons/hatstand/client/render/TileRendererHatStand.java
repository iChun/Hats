package us.ichun.mods.hats.addons.hatstand.client.render;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import us.ichun.mods.hats.client.render.HatRendererHelper;

import java.util.Map;

public class TileRendererHatStand extends TileEntitySpecialRenderer
{
    public static final ResourceLocation texSkele		 = new ResourceLocation("textures/entity/skeleton/skeleton.png");
    public static final ResourceLocation texSkeleWither	 = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");
    public static final ResourceLocation texPigman		 = new ResourceLocation("hats", "textures/entity/pigman.png");
    public static final ResourceLocation texZombie		 = new ResourceLocation("textures/entity/zombie/zombie.png");
    public static final ResourceLocation texCreeper		 = new ResourceLocation("textures/entity/creeper/creeper.png");
    public static final ResourceLocation texPlayer		 = new ResourceLocation("textures/entity/steve.png");
    public static final ResourceLocation texWitherInvul	 = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
    public static final ResourceLocation texWither		 = new ResourceLocation("textures/entity/wither/wither.png");
    public static final ResourceLocation texBlaze		 = new ResourceLocation("textures/entity/blaze.png");
    public static final ResourceLocation texSpiderEyes	 = new ResourceLocation("textures/entity/spider_eyes.png");

    public static TileRendererHatStand renderer;

    public ModelSkeletonHead head32 = new ModelSkeletonHead(0, 0, 64, 32);
    public ModelSkeletonHead head64 = new ModelSkeletonHead(0, 0, 64, 64);

    public TileRendererHatStand()
    {
    }

    @Override
    public void setRendererDispatcher(TileEntityRendererDispatcher par1TileEntityRenderer)
    {
        super.setRendererDispatcher(par1TileEntityRenderer);
        renderer = this;
    }

    public void renderHatStand(TileEntityHatStand stand, double d, double d1, double d2, float f, int destroyState, GameProfile profile)
    {
        GlStateManager.alphaFunc(GL11.GL_GREATER, 16F/255F);
        GlStateManager.pushMatrix();

        GlStateManager.translate(d + 0.5D, d1 + 0.4D, d2 + 0.5D);
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);

        GlStateManager.rotate((stand.orientation - 1) * 90F, 0.0F, 1.0F, 0.0F);

        if(!stand.isOnFloor)
        {
            if(stand.hasBase)
            {
                GlStateManager.translate(0.0D, 0.2D, 0.1D);
            }
            else
            {
                GlStateManager.translate(0.0D, 0.2D, 0.25D);
            }
        }
        else if(!stand.hasStand)
        {
            if(!stand.hasBase)
            {
                GlStateManager.translate(0.0D, 0.45D, 0.0D);
            }
            else
            {
                GlStateManager.translate(0.0D, 0.3D, 0.0D);
            }
        }

        if(stand.head > 0)
        {
            if (destroyState >= 0)
            {
                this.bindTexture(DESTROY_STAGES[destroyState]);
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.pushMatrix();
                GlStateManager.scale(4.0F, 2.0F, 1.0F);
                GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            }

            if(!stand.hasStand)
            {
                GlStateManager.translate(0.0D, -0.05D, 0.0D);
            }

            ModelSkeletonHead head = head32;

            switch(stand.head)
            {
                case 1:
                default:
                    this.bindTexture(texSkele);
                    break;
                case 2:
                    this.bindTexture(texSkeleWither);
                    break;
                case 3:
                    this.bindTexture(texZombie);
                    head = head64;
                    break;
                case 4:

                    ResourceLocation resourcelocation = DefaultPlayerSkin.getDefaultSkinLegacy();
                    if (profile != null)
                    {
                        Minecraft minecraft = Minecraft.getMinecraft();
                        Map map = minecraft.getSkinManager().loadSkinFromCache(profile);

                        if (map.containsKey(MinecraftProfileTexture.Type.SKIN))
                        {
                            resourcelocation = minecraft.getSkinManager().loadSkin((MinecraftProfileTexture)map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                        }
                    }
                    this.bindTexture(resourcelocation);
                    break;
                case 5:
                    this.bindTexture(texPlayer);
                    break;
                case 6:
                    this.bindTexture(texCreeper);
                    break;
                case 7:
                    this.bindTexture(texWither);
                    head = head64;
                    break;
                case 8:
                    this.bindTexture(texWitherInvul);
                    head = head64;
                    break;
                case 9:
                    this.bindTexture(texPigman);
                    break;
                case 10:
                    this.bindTexture(texBlaze);
                    break;
                case 11:
                    this.bindTexture(texSpiderEyes);
                    break;
            }


            float f4 = 0.0625F;
            GlStateManager.enableRescaleNormal();
            head.render((Entity)null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f4);

            if (destroyState >= 0)
            {
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            }
        }
        else
        {
            GlStateManager.translate(0.0D, 0.45D, 0.0D);
        }

        GlStateManager.scale(-1.0F, -1.0F, 1.0F);

        GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);

        HatRendererHelper.renderHat(stand.info, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.50000000000F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, true, true, f);

        GlStateManager.popMatrix();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1,
                                   double d2, float f, int destroyState) {
        this.renderHatStand((TileEntityHatStand)tileentity, d0, d1, d2, f, destroyState, ((TileEntityHatStand)tileentity).gameProfile);

    }

}
