package hats.addons.hatstand.client.render;

import hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import hats.client.render.HatRendererHelper;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

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
	public static RenderBlocks renderBlocks = new RenderBlocks();
	
	public ModelSkeletonHead head32 = new ModelSkeletonHead(0, 0, 64, 32);
	public ModelSkeletonHead head64 = new ModelSkeletonHead(0, 0, 64, 64);
	
	public TileRendererHatStand()
	{
	}

    @Override
    public void func_147497_a(TileEntityRendererDispatcher par1TileEntityRenderer)
    {
        super.func_147497_a(par1TileEntityRenderer);
        renderer = this;
    }
	
	public void renderHatStand(TileEntityHatStand stand, double d, double d1, double d2, float f)
	{
		GL11.glPushMatrix();
		
		GL11.glTranslated(d + 0.5D, d1 + 0.4D, d2 + 0.5D);
        GL11.glScalef(-1.0F, -1.0F, 1.0F);

		GL11.glRotatef((stand.orientation - 1) * 90F, 0.0F, 1.0F, 0.0F);
		
        if(!stand.isOnFloor)
        {
        	if(stand.hasBase)
        	{
        		GL11.glTranslated(0.0D, 0.2D, 0.1D);
        	}
        	else
        	{
        		GL11.glTranslated(0.0D, 0.2D, 0.25D);
        	}
        }
        else if(!stand.hasStand)
        {
        	if(!stand.hasBase)
        	{
        		GL11.glTranslated(0.0D, 0.45D, 0.0D);
        	}
        	else
        	{
        		GL11.glTranslated(0.0D, 0.3D, 0.0D);
        	}
        }

		if(stand.head > 0)
		{
			if(!stand.hasStand)
			{
				GL11.glTranslated(0.0D, -0.05D, 0.0D);
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
            	ResourceLocation resourcelocation = AbstractClientPlayer.locationStevePng;
                if (stand.headName != null && stand.headName.length() > 0)
                {
                    resourcelocation = AbstractClientPlayer.getLocationSkull(stand.headName);
                    AbstractClientPlayer.getDownloadImageSkin(resourcelocation, stand.headName);
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
	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	        head.render((Entity)null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f4);
		}
		else
		{
			GL11.glTranslated(0.0D, 0.45D, 0.0D);
		}
		
		GL11.glScalef(-1.0F, -1.0F, 1.0F);
		
		GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
		
		HatRendererHelper.renderHat(stand.info, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.50000000000F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, true, true, f);
		
        GL11.glPopMatrix();
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double d0, double d1,
			double d2, float f) {
		this.renderHatStand((TileEntityHatStand)tileentity, d0, d1, d2, f);

	}

}
