package us.ichun.mods.hats.client.render.helper;

import us.ichun.mods.hats.api.RenderOnEntityHelper;
import net.minecraft.entity.EntityLivingBase;
import us.ichun.mods.hats.api.RenderOnEntityHelper;

/**
 * This class is really ugly and uses a lot of reflection but I don't know how else I would have done it.
 */
public class HelperGeneric extends RenderOnEntityHelper
{
    public final Class entityClass;
    public final boolean canUnlockHat;

    public float entPrevRenderYawOffset;
    public float entRenderYawOffset;
    public float entPrevRotationYawHead;
    public float entRotationYawHead;
    public float entPrevRotationPitch;
    public float entRotationPitch;

    public Object prevRenderYawOffset;
    public Object renderYawOffset;
    public Object prevRotationYawHead;
    public Object rotationYawHead;
    public Object prevRotationPitch;
    public Object rotationPitch;

    public Object rotatePointVert;
    public Object rotatePointHori;
    public Object rotatePointSide;

    public Object offsetPointVert;
    public Object offsetPointHori;
    public Object offsetPointSide;

    public Object hatScale;

    public HelperGeneric(Class entityClass, boolean canUnlockHat)
    {
        this.entityClass = entityClass;
        this.canUnlockHat = canUnlockHat;
    }

    @Override
    public Class helperForClass()
    {
        return entityClass;
    }

    @Override
    public boolean canUnlockHat(EntityLivingBase living)
    {
        return canUnlockHat;
    }

    public void update(EntityLivingBase living)
    {
        entPrevRenderYawOffset = living.prevRenderYawOffset;
        entRenderYawOffset = living.renderYawOffset;
        entPrevRotationYawHead = living.prevRotationYawHead;
        entRotationYawHead = living.rotationYawHead;
        entPrevRotationPitch = living.prevRotationPitch;
        entRotationPitch = living.rotationPitch;
    }

    @Override
    public float getPrevRenderYaw(EntityLivingBase living)
    {
        return getValue("prevRenderYawOffset");
    }

    @Override
    public float getRenderYaw(EntityLivingBase living)
    {
        return getValue("renderYawOffset");
    }

    @Override
    public float getPrevRotationYaw(EntityLivingBase living)
    {
        return getValue("prevRotationYawHead");
    }

    @Override
    public float getRotationYaw(EntityLivingBase living)
    {
        return getValue("rotationYawHead");
    }

    @Override
    public float getPrevRotationPitch(EntityLivingBase living)
    {
        return getValue("prevRotationPitch");
    }

    @Override
    public float getRotationPitch(EntityLivingBase living)
    {
        return getValue("rotationPitch");
    }

    @Override
    public float getRotatePointVert(EntityLivingBase ent)
    {
        return getValue("rotatePointVert");
    }

    @Override
    public float getRotatePointHori(EntityLivingBase ent)
    {
        return getValue("rotatePointHori");
    }

    @Override
    public float getRotatePointSide(EntityLivingBase ent)
    {
        return getValue("rotatePointSide");
    }

    @Override
    public float getOffsetPointVert(EntityLivingBase ent)
    {
        return getValue("offsetPointVert");
    }

    @Override
    public float getOffsetPointHori(EntityLivingBase ent)
    {
        return getValue("offsetPointHori");
    }

    @Override
    public float getOffsetPointSide(EntityLivingBase ent)
    {
        return getValue("offsetPointSide");
    }

    @Override
    public float getHatScale(EntityLivingBase ent)
    {
        return getValue("hatScale");
    }

    public float getValue(String s)
    {
        Object obj = null;
        if(s.equalsIgnoreCase("prevRenderYawOffset"))
        {
            obj = prevRenderYawOffset;
        }
        else if(s.equalsIgnoreCase("renderYawOffset"))
        {
            obj = renderYawOffset;
        }
        else if(s.equalsIgnoreCase("prevRotationYawHead"))
        {
            obj = prevRotationYawHead;
        }
        else if(s.equalsIgnoreCase("rotationYawHead"))
        {
            obj = rotationYawHead;
        }
        else if(s.equalsIgnoreCase("prevRotationPitch"))
        {
            obj = prevRotationPitch;
        }
        else if(s.equalsIgnoreCase("rotationPitch"))
        {
            obj = rotationPitch;
        }
        else if(s.equalsIgnoreCase("rotatePointVert"))
        {
            obj = rotatePointVert;
        }
        else if(s.equalsIgnoreCase("rotatePointHori"))
        {
            obj = rotatePointHori;
        }
        else if(s.equalsIgnoreCase("rotatePointSide"))
        {
            obj = rotatePointSide;
        }
        else if(s.equalsIgnoreCase("offsetPointVert"))
        {
            obj = offsetPointVert;
        }
        else if(s.equalsIgnoreCase("offsetPointHori"))
        {
            obj = offsetPointHori;
        }
        else if(s.equalsIgnoreCase("offsetPointSide"))
        {
            obj = offsetPointSide;
        }
        else if(s.equalsIgnoreCase("hatScale"))
        {
            obj = hatScale;
        }

        if(obj != null || !s.equalsIgnoreCase("hatScale"))
        {
            if(obj instanceof Float)
            {
                return (Float)obj;
            }
            else if(obj instanceof String || obj == null)
            {
                return getEntValue(obj == null ? s : (String)obj);
            }
        }
        if(s.equals("hatScale"))
        {
            return 1.0F;
        }
        return 0.0F;
    }

    public float getEntValue(String s)
    {
        if(s.equalsIgnoreCase("prevRenderYawOffset"))
        {
            return entPrevRenderYawOffset;
        }
        else if(s.equalsIgnoreCase("renderYawOffset"))
        {
            return entRenderYawOffset;
        }
        else if(s.equalsIgnoreCase("prevRotationYawHead"))
        {
            return entPrevRotationYawHead;
        }
        else if(s.equalsIgnoreCase("rotationYawHead"))
        {
            return entRotationYawHead;
        }
        else if(s.equalsIgnoreCase("prevRotationPitch"))
        {
            return entPrevRotationPitch;
        }
        else if(s.equalsIgnoreCase("rotationPitch"))
        {
            return entRotationPitch;
        }
        return 0.0F;
    }
}
