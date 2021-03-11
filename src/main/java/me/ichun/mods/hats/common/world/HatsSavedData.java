package me.ichun.mods.hats.common.world;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class HatsSavedData extends WorldSavedData
{
    public static final String ID = "hats_save";
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
            implements Comparable<HatPart>
    {
        @CapabilityInject(HatPart.class)
        public static Capability<HatPart> CAPABILITY_INSTANCE;
        public static final ResourceLocation CAPABILITY_IDENTIFIER = new ResourceLocation("hats", "capability_hat");

        public String name = "";
        public int count = -1;
        public boolean isFavourite;
        public boolean isNew;
        public boolean isShowing;
        public float[] colouriser = new float[] { 0F, 0F, 0F, 0F }; //0 0 0 0 = no change to colours. goes up to 1 1 1 1 for black & invisible
        public ArrayList<HatPart> hatParts = new ArrayList<>(); //yay infinite recursion

        public HatPart(){}

        public HatPart(String s)
        {
            name = s;
            count = 1;
        }

        public HatPart setNew()
        {
            isNew = true;
            return this;
        }

        public void copy(HatPart part)
        {
            name = part.name;
            count = part.count;
            isFavourite = part.isFavourite;
            isShowing = part.isShowing;
            colouriser = part.colouriser.clone();

            hatParts.clear();
            for(HatPart hatPart : part.hatParts)
            {
                hatParts.add(hatPart.createCopy());
            }
        }

        public HatPart createCopy()
        {
            HatPart part = new HatPart();
            part.copy(this);
            return part;
        }

        public boolean isAHat()
        {
            return !name.isEmpty() && count >= 0;
        }

        public boolean hasNew()
        {
            boolean newStuff = isNew;
            if(!isNew)
            {
                for(HatPart hatPart : hatParts)
                {
                    newStuff = newStuff | hatPart.hasNew();
                }
            }
            return newStuff;
        }

        public boolean add(HatPart part)
        {
            if(!name.isEmpty() && name.equals(part.name)) //we are the same my buddy, we are the same my friend.
            {
                count += part.count;

                ArrayList<HatPart> partParts = new ArrayList<>(part.hatParts);
                for(HatPart hatPart : hatParts) //look for matching accessories
                {
                    for(int i = partParts.size() - 1; i >= 0; i--)
                    {
                        if(hatPart.add(partParts.get(i))) //if it matches
                        {
                            partParts.remove(i);
                            break;
                        }
                    }
                }

                hatParts.addAll(partParts); //add the accessories that don't match

                return true;
            }
            return false;
        }

        public boolean isIdenticalCustomisation(HatPart part)
        {
            if(name.equals(part.name) && isFavourite == part.isFavourite && isShowing == part.isShowing && Arrays.equals(colouriser, part.colouriser) && hatParts.size() == part.hatParts.size())
            {
                //compare the hatParts
                for(HatPart hatPart : hatParts)
                {
                    boolean foundTwin = false;

                    for(HatPart hatPart1 : part.hatParts)
                    {
                        if(hatPart.isIdenticalCustomisation(hatPart1)) //compares as well
                        {
                            foundTwin = true;
                            break;
                        }
                    }

                    if(!foundTwin)
                    {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        public void read(CompoundNBT tag)
        {
            name = tag.getString("name");
            count = tag.getInt("count");
            isFavourite = tag.getBoolean("isFavourite");

            isNew = tag.getBoolean("isNew");

            isShowing = tag.getBoolean("isShowing");

            colouriser = new float[] { tag.getFloat("clrR"), tag.getFloat("clrG"), tag.getFloat("clrB"), tag.getFloat("clrA") };

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
            tag.putBoolean("isFavourite", isFavourite);

            tag.putBoolean("isNew", isNew);

            tag.putBoolean("isShowing", isShowing);

            tag.putFloat("clrR", colouriser[0]);
            tag.putFloat("clrG", colouriser[1]);
            tag.putFloat("clrB", colouriser[2]);
            tag.putFloat("clrA", colouriser[3]);

            tag.putInt("partCount", hatParts.size());

            for(int i = 0; i < hatParts.size(); i++)
            {
                HatPart part = hatParts.get(i);

                tag.put("part_" + i, part.write(new CompoundNBT()));
            }

            return tag;
        }

        @Override
        public int compareTo(HatPart o)
        {
            return name.compareTo(o.name);
        }


        public static class CapProvider implements ICapabilitySerializable<CompoundNBT>
        {
            private final HatPart hatPart;
            private final LazyOptional<HatPart> optional;

            public CapProvider(HatPart hatPart)
            {
                this.hatPart = hatPart;
                this.optional = LazyOptional.of(() -> hatPart);
            }

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
            {
                if(cap == CAPABILITY_INSTANCE)
                {
                    return optional.cast();
                }
                return LazyOptional.empty();
            }

            @Override
            public CompoundNBT serializeNBT()
            {
                return hatPart.write(new CompoundNBT());
            }

            @Override
            public void deserializeNBT(CompoundNBT nbt)
            {
                hatPart.read(nbt);
            }
        }
    }
}
