package hats.common.entity;

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
	
	public EntityHat(World par1World) 
	{
		super(par1World);
		setSize(0.1F, 0.1F);
		hatName = "";
		ignoreFrustumCheck = true;
		renderDistanceWeight = 10D;
	}
	
	public EntityHat(World par1World, EntityPlayer ply) 
	{
		super(par1World);
		setSize(0.1F, 0.1F);
		player = ply;
		hatName = "minerhat";
		ignoreFrustumCheck = true;
		renderDistanceWeight = 10D;

	}
	
	@Override
	public void onUpdate()
	{
		ticksExisted++;
		if(player == null || !player.isEntityAlive())
		{
			setDead();
			return;
		}

		lastTickPosX = prevPosX = player.prevPosX;
		lastTickPosY = prevPosY = player.prevPosY - player.getEyeHeight();
		lastTickPosZ = prevPosZ = player.prevPosZ;
		
		setPosition(posX, posY, posZ);
		posX = player.posX;
		posY = player.posY - player.getEyeHeight();
		posZ = player.posZ;
		
		prevRotationYaw = player.prevRenderYawOffset;
		rotationYaw = player.renderYawOffset;

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
