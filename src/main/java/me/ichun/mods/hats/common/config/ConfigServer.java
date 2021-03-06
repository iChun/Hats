package me.ichun.mods.hats.common.config;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.EnumRarity;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class ConfigServer extends ConfigBase
{
    @CategoryDivider(name = "hatRandomisation")
    public String randSeed = ""; //An empty string denotes a seed that is not set yet.

    @Prop(min = 0.0D, max = 1.0D)
    public double hatChance = 0.1D; //10% chance

    public List<String> disabledMobs = new ArrayList<>();

    @Prop(validator = "numbersOnly")
    public List<String> rarityWeight = new ArrayList<String>(){{
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

    public double peripheralCostMultiplier = 0.4D;

    public double salesCostMultiplier = 10D;

    public double bossHatChanceBonus = 0.1D;

    public double bossRarityBonus = 0.2D;


    @CategoryDivider(name = "others")
    public boolean userSubmissionsRequireApproval = true;

    //======================================================//

    public transient ArrayList<Double> rarityMeasure = new ArrayList<>();
    public transient ArrayList<Double> rarityIndividual = new ArrayList<>();

    public transient EnumMap<EnumRarity, Integer> coinByRarity = new EnumMap<>(EnumRarity.class);

    public ConfigServer()
    {
        super(ModLoadingContext.get().getActiveContainer().getModId() + "-server.toml");
    }

    @Override
    public void onConfigLoaded()
    {
        if(randSeed.isEmpty())
        {
            randSeed = RandomStringUtils.randomAscii(Project.IDENTIFIER_LENGTH);

            save();
        }

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

        double stack = 0;
        for(Integer weight : weights)
        {
            stack += (double)weight;
            rarityIndividual.add(weight / (double)total);
            rarityMeasure.add(stack / total);
        }
        //Done calculating rarities

        //Calculate coin costs
        coinByRarity.clear();
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
            coinByRarity.put(rarities[i], costs.get(i));
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
                Integer.parseInt((String)o);
                return true;
            }
            catch(NumberFormatException e)
            {
                return false;
            }
        }
        return false;
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
