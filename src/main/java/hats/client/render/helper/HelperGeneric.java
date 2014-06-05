package hats.client.render.helper;

import hats.api.RenderOnEntityHelper;
import net.minecraft.entity.EntityLivingBase;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * This class is really ugly and uses a lot of reflection but I don't know how else I would have done it.
 */
public class HelperGeneric extends RenderOnEntityHelper
{
    public final Class entityClass;

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

    public HelperGeneric(Class entityClass)
    {
        this.entityClass = entityClass;
    }

    @Override
    public Class helperForClass()
    {
        return entityClass;
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
        try
        {
            Field f = this.getClass().getDeclaredField(s);
            f.setAccessible(true);
            Object obj = f.get(this);
            if(obj instanceof Float)
            {
                return (Float)obj;
            }
            else if(obj instanceof String || obj == null)
            {
                Field f1 = stringToFieldMap.get(obj == null ? s : (String)obj);
                if(f1 != null)
                {
                    f1.setAccessible(true);
                    return f1.getFloat(this);
                }
            }
        }
        catch(Exception e)
        {
        }
        if(s.equals("hatScale"))
        {
            return 1.0F;
        }
        return 0.0F;
    }

    public static HashMap<String, Field> stringToFieldMap = new HashMap<String, Field>() {{
        try
        {
            put("prevRenderYawOffset", HelperGeneric.class.getDeclaredField("entPrevRenderYawOffset"));
            put("renderYawOffset", HelperGeneric.class.getDeclaredField("entRenderYawOffset"));
            put("prevRotationYawHead", HelperGeneric.class.getDeclaredField("entPrevRotationYawHead"));
            put("rotationYawHead", HelperGeneric.class.getDeclaredField("entRotationYawHead"));
            put("prevRotationPitch", HelperGeneric.class.getDeclaredField("entPrevRotationPitch"));
            put("rotationPitch", HelperGeneric.class.getDeclaredField("entRotationPitch"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }};
}
