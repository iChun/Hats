package hats.common.entity;

import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityHat extends Entity 
{

	public EntityLiving parent;
	public String hatName;
	
	public int reColour;
	
	public int prevR;
	public int prevG;
	public int prevB;
	
	private int colourR;
	private int colourG;
	private int colourB;
	
	public long lastUpdate;
	
	public EntityHat(World par1World) 
	{
		super(par1World);
		setSize(0.1F, 0.1F);
		
		hatName = "";
		
		reColour = 0;
		
		prevR = 0;
		prevG = 0;
		prevB = 0;
		
		colourR = 0;
		colourG = 0;
		colourB = 0;
		lastUpdate = par1World.getWorldTime();
		ignoreFrustumCheck = true;
		renderDistanceWeight = 10D;
	}
	
	public EntityHat(World par1World, EntityLiving ent, HatInfo hatInfo) 
	{
		super(par1World);
		setSize(0.1F, 0.1F);
		parent = ent;
		hatName = hatInfo.hatName;
		
		setLocationAndAngles(parent.posX, parent.posY + parent.getEyeHeight() - 0.35F, parent.posZ, parent.rotationYaw, parent.rotationPitch);
		
		reColour = 0;
		
		prevR = 0;
		prevG = 0;
		prevB = 0;

		colourR = hatInfo.colourR;
		colourG = hatInfo.colourG;
		colourB = hatInfo.colourB;
		lastUpdate = par1World.getWorldTime();
		ignoreFrustumCheck = true;
		renderDistanceWeight = 10D;

	}
	
	@Override
	public void onUpdate()
	{
		ticksExisted++;
		
		if(reColour > 0)
		{
			reColour--;
		}
		
		if(parent == null || !parent.isEntityAlive())
		{
			setDead();
			return;
		}
		
		lastUpdate = worldObj.getWorldTime();

		lastTickPosX = prevPosX = parent.prevPosX - HatHandler.getHorizontalPosOffset(parent) * Math.sin(Math.toRadians(parent.prevRenderYawOffset));
		lastTickPosY = prevPosY = parent.prevPosY + HatHandler.getVerticalPosOffset(parent);
		lastTickPosZ = prevPosZ = parent.prevPosZ + HatHandler.getHorizontalPosOffset(parent) * Math.cos(Math.toRadians(parent.prevRenderYawOffset));
		
		setPosition(posX, posY, posZ);
		posX = parent.posX - HatHandler.getHorizontalPosOffset(parent) * Math.sin(Math.toRadians(parent.renderYawOffset));
		posY = parent.posY + HatHandler.getVerticalPosOffset(parent);
		posZ = parent.posZ + HatHandler.getHorizontalPosOffset(parent) * Math.cos(Math.toRadians(parent.renderYawOffset));
		
		prevRotationYaw = parent.prevRotationYawHead;
		rotationYaw = parent.rotationYawHead;

		motionX = posX - prevPosX;
		motionY = posY - prevPosY;
		motionZ = posZ - prevPosZ;
		
		
		if(parent.ridingEntity != null && !(parent.ridingEntity instanceof EntityMinecart))
		{
			motionX = parent.ridingEntity.posX - parent.ridingEntity.prevPosX;
			motionY = parent.ridingEntity.posY - parent.ridingEntity.prevPosY;
			motionZ = parent.ridingEntity.posZ - parent.ridingEntity.prevPosZ;
			
			lastTickPosX = prevPosX = prevPosX + motionX;
			lastTickPosY = prevPosY = prevPosY + motionY;
			lastTickPosZ = prevPosZ = prevPosZ + motionZ;

			posX += motionX;
			posY += motionY;
			posZ += motionZ;
			
			if(parent.ridingEntity instanceof EntityPig)
			{
				prevRotationYaw = ((EntityPig)parent.ridingEntity).prevRenderYawOffset;
				rotationYaw = ((EntityPig)parent.ridingEntity).renderYawOffset;
			}
		}
		
		if(!(parent instanceof EntityPlayer))
		{
			worldObj.spawnParticle("smoke", posX, posY, posZ, 1.0D, 0.0D, 0.0D);
			worldObj.spawnParticle("smoke", posX, posY, posZ, -1.0D, 0.0D, 0.0D);
			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, 1.0D, 0.0D);
			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, -1.0D, 0.0D);
			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, 0.0D, 1.0D);
			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, 0.0D, -1.0D);
		}
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
	
	@Override
	protected void entityInit() 
	{
	}
	
	@Override
    public boolean addEntityID(NBTTagCompound par1NBTTagCompound)//disable saving of the entity
    {
    	return false;
    }

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) 
	{
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) 
	{
	}

}
