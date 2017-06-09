package me.ichun.mods.hats.client.core;

import me.ichun.mods.hats.client.gui.GuiHatSelection;
import me.ichun.mods.hats.client.render.RenderHat;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.core.HatHandler;
import me.ichun.mods.hats.common.core.ProxyCommon;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.hats.common.thread.ThreadHatsReader;
import me.ichun.mods.ichunutil.client.module.tabula.model.ModelTabula;
import me.ichun.mods.ichunutil.common.module.tabula.formats.ImportList;
import me.ichun.mods.ichunutil.common.module.tabula.project.ProjectInfo;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import java.io.File;
import java.util.HashMap;

public class ProxyClient extends ProxyCommon
{
    @Override
    public void preInitMod()
    {
        super.preInitMod();

        Hats.eventHandlerClient = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(Hats.eventHandlerClient);

        RenderingRegistry.registerEntityRenderingHandler(EntityHat.class, new RenderHat.RenderFactory());
    }

    @Override
    public void getHatsAndOpenGui()
    {
        new ThreadHatsReader(HatHandler.hatsFolder, false, true).start();
    }

    @Override
    public void clearAllHats()
    {
        super.clearAllHats();
        models.clear();
    }

    @Override
    public void remap(String duplicate, String original)
    {
        super.remap(duplicate, original);
        models.put(duplicate, models.get(original));
    }

    @Override
    public void openHatsGui()
    {
        FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiHatSelection(Minecraft.getMinecraft().thePlayer));
    }

    @Override
    public void loadHatFile(File file)
    {
        if(ImportList.isFileSupported(file))
        {
            ProjectInfo info = ImportList.createProjectFromFile(file);
            if(info != null)
            {
                String hatName = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
                HatHandler.getActualHatNamesMap().put(file, hatName);

                models.put(hatName, new ModelTabula(info));
//
//                File newHat = new File(file.getParentFile(), file.getName().substring(0, file.getName().length() - 4) + ".tbl");
//                ProjectInfo.saveProject(info, newHat);
            }
        }
    }

    public static HashMap<String, ModelTabula> models = new HashMap<>();

}
