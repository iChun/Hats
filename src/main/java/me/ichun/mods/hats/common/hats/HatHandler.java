package me.ichun.mods.hats.common.hats;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.item.ItemHatLauncher;
import me.ichun.mods.hats.common.packet.PacketEntityHatDetails;
import me.ichun.mods.hats.common.packet.PacketHatLauncherInfo;
import me.ichun.mods.hats.common.packet.PacketNewHatPart;
import me.ichun.mods.hats.common.packet.PacketUpdateHats;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.head.HeadInfo;
import me.ichun.mods.ichunutil.common.item.DualHandedItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class HatHandler //Handles most of the server-related things.
{
    public static final EnumMap<EnumRarity, ArrayList<HatPool>> HAT_POOLS = new EnumMap<>(EnumRarity.class);
    public static final Random RAND = new Random();

    private static HatsSavedData saveData;

    public static synchronized void allocateHatPools() //Server and client shares the same pools //TODO loading method when receiving a hat from the server.
    {
        HAT_POOLS.clear();

        HashMap<String, HatPool> poolsByName = new HashMap<>();

        for(Map.Entry<String, HatInfo> entry : HatResourceHandler.HATS.entrySet())
        {
            String hatName = entry.getKey();
            HatInfo hatInfo = entry.getValue();

            HatPool pool;
            if(hatInfo.forcedPool != null)
            {
                pool = poolsByName.computeIfAbsent(hatInfo.forcedPool, k -> new HatPool());
            }
            else
            {
                pool = poolsByName.computeIfAbsent(hatName, k -> new HatPool());
            }

            pool.addHatToPool(hatInfo);

            if(hatInfo.forcedRarity != null)
            {
                pool.forcedRarity = hatInfo.forcedRarity;
            }
        }

        for(Map.Entry<String, HatPool> entry : poolsByName.entrySet())
        {
            HatPool pool = entry.getValue();
            if(pool.forcedRarity != null)
            {
                ArrayList<HatPool> hats = HAT_POOLS.computeIfAbsent(pool.forcedRarity, k -> new ArrayList<>());
                hats.add(pool);
                continue;
            }

            RAND.setSeed(Math.abs((Hats.configServer.randSeed + entry.getKey()).hashCode()) * 154041013L); //Chat contributed random

            EnumRarity rarity = getRarityForChance(RAND.nextDouble());
            ArrayList<HatPool> hats = HAT_POOLS.computeIfAbsent(rarity, k -> new ArrayList<>());
            for(HatInfo hatInfo : pool.hatsInPool)
            {
                hatInfo.setRarity(rarity);
            }
            hats.add(pool);
        }

        Hats.LOGGER.info("Allocated Hat Pools."); //TODO why are we being triggered three times?
    }

    public static EnumRarity getRarityForChance(double chance)
    {
        EnumRarity[] rarities = EnumRarity.values();
        for(int i = 0; i < Hats.configServer.rarityMeasure.size(); i++)
        {
            if(chance < Hats.configServer.rarityMeasure.get(i))
            {
                return rarities[i];
            }
        }
        return EnumRarity.COMMON;
    }

    public static double getHatChance(LivingEntity ent)
    {
        double chance = Hats.configServer.hatChance;
        HeadInfo<?> info = HeadHandler.getHelper(ent.getClass());
        if(info != null && info.isBoss)
        {
            chance += Hats.configServer.bossHatChanceBonus;
        }
        return chance;
    }

    public static boolean canWearHat(LivingEntity ent)
    {
        HeadInfo info = HeadHandler.getHelper(ent.getClass());
        if(info != null && !info.noTopInfo)
        {
            for(String disabledName : Hats.configServer.disabledMobs)
            {
                if(new ResourceLocation(disabledName).equals(ent.getType().getRegistryName()))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static HatsSavedData.HatPart getHatPart(LivingEntity ent)
    {
        return ent.getCapability(HatsSavedData.HatPart.CAPABILITY_INSTANCE).orElseThrow(() -> new IllegalArgumentException("Entity " + ent.getName().getUnformattedComponentText() + " has no hat capabilities"));
    }

    public static boolean hasBeenRandomlyAllocated(LivingEntity ent)
    {
        return getHatPart(ent).count >= 0;
    }

    public static void assignHat(LivingEntity ent)
    {
        HatsSavedData.HatPart hatPart = getHatPart(ent);

        RAND.setSeed(Math.abs((Hats.configServer.randSeed + ent.getUniqueID().toString()).hashCode()) * 425480085L); //Chat contributed random

        double chance = RAND.nextDouble();
        HeadInfo<?> info = HeadHandler.getHelper(ent.getClass());
        if(info != null && info.isBoss)
        {
            chance += Hats.configServer.bossRarityBonus;
        }
        EnumRarity rarity = getRarityForChance(chance);

        ArrayList<HatPool> hatPools = HAT_POOLS.get(rarity);
        HatPool pool = hatPools.get(RAND.nextInt(hatPools.size()));

        HatInfo hatInfo = pool.getRandomHat();
        hatPart.name = hatInfo.name;
        hatPart.count = 1;
        hatPart.isShowing = true;

        hatInfo.assignAccessoriesToPart(hatPart, ent);
    }

    public static void assignSpecificHat(LivingEntity ent, HatsSavedData.HatPart part)
    {
        HatsSavedData.HatPart hatPart = getHatPart(ent);
        if(part != null)
        {
            hatPart.copy(part);
        }
        else
        {
            hatPart.name = "";
            hatPart.count = 0;
        }
    }

    public static void assignNoHat(LivingEntity ent)
    {
        assignSpecificHat(ent, null);
    }

    public static void setSaveData(HatsSavedData data)
    {
        saveData = data;
    }

    private static void addNamesOfLackingParts(ArrayList<String> names, @Nullable HatsSavedData.HatPart source, @Nonnull HatsSavedData.HatPart target)
    {
        //ONLY USE THIS FOR WHEN WE ADD A NEW HAT!! WE MARK AS NEW!
        //We do it for this level's children first
        ArrayList<String> parts = new ArrayList<>();
        for(HatsSavedData.HatPart hatPart : target.hatParts)
        {
            parts.add(hatPart.name);
        }
        if(source != null)
        {
            for(HatsSavedData.HatPart part : source.hatParts)
            {
                parts.remove(part.name);
            }
        }
        names.addAll(parts);

        //look for the next level's pairs to compare.
        for(HatsSavedData.HatPart nextTarget : target.hatParts)
        {
            HatsSavedData.HatPart nextSource = null;
            if(source != null)
            {
                for(HatsSavedData.HatPart part : source.hatParts)
                {
                    if(part.name.equals(nextTarget.name))
                    {
                        nextSource = part;
                    }
                }
            }
            addNamesOfLackingParts(names, nextSource, nextTarget);
        }
    }

    public static void addHat(ServerPlayerEntity player, HatsSavedData.HatPart hatToAdd)
    {
        HatInfo info = HatResourceHandler.HATS.get(hatToAdd.name);
        if(info != null) //it's a valid hat
        {
            boolean foundBase = false; //if stays false, this is a new hat.

            //We're looking if this hat has been unlocked before.
            HatsSavedData.HatPart inventoryHat = null;
            HatsSavedData.PlayerHatData playerHatData = saveData.playerHats.computeIfAbsent(player.getGameProfile().getId(), k -> new HatsSavedData.PlayerHatData(player.getGameProfile().getId()));
            for(HatsSavedData.HatPart hatPart : playerHatData.hatParts)
            {
                if(hatToAdd.name.equals(hatPart.name))
                {
                    inventoryHat = hatPart;
                    foundBase = true;
                    break;
                }
            }

            //We can't find the hat, it has not been unlocked before.
            if(inventoryHat == null)
            {
                playerHatData.hatParts.add(inventoryHat = new HatsSavedData.HatPart(hatToAdd.name).setNew());
                inventoryHat.count = 0; //we are adding the added hat at the end.
            }

            ArrayList<String> names = new ArrayList<>();
            names.add(info.getDisplayName());

            ArrayList<String> accessoryNames = new ArrayList<>();
            addNamesOfLackingParts(accessoryNames, inventoryHat, hatToAdd);

            inventoryHat.add(hatToAdd);

            if(!accessoryNames.isEmpty())
            {
                for(String accessoryName : accessoryNames)
                {
                    String dispName = info.getDisplayNameFor(accessoryName);
                    if(dispName != null)
                    {
                        names.add("- " + dispName);
                    }
                }
            }

            if(!foundBase || !accessoryNames.isEmpty()) //there's something new
            {
                Hats.channel.sendTo(new PacketNewHatPart(!foundBase, hatToAdd, names), player);
            }

            Hats.channel.sendTo(new PacketUpdateHats(inventoryHat.write(new CompoundNBT()), false), player);

            saveData.markDirty();
        }
    }

    public static ArrayList<HatsSavedData.HatPart> getHatSource(PlayerEntity player)
    {
        ArrayList<HatsSavedData.HatPart> source = new ArrayList<>();
        if(HatHandler.useInventory(player))
        {
            source.addAll(getPlayerInventory(player));
        }
        else
        {
            for(HatsSavedData.HatPart hatPart : HatResourceHandler.HAT_PARTS)
            {
                source.add(hatPart.createCopy());
            }
            HatResourceHandler.combineLists(source, getPlayerInventory(player)); //combine all the hats with our personalisation
        }
        return source;
    }

    public static ArrayList<HatsSavedData.HatPart> getPlayerInventory(PlayerEntity player)
    {
        if(player.world.isRemote)
        {
            return Hats.eventHandlerClient.hatsInventory.hatParts;
        }
        else
        {
            return saveData.playerHats.computeIfAbsent(player.getGameProfile().getId(), k -> new HatsSavedData.PlayerHatData(player.getGameProfile().getId())).hatParts;
        }
    }

    public static void setPlayerHatCustomisation(ServerPlayerEntity player, ArrayList<HatsSavedData.HatPart> customisedHats, @Nullable HatsSavedData.HatPart hatChange)
    {
        if(hatChange != null)
        {
            setPlayerHat(player, hatChange);
        }

        if(!customisedHats.isEmpty())
        {
            HatsSavedData.PlayerHatData playerHatData = saveData.playerHats.computeIfAbsent(player.getGameProfile().getId(), k -> new HatsSavedData.PlayerHatData(player.getGameProfile().getId()));
            for(HatsSavedData.HatPart hatPart : playerHatData.hatParts)
            {
                for(int i = customisedHats.size() - 1; i >= 0; i--)
                {
                    HatsSavedData.HatPart customisedHat = customisedHats.get(i);
                    if(hatPart.copyPersonalisation(customisedHat))
                    {
                        customisedHats.remove(i);
                    }
                }
            }
            for(HatsSavedData.HatPart customisedHat : customisedHats) //these are hats we don't own.
            {
                customisedHat.setCountTo(0);
                playerHatData.hatParts.add(customisedHat);
            }
            saveData.markDirty();
        }
    }

    public static void setPlayerHat(ServerPlayerEntity player, HatsSavedData.HatPart part)
    {
        HatHandler.assignSpecificHat(player, part);
        HashMap<Integer, HatsSavedData.HatPart> deets = new HashMap<>();
        deets.put(player.getEntityId(), part);
        Hats.channel.sendTo(new PacketEntityHatDetails(deets), PacketDistributor.TRACKING_ENTITY.with(() -> player));
    }

    public static @Nonnull CompoundNBT getPlayerHatsNBT(PlayerEntity player)
    {
        if(saveData == null)
        {
            Hats.LOGGER.error("We're trying to get the save data for a player without having loaded the save! Player: {}", player.getName());
            Thread.dumpStack();
            return new CompoundNBT();
        }

        HatsSavedData.PlayerHatData playerHatData = saveData.playerHats.get(player.getGameProfile().getId());

        if(playerHatData == null)
        {
            playerHatData = new HatsSavedData.PlayerHatData(player.getGameProfile().getId());

            ArrayList<HatPool> hatPools = HAT_POOLS.get(EnumRarity.LEGENDARY);
            for(HatPool hatPool : hatPools)
            {
                if(hatPool.forcedRarity == EnumRarity.LEGENDARY) //likely our contributors.
                {
                    for(HatInfo hatInfo : hatPool.hatsInPool)
                    {
                        if(hatInfo.contributorUUID != null && hatInfo.contributorUUID.equals(player.getGameProfile().getId())) //AMAGA WE FOUND A CONTRIBUTOR
                        {
                            playerHatData.hatParts.add(new HatsSavedData.HatPart(hatInfo.name));//this already sets the count to 1.
                        }
                    }
                }
            }

            saveData.playerHats.put(player.getGameProfile().getId(), playerHatData);
        }

        return playerHatData.write(new CompoundNBT());
    }

    public static boolean useInventory(@Nonnull PlayerEntity player)
    {
        return !(player.isCreative() && !Hats.configServer.enableCreativeModeHatHunting);
    }


    // ItemStack based functions

    public static HatsSavedData.HatPart getHatPart(ItemStack is) //Please check that it's ItemHatLauncher first
    {
        return is.getCapability(HatsSavedData.HatPart.CAPABILITY_INSTANCE).orElseThrow(() -> new IllegalArgumentException("Item " + is.toString() + " has no hat capabilities"));
    }

    public static void setHatPart(ItemStack is, @Nonnull HatsSavedData.HatPart part)
    {
        getHatPart(is).copy(part);
    }

    public static HatsSavedData.HatPart getRandomHat(PlayerEntity player) //WE CONSUME THE HAT. Be mindful of use
    {
        HatsSavedData.HatPart part = null;

        ArrayList<HatsSavedData.HatPart> source = getHatSource(player);
        if(!source.isEmpty())
        {
            if(HatHandler.useInventory(player))
            {
                source = source.stream().filter(hatPart -> hatPart.count > 0).collect(Collectors.toCollection(ArrayList::new)); //remove the hats that have no count

                HatsSavedData.HatPart currentlyWearing = HatHandler.getHatPart(player);

                int tries = 0;
                while(tries++ < 10) // 10 should be enough
                {
                    HatsSavedData.HatPart oriHatPart = source.get(player.getRNG().nextInt(source.size()));
                    HatsSavedData.HatPart copyHatPart = oriHatPart.createCopy();
                    copyHatPart.minusByOne(currentlyWearing);

                    if(copyHatPart.count > 0) //this is a valid random hat
                    {
                        HatsSavedData.HatPart partToReturn = copyHatPart.createRandom(player.getRNG());
                        oriHatPart.minusByOne(partToReturn);

                        part = partToReturn;

                        saveData.markDirty();
                        break;
                    }
                }
            }
            else //DO NOT USE INVENTORY!!
            {
                part = source.get(player.getRNG().nextInt(source.size())).createRandom(player.getRNG()); //This creates a copy of
            }
        }
        return part;
    }

    public static void removeOneFromInventory(ServerPlayerEntity player, HatsSavedData.HatPart part)
    {
        ArrayList<HatsSavedData.HatPart> playerInventory = getPlayerInventory(player);
        for(HatsSavedData.HatPart hatPart : playerInventory)
        {
            if(hatPart.minusByOne(part))
            {
                Hats.channel.sendTo(new PacketUpdateHats(hatPart.write(new CompoundNBT()), false), player);

                saveData.markDirty();
                break;
            }
        }
    }

    public static void setHatLauncherCustomisation(ServerPlayerEntity player, HatsSavedData.HatPart newHat)
    {
        ItemStack is = DualHandedItem.getUsableDualHandedItem(player);
        if(is.getItem() instanceof ItemHatLauncher)
        {
            setHatPart(is, newHat);
            HatsSavedData.HatPart part = newHat.createCopy();
            part.count = player.getPrimaryHand() == DualHandedItem.getHandSide(player, is) ? 0 : 1;
            Hats.channel.sendTo(new PacketHatLauncherInfo(player.getEntityId(), part.write(new CompoundNBT())), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));
        }
    }

    public static boolean playerHasHat(PlayerEntity player, HatsSavedData.HatPart part)
    {
        if(useInventory(player))
        {
            ArrayList<HatsSavedData.HatPart> playerInventory = getPlayerInventory(player);
            for(HatsSavedData.HatPart hatPart : playerInventory)
            {
                if(hatPart.hasFullPart(part))
                {
                    return true;
                }
            }

            return false;
        }
        return true;
    }
}
