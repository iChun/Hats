package me.ichun.mods.hats.client.core;

import me.ichun.mods.hats.common.core.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import me.ichun.mods.hats.client.gui.GuiHatSelection;
import me.ichun.mods.hats.common.core.HatHandler;
import me.ichun.mods.hats.common.thread.ThreadHatsReader;
import me.ichun.mods.ichunutil.common.module.tabula.client.formats.ImportList;
import me.ichun.mods.ichunutil.common.module.tabula.client.model.ModelTabula;
import me.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;

import java.io.File;
import java.util.HashMap;

public class ClientProxy extends CommonProxy
{
    @Override
    public void initTickHandlers()
    {
        super.initTickHandlers();
        tickHandlerClient = new TickHandlerClient();
        MinecraftForge.EVENT_BUS.register(tickHandlerClient);
    }

    @Override
    public void getHatsAndOpenGui()
    {
        System.out.println("Open heads GUI");
        (new ThreadHatsReader(HatHandler.hatsFolder, false, true)).start();
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
        FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().player, new GuiHatSelection(Minecraft.getMinecraft().player));
        //		FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().player, new GuiTradeWindow("Kihira"));
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

    public static HashMap<String, ModelTabula> models = new HashMap<String, ModelTabula>();

}
