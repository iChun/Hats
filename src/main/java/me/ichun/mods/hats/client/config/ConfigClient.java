package me.ichun.mods.hats.client.config;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;

public class ConfigClient extends ConfigBase
{
    @CategoryDivider(name = "clientOnly")
    @Prop(min = 0, max = 1000)
    public int maxHatRenders = 200;

    @Prop(min = 0.0D, max = 1.0D)
    public double hatChance = 0.1D;

    @Prop(min = 0, max = 5)
    public int hatUnlockString = 0;

    public boolean guiMinecraftStyle = true;

    @Prop(min = 1)
    public int guiAnimationTime = 10;

    public boolean forceGuiFallback = false;

    public boolean invisibleEntityInHatSelector = false;

    public ConfigClient()
    {
        super(ModLoadingContext.get().getActiveContainer().getModId() + "-client.toml");
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
