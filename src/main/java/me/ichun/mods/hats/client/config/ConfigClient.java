package me.ichun.mods.hats.client.config;

import com.google.common.base.Splitter;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.sort.HatSorter;
import me.ichun.mods.hats.common.hats.sort.SortHandler;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ConfigClient extends ConfigBase
{
    @CategoryDivider(name = "clientOnly")
    @Prop(min = 0, max = 1000)
    public int maxHatRenders = 200;

    @Prop(min = 0.0D, max = 1.0D)
    public double hatChance = 0.1D;

    @Prop(min = 0, max = 5)
    public int hatUnlockString = 0;

    @Prop(min = 0, max = 2)
    public int guiMinecraftStyle = 2;

    @Prop(min = 1)
    public int guiAnimationTime = 10;

    public boolean shownTutorial = false;

    public boolean newHatToast = true;

    public boolean forceRenderToasts = true;

    public boolean forceGuiFallback = false;

    public boolean disableHatNameRenderInHatSelector = false;

    public boolean invisibleEntityInHatSelector = false;

    @Prop(min = 1)
    public int hatLauncherRandomHatSpeed = 5;

    public List<String> filterSorterConfig = new ArrayList<String>(){{
        add("filterUndiscovered");
        add("sorterFavourite");
        add("sorterRarity:inverse");
        add("sorterAlphabetical");
    }};

    //======================================================//

    public transient final static Splitter ON_COLON = Splitter.on(":").trimResults().omitEmptyStrings();

    public transient ArrayList<HatSorter> filterSorters = new ArrayList<>();

    public ConfigClient()
    {
        super(ModLoadingContext.get().getActiveContainer().getModId() + "-client.toml");
    }

    @Override
    public void onConfigLoaded()
    {
        filterSorters.clear();

        for(String s : filterSorterConfig)
        {
            List<String> split = ON_COLON.splitToList(s);
            if(!split.isEmpty() && SortHandler.SORTERS.containsKey(split.get(0)))
            {
                try
                {
                    HatSorter sorter = SortHandler.SORTERS.get(split.get(0)).newInstance();
                    if(split.size() >= 2)
                    {
                        sorter.isInverse = split.get(1).equals("inverse");
                    }

                    filterSorters.add(sorter);
                }
                catch(InstantiationException | IllegalAccessException e)
                {
                    Hats.LOGGER.error("Error creating known sorter type: {}", split.get(0));
                    e.printStackTrace();
                }
            }
        }
    }

    @Nonnull
    @Override
    public String getModId()
    {
        return Hats.MOD_ID;
    }

    @Nonnull
    @Override
    public String getConfigName()
    {
        return Hats.MOD_NAME;
    }

    @Nonnull
    @Override
    public ModConfig.Type getConfigType()
    {
        return ModConfig.Type.CLIENT;
    }
}
