package me.ichun.mods.hats.common.config;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import net.minecraftforge.fml.ModLoadingContext;

import javax.annotation.Nonnull;

public class ConfigCommon extends ConfigBase
{
    @CategoryDivider(name = "general")
    public boolean attemptToFixOldHats = true; //TODO not used. Update localisation

    //TODO allow file transfer

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
