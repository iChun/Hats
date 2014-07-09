package hats.api;

import net.minecraft.entity.EntityLivingBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * RenderOnEntityHelper class
 * Contains functions which are overriden to provide the Hats mod with values to help rendering hats on mobs. 
 * Mainly used on the client but also used on the server to see if mob should be allowed to wear a hat.
 * Think of it like the Entity class, extend it to make your own types.
 * @author iChun
 *
 */
public abstract class RenderOnEntityHelper 
{
	/**
	 * Indicates what class this render helper is meant for.
	 * @return Class this helper is for.
	 */
	public abstract Class helperForClass();
	
	/**
	 * Used to check if the mob can be added to the pool of mobs with hats.
	 * @param living
	 * @return canWearHat
	 */
	public boolean canWearHat(EntityLivingBase living)
	{
		return true;
	}

    /**
     * Used to check if the mob will unlock a hat if killed.
     * Recommend you disable if the mob that wears the hat can be spawned.
     * EG: Portalgun turrets.
     * @param living
     * @return canUnlockHat
     */
    public boolean canUnlockHat(EntityLivingBase living)
    {
        return true;
    }
	
	/**
	 * The following are mostly self explanatory.
	 * Only some need to be overriden such as EntitySquid or EntityGhast, etc.
	 * Rotate/OffsetPoint getters return a value in (number of voxels)/16. 16 voxels make up the length of one block.
	 * The player head, for example, is 8 x 8 x 8 voxels.
	 * Rotate point is the point on the model where the head rotates.
	 * Offset point is the point from the rotate point where the hat should lay on.
	 * Do note that the hat will also be scaled based on what preRenderCallback in RendererLivingEntity scales.
	 * @param living
	 * @return default mob values
	 */
	public float getPrevRenderYaw(EntityLivingBase living)
	{
		return living.prevRenderYawOffset;
	}

	public float getRenderYaw(EntityLivingBase living)
	{
		return living.renderYawOffset;
	}
	
	public float getPrevRotationYaw(EntityLivingBase living)
	{
		return living.prevRotationYawHead;
	}

	public float getRotationYaw(EntityLivingBase living)
	{
		return living.rotationYawHead;
	}
	
	public float getPrevRotationPitch(EntityLivingBase living)
	{
		return living.prevRotationPitch;
	}

	public float getRotationPitch(EntityLivingBase living)
	{
		return living.rotationPitch;
	}

    public float getPrevRotationRoll(EntityLivingBase living)
    {
        return 0.0F;
    }

	public float getRotationRoll(EntityLivingBase living)
	{
		return 0.0F;
	}

	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 0.0F;
	}
	
	public float getRotatePointHori(EntityLivingBase ent)
	{
		return 0.0F;
	}
	
	public float getRotatePointSide(EntityLivingBase ent)
	{
		return 0.0F;
	}
	
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 0.0F;
	}
	
	public float getOffsetPointHori(EntityLivingBase ent)
	{
		return 0.0F;
	}
	
	public float getOffsetPointSide(EntityLivingBase ent)
	{
		return 0.0F;
	}

	public float getHatScale(EntityLivingBase ent)
	{
		return 1.0F;
	}
	
	public int currentPass;
	
	/**
	 * Returns how many passes are needed for rendering the hat.
	 * For use of mobs with multiple heads.
	 * currentPass var will be set from 0 to (passesNeeded() - 1) before rendering
	 * @return
	 */
	public int passesNeeded()
	{
		return 1;
	}
}
