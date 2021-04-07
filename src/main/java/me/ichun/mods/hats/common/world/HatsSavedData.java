package me.ichun.mods.hats.common.world;

import me.ichun.mods.hats.common.hats.HatHandler;
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
        public int tokenCount;
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

            tokenCount = tag.getInt("tokenCount");

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

            tag.putInt("tokenCount", tokenCount);

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
        public float[] hsbiser = new float[] { 0F, 0F, 0F }; //0 0 0 = no change to colours. goes up to 1 1 1 HSB
        public boolean enchanted = false;
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
            isNew = part.isNew;
            colouriser = part.colouriser.clone();
            hsbiser = part.hsbiser.clone();
            enchanted = part.enchanted;

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

        public HatPart createRandom(Random random)
        {
            HatPart part = new HatPart();
            part.copy(this);

            part.randomize(random);

            return part;
        }

        public HatPart get(HatPart part)
        {
            if(part.name.equals(name))
            {
                return this;
            }

            for(HatPart hatPart : hatParts)
            {
                HatPart accPart = hatPart.get(part);
                if(accPart != null)
                {
                    return accPart;
                }
            }

            return null;
        }

        public void randomize(Random random)
        {
            this.count = 1;
            this.isShowing = true;

            for(int i = hatParts.size() - 1; i >= 0; i--)
            {
                if(random.nextBoolean()) //you LOST the coin toss
                {
                    hatParts.remove(i);
                }
            }

            for(HatPart hatPart : hatParts)
            {
                hatPart.randomize(random);
            }
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

        public HatPart setNoNew()
        {
            isNew = false;
            for(HatPart hatPart : hatParts)
            {
                hatPart.setNoNew();
            }
            return this;
        }

        public boolean hasFavourite()
        {
            boolean hasFav = isFavourite;
            if(!isFavourite)
            {
                for(HatPart hatPart : hatParts)
                {
                    hasFav = hasFav | hatPart.hasFavourite();
                }
            }
            return hasFav;
        }

        public HatPart setNoFavourite()
        {
            isFavourite = false;
            for(HatPart hatPart : hatParts)
            {
                hatPart.setNoFavourite();
            }
            return this;
        }

        public boolean add(HatPart part)
        {
            if(!name.isEmpty() && name.equals(part.name)) //we are the same my buddy, we are the same my friend.
            {
                count += part.count;

                if(count > 999999999)
                {
                    count = 999999999; //blame jackylam5
                }

                copyPersonalisation(part);

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

                for(HatPart newPart : partParts)
                {
                    newPart.setNew();
                }

                hatParts.addAll(partParts); //add the accessories that don't match

                return true;
            }
            return false;
        }

        public boolean minusByOne(HatPart part)
        {
            if(!name.isEmpty() && name.equals(part.name)) //we are the same my buddy, we are the same my friend.
            {
                count -= 1;

                copyPersonalisation(part);

                ArrayList<HatPart> partParts = new ArrayList<>(part.hatParts);
                for(HatPart hatPart : hatParts) //look for matching accessories
                {
                    for(int i = partParts.size() - 1; i >= 0; i--)
                    {
                        if(hatPart.minusByOne(partParts.get(i)) && hatPart.count <= 0) //if it matches
                        {
                            partParts.remove(i);
                            break;
                        }
                    }
                }

                return true;
            }
            return false;
        }

        public boolean hasFullPart(HatPart part)
        {
            if(!name.isEmpty() && name.equals(part.name) && count >= part.count)
            {
                boolean flag = true;

                for(HatPart hatPart : part.hatParts)
                {
                    boolean has = false;
                    for(HatPart hatPart1 : hatParts)
                    {
                        if(hatPart1.hasFullPart(hatPart))
                        {
                            has = true;
                            break;
                        }
                    }

                    if(!has)
                    {
                        flag = false;
                        break;
                    }
                }

                return flag;
            }
            return false;
        }

        public boolean has(@Nonnull String partName) //this better already be lower case
        {
            if(name.toLowerCase(Locale.ROOT).contains(partName))
            {
                return true;
            }
            else
            {
                for(HatPart accessory : hatParts)
                {
                    if(accessory.has(partName))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        public void setCountOfAllTo(int count)
        {
            this.count = count;
            for(HatPart hatPart : hatParts)
            {
                hatPart.setCountOfAllTo(count);
            }
        }

        public boolean copyPersonalisation(HatPart part)
        {
            if(name.equals(part.name))
            {
                isFavourite = part.isFavourite;
                isNew = part.isNew;
                isShowing = part.isShowing;
                colouriser = part.colouriser.clone();
                hsbiser = part.hsbiser.clone();
                enchanted = part.enchanted;

                for(HatPart hatPart : hatParts)
                {
                    for(HatPart hatPart1 : part.hatParts)
                    {
                        hatPart.copyPersonalisation(hatPart1);
                    }
                }
                return true;
            }
            return false;
        }

        public void hideAll()
        {
            isShowing = false;
            for(HatPart hatPart : hatParts)
            {
                hatPart.hideAll();
            }
        }

        public int accessoriesUnlocked()
        {
            int count = 0;
            count += hatParts.size();
            for(HatPart hatPart : hatParts)
            {
                count += hatPart.accessoriesUnlocked();
            }
            return count;
        }

        public void eventDay(int age, float partialTick)
        {
            if(colouriser[0] == 0F  && colouriser[1] == 0F && colouriser[2] == 0F)
            {
                HatHandler.RAND.setSeed(Math.abs(name.hashCode()));

                int ageR = 40 + HatHandler.RAND.nextInt(60);
                int ageG = 40 + HatHandler.RAND.nextInt(60);
                int ageB = 40 + HatHandler.RAND.nextInt(60);

                float offsetR = 180 * HatHandler.RAND.nextFloat();
                float offsetG = 180 * HatHandler.RAND.nextFloat();
                float offsetB = 180 * HatHandler.RAND.nextFloat();

                colouriser[0] = ((float)Math.sin(Math.toRadians(offsetR + ((age + partialTick) / (float)ageR) * 360F)) * 0.5F) + 0.5F;
                colouriser[1] = ((float)Math.sin(Math.toRadians(offsetG + ((age + partialTick) / (float)ageG) * 360F)) * 0.5F) + 0.5F;
                colouriser[2] = ((float)Math.sin(Math.toRadians(offsetB + ((age + partialTick) / (float)ageB) * 360F)) * 0.5F) + 0.5F;
            }

            for(HatPart hatPart : hatParts)
            {
                hatPart.eventDay(age, partialTick);
            }
        }

        public void read(CompoundNBT tag)
        {
            name = tag.getString("name");
            count = tag.getInt("count");
            isFavourite = tag.getBoolean("isFavourite");

            isNew = tag.getBoolean("isNew");

            isShowing = tag.getBoolean("isShowing");

            colouriser = new float[] { tag.getFloat("clrR"), tag.getFloat("clrG"), tag.getFloat("clrB"), tag.getFloat("clrA") };
            hsbiser = new float[] { tag.getFloat("hsbH"), tag.getFloat("hsbS"), tag.getFloat("hsbB") };
            enchanted = tag.getBoolean("enchanted");

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

            tag.putFloat("hsbH", hsbiser[0]);
            tag.putFloat("hsbS", hsbiser[1]);
            tag.putFloat("hsbB", hsbiser[2]);

            tag.putBoolean("enchanted", enchanted);

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

        public HatPart setModifier(HatPart modifier)
        {
            modify(modifier);
            return this;
        }

        public boolean modify(HatPart modifier) //returns true if this or any of it's children are the modifier
        {
            if(modifier == this)
            {
                return true;
            }

            if(name.equals(modifier.name))
            {
                copy(modifier);
                return true;
            }
            else
            {
                for(HatPart hatPart : hatParts)
                {
                    if(hatPart.modify(modifier))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        public void removeHiddenChildren()
        {
            hatParts.removeIf(part -> !part.isShowing);
            for(HatPart hatPart : hatParts)
            {
                hatPart.removeHiddenChildren();
            }
        }

        public void setBrightnessZero()
        {
            hsbiser[2] = 1F;

            for(HatPart hatPart : hatParts)
            {
                hatPart.setBrightnessZero();
            }
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
