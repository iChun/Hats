package me.ichun.mods.hats.common.world;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HatsSavedData extends WorldSavedData
{
    public static final String ID = "HatsSave";
    public HashMap<UUID, PlayerHatData> playerHats = new HashMap<>();

    public HatsSavedData()
    {
        super(ID);
    }

    @Override
    public void read(CompoundNBT tag)
    {
        playerHats.clear();

        int count = tag.getInt("count");
        for(int i = 0; i < count; i++)
        {
            PlayerHatData playerData = new PlayerHatData();
            playerData.read(tag.getCompound("hats_" + i));

            playerHats.put(playerData.owner, playerData);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        tag.putInt("count", playerHats.size());

        int i = 0;
        for(Map.Entry<UUID, PlayerHatData> entry : playerHats.entrySet())
        {
            tag.put("hats_" + i, entry.getValue().write(new CompoundNBT()));
            i++;
        }

        return tag;
    }

    public static class PlayerHatData
    {
        public UUID owner;
        public ArrayList<HatPart> hatParts = new ArrayList<>();

        public PlayerHatData(){}

        public PlayerHatData(UUID owner)
        {
            this.owner = owner;
        }

        public void read(CompoundNBT tag)
        {
            hatParts.clear();

            owner = tag.getUniqueId("owner");
            int count = tag.getInt("partCount");

            for(int i = 0; i < count; i++)
            {
                CompoundNBT hatTag = tag.getCompound("hat_" + i);
                HatPart part = new HatPart();
                part.read(hatTag);

                if(!part.name.isEmpty())
                {
                    hatParts.add(part);
                }
            }
        }

        public CompoundNBT write(CompoundNBT tag)
        {
            tag.putUniqueId("owner", owner);
            tag.putInt("partCount", hatParts.size());

            for(int i = 0; i < hatParts.size(); i++)
            {
                HatPart part = hatParts.get(i);

                tag.put("hat_" + i, part.write(new CompoundNBT()));
            }

            return tag;
        }
    }

    public static class HatPart
    {
        public String name;
        public int count;
        public ArrayList<HatPart> hatParts = new ArrayList<>(); //yay infinite recursion

        public HatPart(){}

        public HatPart(String s)
        {
            name = s;
            count = 1;
        }

        public void read(CompoundNBT tag)
        {
            name = tag.getString("name");
            count = tag.getInt("count");

            int count = tag.getInt("partCount");

            for(int i = 0; i < count; i++)
            {
                CompoundNBT partTag = tag.getCompound("part_" + i);

                HatPart part = new HatPart();
                part.read(partTag);

                if(!part.name.isEmpty())
                {
                    hatParts.add(part);
                }
            }
        }

        public CompoundNBT write(CompoundNBT tag)
        {
            tag.putString("name", name);
            tag.putInt("count", count);

            tag.putInt("partCount", hatParts.size());

            for(int i = 0; i < hatParts.size(); i++)
            {
                HatPart part = hatParts.get(i);

                tag.put("part_" + i, part.write(new CompoundNBT()));
            }

            return tag;
        }
    }
}
