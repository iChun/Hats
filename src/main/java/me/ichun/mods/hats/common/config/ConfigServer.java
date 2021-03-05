package me.ichun.mods.hats.common.config;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ConfigServer extends ConfigBase
{
    @CategoryDivider(name = "hatRandomisation")
    @Prop
    public String randSeed = ""; //An empty string denotes a seed that is not set yet.

    @Prop
    public List<String> disabledMobs = new ArrayList<>();


    public ConfigServer()
    {
        super(ModLoadingContext.get().getActiveContainer().getModId() + "-server.toml");
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
