package me.ichun.mods.hats.common.config;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;

public class ConfigCommon extends ConfigBase
{
    @CategoryDivider(name = "general")
    @Prop
    public boolean attemptToFixOldHats = true; //TODO not used.

    public ConfigCommon()
    {
        super(ModLoadingContext.get().getActiveContainer().getModId() + "-common.toml");
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
}
