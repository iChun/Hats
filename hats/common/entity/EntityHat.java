package hats.common.entity;

import hats.common.core.HatInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityHat extends Entity 
{

	public EntityPlayer player;
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
	
	public EntityHat(World par1World, EntityPlayer ply, HatInfo hatInfo) 
	{
		super(par1World);
		setSize(0.1F, 0.1F);
		player = ply;
		hatName = hatInfo.hatName;
		
		setPosition(player.posX, player.posY + player.getEyeHeight() - 0.35F, player.posZ);
		
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
		
		if(player == null || !player.isEntityAlive())
		{
			setDead();
			return;
		}
		
		lastUpdate = worldObj.getWorldTime();

		lastTickPosX = prevPosX = player.prevPosX;
		lastTickPosY = prevPosY = player.prevPosY + player.getEyeHeight() - 0.35F;
		lastTickPosZ = prevPosZ = player.prevPosZ;
		
		setPosition(posX, posY, posZ);
		posX = player.posX;
		posY = player.posY + player.getEyeHeight() - 0.35F;
		posZ = player.posZ;
		
		prevRotationYaw = player.prevRotationYawHead;
		rotationYaw = player.rotationYawHead;

		motionX = posX - prevPosX;
		motionY = posY - prevPosY;
		motionZ = posZ - prevPosZ;
		
		
		if(player.ridingEntity != null && !(player.ridingEntity instanceof EntityMinecart))
		{
			motionX = player.ridingEntity.posX - player.ridingEntity.prevPosX;
			motionY = player.ridingEntity.posY - player.ridingEntity.prevPosY;
			motionZ = player.ridingEntity.posZ - player.ridingEntity.prevPosZ;
			
			lastTickPosX = prevPosX = prevPosX + motionX;
			lastTickPosY = prevPosY = prevPosY + motionY;
			lastTickPosZ = prevPosZ = prevPosZ + motionZ;

			posX += motionX;
			posY += motionY;
			posZ += motionZ;
			
			if(player.ridingEntity instanceof EntityPig)
			{
				prevRotationYaw = ((EntityPig)player.ridingEntity).prevRenderYawOffset;
				rotationYaw = ((EntityPig)player.ridingEntity).renderYawOffset;
			}
		}
		
//		worldObj.spawnParticle("smoke", posX, posY, posZ, 1.0D, 0.0D, 0.0D);
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
