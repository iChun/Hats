package us.ichun.mods.hats.client.core;

import us.ichun.mods.hats.client.gui.GuiHatSelection;
import us.ichun.mods.hats.client.render.RenderHat;
import us.ichun.mods.hats.common.core.CommonProxy;
import us.ichun.mods.hats.common.core.HatHandler;
import us.ichun.mods.hats.common.entity.EntityHat;
import us.ichun.mods.hats.common.thread.ThreadHatsReader;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import us.ichun.mods.hats.client.gui.GuiHatSelection;
import us.ichun.mods.hats.client.render.RenderHat;
import us.ichun.mods.hats.common.core.HatHandler;
import us.ichun.mods.hats.common.thread.ThreadHatsReader;
import us.ichun.mods.ichunutil.common.module.tabula.client.formats.ImportList;
import us.ichun.mods.ichunutil.common.module.tabula.client.model.ModelTabula;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;

import java.io.File;
import java.util.HashMap;

public class ClientProxy extends CommonProxy
{

    @Override
    public void initRenderersAndTextures()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityHat.class, new RenderHat());
    }

    @Override
    public void initTickHandlers()
    {
        super.initTickHandlers();
        tickHandlerClient = new TickHandlerClient();
        FMLCommonHandler.instance().bus().register(tickHandlerClient);
    }

    @Override
    public void getHatsAndOpenGui()
    {
        ((Thread)new ThreadHatsReader(HatHandler.hatsFolder, false, true)).start();
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
        //		FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiTradeWindow("Kihira"));
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
            }
        }
    }

    public static HashMap<String, ModelTabula> models = new HashMap<String, ModelTabula>();

}
