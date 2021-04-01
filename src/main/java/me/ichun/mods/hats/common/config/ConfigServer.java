package me.ichun.mods.hats.common.config;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.EnumRarity;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

public class ConfigServer extends ConfigBase
{
    @CategoryDivider(name = "hatRandomisation")
    public String randSeed = ""; //An empty string denotes a seed that is not set yet.

    @Prop(min = 0.0D, max = 1.0D)
    public double hatChance = 0.1D; //10% chance

    @Prop(validator = "overrideChance")
    public List<String> entityOverrideChance = new ArrayList<>();

    public List<String> disabledMobs = new ArrayList<>();

    @Prop(validator = "numbersOnly")
    public List<String> rarityWeight = new ArrayList<String>(){{ //TODO GUI fallback fails in the END dimension?
        add("21"); //Common - White
        add("13"); //Uncommon - Green
        add("8"); //Rare - Blue
        add("5"); //Epic - Purple
        add("3"); //Legendary - Gold
    }};

    @Prop(validator = "numbersOnly")
    public List<String> rarityCost = new ArrayList<String>(){{
        add("10"); //Common - White
        add("20"); //Uncommon - Green
        add("40"); //Rare - Blue
        add("70"); //Epic - Purple
        add("110"); //Legendary - Gold
    }};

    @Prop(min = 0.0D, max = 1.0D)
    public double bossHatChanceBonus = 0.1D;

    @Prop(min = 0.0D, max = 1.0D)
    public double bossRarityBonus = 0.2D; //TODO new hat toast config

    //TODO reorganise the configs
    @CategoryDivider(name = "others")
    public boolean userSubmissionsRequireApproval = true; //TODO this config - update localisation

    @Prop(min = 0)
    public double accessoryCostMultiplier = 1.5D; //TODO easter egg rainbow hats

    @Prop(min = 0)
    public double salesCostMultiplier = 10D;

    @Prop(min = 0, max = 2)
    public int enabledGuiStyle = 2; //0 = disabled, 1 = simple, 2 = fancy

    public boolean enableCreativeModeHatHunting = false;

    @Prop(min = 0)
    public int hatEntityLifespan = 6000;

    public boolean hatLauncherReplacesPlayerHat = true;

    public boolean hatLauncherDoesNotRemoveHatFromInventory = true;

    //======================================================//

    public transient HashMap<ResourceLocation, Integer> entityOverrideChanceParsed = new HashMap<>();

    public transient ArrayList<Double> rarityMeasure = new ArrayList<>();
    public transient ArrayList<Double> rarityIndividual = new ArrayList<>();

    public transient EnumMap<EnumRarity, Integer> tokensByRarity = new EnumMap<>(EnumRarity.class);

    public ConfigServer()
    {
        super(ModLoadingContext.get().getActiveContainer().getModId() + "-server.toml");
    }

    @Override
    public synchronized void onConfigLoaded() //synchronised cause a client and server share it.
    {
        if(EffectiveSide.get().isClient() && (ServerLifecycleHooks.getCurrentServer() != null && ServerLifecycleHooks.getCurrentServer().isSinglePlayer())) //we're on single player, let's not reload the pool.
        {
            return;
        }

        if(randSeed.isEmpty())
        {
            randSeed = RandomStringUtils.randomAscii(Project.IDENTIFIER_LENGTH);

            save();
        }

        parseOverrideChance();

        EnumRarity[] rarities = EnumRarity.values();
        int totalRarities = rarities.length;

        //Calculate the rarities
        rarityMeasure.clear();
        rarityIndividual.clear();
        if(rarityWeight.size() != totalRarities)
        {
            Hats.LOGGER.warn("We don't have (exactly) " + totalRarities + " different rarity weights! Any shortage will be replaced with 0");
        }

        ArrayList<Integer> weights = new ArrayList<>();
        for(int i = 0; i < Math.min(rarityWeight.size(), totalRarities); i++)
        {
            weights.add(Integer.parseInt(rarityWeight.get(i)));
        }

        while(weights.size() < totalRarities)
        {
            weights.add(0);
        }

        int total = 0;
        for(Integer weight : weights)
        {
            total += weight;
        }

        if(total == 0)
        {
            Hats.LOGGER.warn("We can't have 0 weight! Forcing 1 weight to COMMON");
            weights.remove(0);
            weights.add(0, 1);
            total = 1;
        }

        double stack = 0;
        for(Integer weight : weights)
        {
            stack += (double)weight;
            rarityIndividual.add(weight / (double)total);
            rarityMeasure.add(stack / total);
        }
        //Done calculating rarities

        //Calculate coin costs
        tokensByRarity.clear();
        if(rarityCost.size() != totalRarities)
        {
            Hats.LOGGER.warn("We don't have (exactly) " + totalRarities + " different rarity costs! Any shortage will be replaced with 0");
        }

        ArrayList<Integer> costs = new ArrayList<>();
        for(int i = 0; i < Math.min(rarityCost.size(), totalRarities); i++)
        {
            costs.add(Integer.parseInt(rarityCost.get(i)));
        }

        while(costs.size() < totalRarities)
        {
            costs.add(0);
        }

        for(int i = 0; i < rarities.length; i++)
        {
            tokensByRarity.put(rarities[i], costs.get(i));
        }
        //Done calculating coin costs.

        HatHandler.allocateHatPools();
    }

    public boolean numbersOnly(Object o)
    {
        if(o instanceof String)
        {
            try
            {
                return Integer.parseInt((String)o) >= 0;
            }
            catch(NumberFormatException e)
            {
                return false;
            }
        }
        return false;
    }

    public boolean overrideChance(Object o)
    {
        if(o instanceof String)
        {
            String[] split = ((String)o).split(",");
            if(split.length == 2)
            {
                try
                {
                    new ResourceLocation(split[0]);
                    int chance = Integer.parseInt(split[1]);
                    return chance >= 0 && chance <= 100;
                }
                catch(ResourceLocationException | NumberFormatException ignored){}
            }
        }
        return false;
    }

    public void parseOverrideChance()
    {
        entityOverrideChanceParsed.clear();

        for(String s : entityOverrideChance)
        {
            String[] split = s.split(",");
            entityOverrideChanceParsed.put(new ResourceLocation(split[0]), Integer.parseInt(split[1]));
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
        return ModConfig.Type.SERVER;
    }
}
