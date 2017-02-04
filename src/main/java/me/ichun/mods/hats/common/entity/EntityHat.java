package me.ichun.mods.hats.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import me.ichun.mods.hats.client.core.HatInfoClient;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.core.HatInfo;
import me.ichun.mods.hats.common.core.SessionState;

@SideOnly(Side.CLIENT)
public class EntityHat extends Entity 
{

	public EntityLivingBase parent;
	public EntityLivingBase renderingParent; 
	public boolean render;
	public String hatName;
	
	public HatInfoClient info;
	
	public int reColour;
	
	public int prevR;
	public int prevG;
	public int prevB;
    public int prevAlpha;
	
	private int colourR;
	private int colourG;
	private int colourB;
    private int alpha;
	
	public long lastUpdate;
	
	public EntityHat(World par1World) 
	{
		super(par1World);
		setSize(0.2F, 0.2F);
		
		hatName = "";
		
		reColour = 0;
		
		prevR = 0;
		prevG = 0;
		prevB = 0;
        prevAlpha = 0;
		
		colourR = 0;
		colourG = 0;
		colourB = 0;
        alpha = 0;
		lastUpdate = par1World.getWorldTime();
		ignoreFrustumCheck = true;
//		validateHatInfo();
	}
	
	public EntityHat(World par1World, EntityLivingBase ent, HatInfo hatInfo)
	{
		super(par1World);
		setSize(0.2F, 0.2F);
		renderingParent = parent = ent;
		hatName = hatInfo.hatName;
		
		setLocationAndAngles(parent.posX, parent.getEntityBoundingBox().minY, parent.posZ, parent.rotationYaw, parent.rotationPitch);
		
		reColour = 0;
		
		prevR = 0;
		prevG = 0;
		prevB = 0;
        prevAlpha = 0;

		colourR = hatInfo.colourR;
		colourG = hatInfo.colourG;
		colourB = hatInfo.colourB;
        alpha = hatInfo.alpha;
		lastUpdate = par1World.getWorldTime();
		ignoreFrustumCheck = true;
//		validateHatInfo();
	}
	
	@Override
	public void onUpdate()
	{
		renderingParent = parent;
		render = true;
		ticksExisted++;
		
		if(reColour > 0)
		{
			reColour--;
		}
		
		if(parent == null || !parent.isEntityAlive() || parent.isChild())
		{
			setDead();
			return;
		}
		
//		if(iChunUtil.hasMorphMod && parent instanceof EntityPlayer)
//		{
//			EntityPlayer player = (EntityPlayer)parent;
//
//			EntityLivingBase morphEnt = morph.api.Api.getMorphEntity(player.getName(), true);
//			if(morphEnt != null)
//			{
//				renderingParent = morphEnt;
//			}
//		} FIXME Morph API
		
		lastUpdate = world.getWorldTime();

		validateHatInfo();
//		if(!(parent instanceof EntityPlayer))
//		{
//			world.spawnParticle("smoke", posX, posY, posZ, 1.0D, 0.0D, 0.0D);
//			world.spawnParticle("smoke", posX, posY, posZ, -1.0D, 0.0D, 0.0D);
//			world.spawnParticle("smoke", posX, posY, posZ, 0.0D, 1.0D, 0.0D);
//			world.spawnParticle("smoke", posX, posY, posZ, 0.0D, -1.0D, 0.0D);
//			world.spawnParticle("smoke", posX, posY, posZ, 0.0D, 0.0D, 1.0D);
//			world.spawnParticle("smoke", posX, posY, posZ, 0.0D, 0.0D, -1.0D);
//		}
	}
	
	@Override
    public int getBrightnessForRender(float par1)
    {
		if(Hats.config.renderHats == 13131) //TODO double check this
		{
			return 15728880;
		}
        return super.getBrightnessForRender(par1);
    }
	
	public void validateHatInfo()
	{
		boolean regen = true;
		if(info != null && info.hatName.equalsIgnoreCase(hatName) && info.colourR == getR() && info.colourG == getG() && info.colourB == getB() && info.alpha == getA() && info.prevColourR == prevR && info.prevColourG == prevG && info.prevColourB == prevB && info.prevAlpha == prevAlpha)
		{
			regen = false;
		}
		if(regen)
		{
			info = new HatInfoClient(hatName, getR(), getG(), getB(), getA());
		}
		info.recolour = reColour;
		info.doNotRender = !render;
		info.prevColourR = prevR;
		info.prevColourG = prevG;
		info.prevColourB = prevB;
        info.prevAlpha = prevAlpha;
	}
	
	public void setR(int i)
	{
		prevR = colourR;
		colourR = i;
	}
	
	public void setG(int i)
	{
		prevG = colourG;
		colourG = i;
	}
	
	public void setB(int i)
	{
		prevB = colourB;
		colourB = i;
	}

    public void setA(int i)
    {
        prevAlpha = alpha;
        alpha = i;
    }
	
	public int getR()
	{
		return colourR;
	}
	
	public int getG()
	{
		return colourG;
	}
	
	public int getB()
	{
		return colourB;
	}

    public int getA()
    {
        return alpha;
    }
	
	@Override
	public void setDead()
	{
		super.setDead();
		if(SessionState.serverHasMod == 1 && Hats.config.playerHatsMode == 4 && parent != null)
		{
			Hats.proxy.tickHandlerClient.requestedMobHats.remove((Object)parent.getEntityId());
		}
	}
	
	@Override
	public void entityInit() 
	{
	}
	
	@Override
    public boolean writeToNBTOptional(NBTTagCompound par1NBTTagCompound)//disable saving of the entity
    {
    	return false;
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) 
	{
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) 
	{
	}

}
