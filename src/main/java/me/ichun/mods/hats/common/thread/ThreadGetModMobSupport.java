package me.ichun.mods.hats.common.thread;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import me.ichun.mods.hats.common.core.CommomProxy;
import net.minecraft.entity.EntityLivingBase;
import me.ichun.mods.hats.client.render.helper.HelperGeneric;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.core.HatHandler;

import java.io.*;
import java.net.URL;
import java.util.Map;

public class ThreadGetModMobSupport extends Thread
{
    public ThreadGetModMobSupport()
    {
        this.setName("Hats Mod Mob Support Thread");
        this.setDaemon(true);
    }

    @Override
    public void run()
    {
        try
        {
            Gson gson = new Gson();
            Map<String, Object> json;
            if(Hats.config.readLocalModMobSupport == 1)
            {
                InputStream con = new FileInputStream(new File(HatHandler.hatsFolder, "HatModMobSupport.json"));
                String data = new String(ByteStreams.toByteArray(con));
                con.close();
                json = gson.fromJson(data, Map.class);
            }
            else
            {
                //https://raw.github.com/iChun/Hats/master/src/main/resources/assets/hats/mod/HatModMobSupport.json
                Reader fileIn = new InputStreamReader(new URL("https://raw.github.com/iChun/Hats/master/src/main/resources/assets/hats/mod/HatModMobSupport.json").openStream());
                json = gson.fromJson(fileIn, Map.class);
                fileIn.close();
            }

            for(Map.Entry<String, Object> e : json.entrySet())
            {
                try
                {
                    Class clz = Class.forName(e.getKey());
                    if(!EntityLivingBase.class.isAssignableFrom(clz))
                    {
                        continue;
                    }
                    Map<String, Object> vars = (Map<String, Object>)e.getValue();
                    Boolean bool = (Boolean)vars.get("canUnlockHat");
                    if(bool == null)
                    {
                        bool = true;
                    }
                    HelperGeneric helperGeneric = new HelperGeneric(clz, bool);
                    helperGeneric.prevRenderYawOffset = getVar(vars.get("prevRenderYawOffset"));
                    helperGeneric.renderYawOffset = getVar(vars.get("renderYawOffset"));
                    helperGeneric.prevRotationYawHead = getVar(vars.get("prevRotationYawHead"));
                    helperGeneric.rotationYawHead = getVar(vars.get("rotationYawHead"));
                    helperGeneric.prevRotationPitch = getVar(vars.get("prevRotationPitch"));
                    helperGeneric.rotationPitch = getVar(vars.get("rotationPitch"));

                    helperGeneric.rotatePointVert = getVar(vars.get("rotatePointVert"));
                    helperGeneric.rotatePointHori = getVar(vars.get("rotatePointHori"));
                    helperGeneric.rotatePointSide = getVar(vars.get("rotatePointSide"));

                    helperGeneric.offsetPointVert = getVar(vars.get("offsetPointVert"));
                    helperGeneric.offsetPointHori = getVar(vars.get("offsetPointHori"));
                    helperGeneric.offsetPointSide = getVar(vars.get("offsetPointSide"));

                    helperGeneric.hatScale = getVar(vars.get("hatScale"));

                    CommomProxy.renderHelpers.put(clz, helperGeneric);

                    Hats.console("Registered " + clz.getName() + " with hat mod mappings.");
                }
                catch(ClassNotFoundException e1)
                {
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Object getVar(Object obj)
    {
        if(obj == null)
        {
            return null;
        }
        try
        {
            return Float.parseFloat(obj.toString());
        }
        catch(NumberFormatException e)
        {
            e.printStackTrace();
            return obj.toString();
        }
    }
}
