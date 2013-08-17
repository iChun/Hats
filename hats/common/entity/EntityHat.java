package hats.common.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityHat extends Entity 
{

	public EntityLivingBase parent;
	public EntityLivingBase renderingParent; 
	public boolean render;
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
	
	public EntityHat(World par1World, EntityLivingBase ent, HatInfo hatInfo) 
	{
		super(par1World);
		setSize(0.1F, 0.1F);
		renderingParent = parent = ent;
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
		
		if(Hats.hasMorphMod && parent instanceof EntityPlayer)
		{
			try
			{
				EntityPlayer player = (EntityPlayer)parent;
				Object obj = Hats.morphMap.get(player.username);
				if(obj != null)
				{
					Method m = obj.getClass().getSuperclass().getDeclaredMethod("getMorphing");
					m.setAccessible(true);
					boolean morphing = (Boolean)m.invoke(obj);
					if(morphing)
					{
						render = false;
					}
					else
					{
						Field stateField = obj.getClass().getSuperclass().getDeclaredField("nextState");
						stateField.setAccessible(true);
						Object state = stateField.get(obj);
						if(state != null)
						{
							Class stateClz = state.getClass();
							
							Field entInstanceField = stateClz.getDeclaredField("entInstance");
							entInstanceField.setAccessible(true);
							EntityLivingBase ent = (EntityLivingBase)entInstanceField.get(state);
							if(ent != null)
							{
								renderingParent = ent;
							}
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		lastUpdate = worldObj.getWorldTime();

		lastTickPosX = prevPosX = renderingParent.prevPosX - HatHandler.getHorizontalPosOffset(renderingParent) * Math.sin(Math.toRadians(renderingParent.prevRenderYawOffset));
		lastTickPosY = prevPosY = renderingParent.prevPosY + HatHandler.getVerticalPosOffset(renderingParent);
		lastTickPosZ = prevPosZ = renderingParent.prevPosZ + HatHandler.getHorizontalPosOffset(renderingParent) * Math.cos(Math.toRadians(renderingParent.prevRenderYawOffset));
		
		setPosition(posX, posY, posZ);
		posX = renderingParent.posX - HatHandler.getHorizontalPosOffset(renderingParent) * Math.sin(Math.toRadians(renderingParent.renderYawOffset));
		posY = renderingParent.posY + HatHandler.getVerticalPosOffset(renderingParent);
		posZ = renderingParent.posZ + HatHandler.getHorizontalPosOffset(renderingParent) * Math.cos(Math.toRadians(renderingParent.renderYawOffset));
		
		prevRotationYaw = getPrevRotationYaw();
		rotationYaw = getRotationYaw();
		
		prevRotationPitch = getPrevRotationPitch();
		rotationPitch = getRotationPitch();

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
		
//		if(!(parent instanceof EntityPlayer))
//		{
//			worldObj.spawnParticle("smoke", posX, posY, posZ, 1.0D, 0.0D, 0.0D);
//			worldObj.spawnParticle("smoke", posX, posY, posZ, -1.0D, 0.0D, 0.0D);
//			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, 1.0D, 0.0D);
//			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, -1.0D, 0.0D);
//			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, 0.0D, 1.0D);
//			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, 0.0D, -1.0D);
//		}
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
	public void setDead()
	{
		super.setDead();
		if(Hats.proxy.tickHandlerClient.serverHasMod && Hats.proxy.tickHandlerClient.serverHatMode == 4 && parent != null)
		{
			Hats.proxy.tickHandlerClient.requestedMobHats.remove((Object)parent.entityId);
		}
	}
	
	public float getPrevRotationYaw()
	{
		if(parent instanceof EntityGhast || parent instanceof EntitySilverfish)
		{
			return parent.prevRenderYawOffset;
		}
		else if(parent instanceof EntitySquid)
		{
			return ((EntitySquid)parent).prevRenderYawOffset;
		}
		return parent.prevRotationYawHead;
	}

	public float getRotationYaw()
	{
		if(parent instanceof EntityGhast || parent instanceof EntitySilverfish)
		{
			return parent.renderYawOffset;
		}
		else if(parent instanceof EntitySquid)
		{
			return ((EntitySquid)parent).renderYawOffset;
		}
		return parent.rotationYawHead;
	}
	
	public float getPrevRotationPitch()
	{
		if(parent instanceof EntityGhast || parent instanceof EntitySilverfish)
		{
			return 0.0F;
		}
		else if(parent instanceof EntitySquid)
		{
			return -((EntitySquid)parent).prevSquidPitch;
		}
		return parent.prevRotationPitch;
	}

	public float getRotationPitch()
	{
		if(parent instanceof EntityGhast || parent instanceof EntitySilverfish)
		{
			return 0.0F;
		}
		else if(parent instanceof EntitySquid)
		{
			return -((EntitySquid)parent).squidPitch;
		}
		return parent.rotationPitch;
	}

	
	@Override
	public void entityInit() 
	{
	}
	
	@Override
    public boolean addEntityID(NBTTagCompound par1NBTTagCompound)//disable saving of the entity
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
