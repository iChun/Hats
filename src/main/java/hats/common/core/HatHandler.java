package hats.common.core;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hats.api.RenderOnEntityHelper;
import hats.common.Hats;
import hats.common.packet.PacketHatFragment;
import hats.common.packet.PacketPing;
import hats.common.packet.PacketRequestHat;
import hats.common.packet.PacketString;
import ichun.common.core.network.PacketHandler;
import ichun.common.core.techne.TC2Info;
import ichun.common.core.util.MD5Checksum;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HatHandler
{

    public static boolean isHatReadable(File file)
    {
        if(file.getName().endsWith(".tc2"))
        {
            return TC2Info.readTechneFile(file) != null;
        }
        return false;
    }

    public static boolean readHatFromFile(File file)
    {
        return readHatFromFile(file, false);
    }

    public static boolean readHatFromFile(File file, boolean category)
    {
        String md5 = MD5Checksum.getMD5Checksum(file);

        if(HatHandler.checksums.get(md5) == null)
        {
            HatHandler.checksums.put(md5, file);
        }
        else
        {
            if(!category)
            {
                Hats.console("Rejecting " + file.getName() + "! Identical to " + HatHandler.checksums.get(md5).getName(), true);
            }
            else
            {
                return false;
            }
            return true;
        }

        if(file.getName().endsWith(".tc2"))
        {
            TC2Info info = TC2Info.readTechneFile(file);

            if(info == null)
            {
                Hats.console("Failed to load: " + file.getName() + " threw a generic exception! If no exception was printed, it's most likely the model has an invalid texture", true);
                return false;
            }

            if(Hats.config.getInt("safeLoad") == 1 && info.tampered)
            {
                Hats.console("Rejecting " + file.getName() + "! It contains files which are not XML or PNG files!", true);
                return false;
            }

            Hats.proxy.loadHatFile(file);
            return true;
        }
        return false;
    }

    public static int loadCategory(File dir)
    {
        int hatCount = 0;
        if(dir.isDirectory())
        {
            ArrayList<String> hatsToLoad = new ArrayList<String>();
            ArrayList<String> categoryHats = new ArrayList<String>();
            File[] files = dir.listFiles();
            for(File file : files)
            {
                if(file.getName().endsWith(".tc2"))
                {
                    String hatName = file.getName().substring(0, file.getName().length() - 4);
                    hatsToLoad.add(hatName.toLowerCase());
                    categoryHats.add(hatName);
                    for(Map.Entry<File, String> e : HatHandler.hatNames.entrySet())
                    {
                        String hatEntryName = e.getValue();
                        for(int i = hatsToLoad.size() - 1; i >= 0; i--)
                        {
                            String hatCategoryEntryName = hatsToLoad.get(i);
                            if(hatCategoryEntryName.equalsIgnoreCase(hatEntryName))
                            {
                                hatsToLoad.remove(i);
                                break;
                            }
                        }
                    }
                    if(!file.isDirectory() && HatHandler.readHatFromFile(file, true) && !dir.getName().equalsIgnoreCase("Favourites"))
                    {
                        hatCount++;
                    }
                }
            }

            categories.put(dir.getName(), categoryHats);
        }
        return hatCount;
    }

    public static void deleteHat(String hatName, boolean disable)
    {
        deleteHat(hatsFolder, hatName, disable);
    }

    public static void deleteHat(File dir, String hatName, boolean disable)
    {
        File[] files = dir.listFiles();
        for(File file : files)
        {
            if(!file.isDirectory() && file.getName().equalsIgnoreCase(hatName + ".tc2"))
            {
                if(disable)
                {
                    File disabledDir = new File(dir, "/Disabled");
                    if(!disabledDir.exists())
                    {
                        disabledDir.mkdirs();
                    }
                    File disabledName = new File(disabledDir, hatName + ".tc2");
                    if(disabledName.exists())
                    {
                        disabledName.delete();
                    }
                    if(!file.renameTo(disabledName))
                    {
                        Hats.console("Failed to disable hat: " + file.getName());
                    }
                }
                else
                {
                    if(!file.delete())
                    {
                        Hats.console("Failed to delete hat: " + file.getName());
                    }
                }
                break;
            }
        }
        files = dir.listFiles();
        for(File file : files)
        {
            if(file.isDirectory() && !file.getName().equalsIgnoreCase("Disabled"))
            {
                deleteHat(file, hatName, disable);
            }
        }
    }

    public static boolean isInFavourites(String hatName)
    {
        return isInCategory(hatName, "Favourites");
    }

    public static boolean isInCategory(String hatName, String category)
    {
        if(category.equalsIgnoreCase("Contributors"))
        {
            return false;
        }
        ArrayList<String> favs = categories.get(category);
        if(favs != null)
        {
            for(String s : favs)
            {
                if(s.equalsIgnoreCase(hatName))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isContributor(String hatName)
    {
        ArrayList<String> favs = categories.get("Contributors");
        if(favs != null)
        {
            for(String s : favs)
            {
                if(s.equalsIgnoreCase(hatName))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addToCategory(String hatName, String category)
    {
        ArrayList<String> favs = categories.get(category);
        if(favs != null)
        {
            boolean contained = false;
            for(int i = favs.size() - 1; i >= 0; i--)
            {
                String fav = favs.get(i);
                if(fav.equalsIgnoreCase(hatName))
                {
                    contained = true;
                    break;
                }
            }
            if(!contained)
            {
                for(Map.Entry<File, String> e : hatNames.entrySet())
                {
                    if(hatName.toLowerCase().equalsIgnoreCase(e.getValue()))
                    {
                        File favFile = new File(hatsFolder, "/" + category + "/" + hatName + ".tc2");

                        InputStream inStream = null;
                        OutputStream outStream = null;

                        try
                        {
                            inStream = new FileInputStream(e.getKey());
                            outStream = new FileOutputStream(favFile);

                            byte[] buffer = new byte[1024];

                            int length;

                            while ((length = inStream.read(buffer)) > 0)
                            {
                                outStream.write(buffer, 0, length);
                            }
                        }
                        catch(Exception e1){}

                        try
                        {
                            if(inStream != null)
                            {
                                inStream.close();
                            }
                        }
                        catch(IOException e1){}
                        try
                        {
                            if(outStream != null)
                            {
                                outStream.close();
                            }
                        }
                        catch(IOException e1){}

                        favs.add(hatName);
                        break;
                    }
                }
            }
        }
    }

    public static void removeFromCategory(String hatName, String category)
    {
        ArrayList<String> favs = categories.get(category);
        if(favs != null)
        {
            for(int i = favs.size() - 1; i >= 0; i--)
            {
                String fav = favs.get(i);
                if(fav.equalsIgnoreCase(hatName))
                {
                    File favFile = new File(hatsFolder, "/" + category +"/" + hatName + ".tc2");
                    File hatFile = new File(hatsFolder, hatName + ".tc2");
                    if(!hatFile.exists())
                    {
                        favFile.renameTo(hatFile);
                    }
                    else
                    {
                        favFile.delete();
                    }
                    favs.remove(i);
                    break;
                }
            }
        }
    }

    public static void requestHat(String name, EntityPlayer player)
    {
        if(player != null)
        {
            PacketHandler.sendToPlayer(Hats.channels, new PacketRequestHat(name), player);
        }
        else
        {
            PacketHandler.sendToServer(Hats.channels, new PacketRequestHat(name));
        }
    }

    public static void receiveHatData(String hatName, byte packets, byte packetNumber, byte[] byteValues, EntityPlayer player, boolean isServer)
    {
        if(Hats.config.getInt("allowReceivingOfHats") != 1)
        {
            return;
        }
        try
        {
            ArrayList<byte[]> byteArray = hatParts.get(hatName);
            if(byteArray == null)
            {
                byteArray = new ArrayList<byte[]>();

                hatParts.put(hatName, byteArray);

                for(int i = 0; i < packets; i++)
                {
                    byteArray.add(new byte[0]);
                }
            }

            byteArray.set(packetNumber, byteValues);

            boolean hasAllInfo = true;

            for(int i = 0; i < byteArray.size(); i++)
            {
                byte[] byteList = byteArray.get(i);
                if(byteList.length == 0)
                {
                    hasAllInfo = false;
                }
            }

            if(hasAllInfo)
            {
                File file = new File(hatsFolder, hatName + ".tc2");

                if(file.exists())
                {
                    Hats.console(file.getName() + " already exists! Will not save.");
                    return;
                }

                FileOutputStream fis = new FileOutputStream(file);

                for(int i = 0; i < byteArray.size(); i++)
                {
                    byte[] byteList = byteArray.get(i);
                    fis.write(byteList);
                }

                fis.close();

                String md5 = MD5Checksum.getMD5Checksum(file);

                boolean newHat = HatHandler.checksums.get(md5) == null;

                if(readHatFromFile(file))
                {
                    if(isServer)
                    {
                        ArrayList<String> queuedLists = queuedHats.get(hatName.toLowerCase());
                        if(queuedLists != null)
                        {
                            queuedHats.remove(hatName);
                            for(String name : queuedLists)
                            {
                                EntityPlayer player1 = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(name);
                                if(player1 != null)
                                {
                                    sendHat(hatName, player1);
                                }
                            }
                        }

                        if(newHat)
                        {
                            Hats.console("Received " + file.getName() + " from " + player.getCommandSenderName());
                        }
                        else
                        {
                            Hats.proxy.remap(file.getName().substring(0, file.getName().length() - 4).toLowerCase(), HatHandler.checksums.get(md5).getName().substring(0, HatHandler.checksums.get(md5).getName().length() - 4).toLowerCase());
                            Hats.console("Deleting " + file.getName() + " from " + (isServer ? player.getCommandSenderName() : "server") + "! Duplicate hat file with different name. Remapping hat to original file.", true);
                            if(!file.delete())
                            {
                                Hats.console("Failed to delete file! We're doomed!", true);
                            }

                            HatInfo info = Hats.proxy.playerWornHats.get(player.getCommandSenderName());
                            Hats.proxy.playerWornHats.put(player.getCommandSenderName(), new HatInfo(HatHandler.checksums.get(md5).getName().substring(0, HatHandler.checksums.get(md5).getName().length() - 4).toLowerCase(), info.colourR, info.colourG, info.colourB, info.alpha));
                        }

                        Hats.proxy.sendPlayerListOfWornHats(player, false);
                    }
                    else
                    {
                        Hats.proxy.tickHandlerClient.requestedHats.remove(hatName.toLowerCase());

                        if(newHat)
                        {
                            Hats.console("Received " + file.getName() + " from server.");
                            HatHandler.repopulateHatsList();
                        }
                        else
                        {
                            Hats.proxy.remap(file.getName().substring(0, file.getName().length() - 4).toLowerCase(), HatHandler.checksums.get(md5).getName().substring(0, HatHandler.checksums.get(md5).getName().length() - 4).toLowerCase());
                            Hats.console("Deleting " + file.getName() + " from " + (isServer ? player.getCommandSenderName() : "server") + "! Duplicate hat file with different name. Remapping hat to original file.", true);
                            if(!file.delete())
                            {
                                Hats.console("Failed to delete file! We're doomed!", true);
                            }
                        }
                    }
                }
                else
                {
                    Hats.console("Deleting " + file.getName() + " from " + (isServer ? player.getCommandSenderName() : "server") + "! SafeLoad is on, and the Model file contains files which are not XML or PNG files.", true);
                    if(!file.delete())
                    {
                        Hats.console("Failed to delete file! We're doomed!", true);
                    }
                }
            }
        }
        catch(IOException e)
        {
        }
    }

    public static void sendHat(String hatName, EntityPlayer player)
    {
        if(Hats.config.getInt("allowSendingOfHats") != 1)
        {
            return;
        }

        File file = null;

        for(Entry<File, String> e : hatNames.entrySet())
        {
            if(e.getValue().equalsIgnoreCase(hatName))
            {
                file = e.getKey();
            }
        }

        if(file != null)
        {
            int fileSize = (int)file.length();

            if(fileSize > 250000)
            {
                Hats.console("Unable to send " + file.getName() + ". It is above the size limit!", true);
                return;
            }
            else if(fileSize == 0)
            {
                Hats.console("Unable to send " + file.getName() + ". The file is empty!", true);
                return;
            }

            Hats.console("Sending " + file.getName() + " to " + (player == null ? "the server" : player.getCommandSenderName()));

            try
            {
                FileInputStream fis = new FileInputStream(file);

                String hatFullName = file.getName().substring(0, file.getName().length() - 4);
                int packetsToSend = (int)Math.ceil((float)fileSize / 32000F);

                int packetCount = 0;
                while(fileSize > 0)
                {
                    byte[] fileBytes = new byte[fileSize > 32000 ? 32000 : fileSize];
                    fis.read(fileBytes);

                    if(player != null)
                    {
                        PacketHandler.sendToPlayer(Hats.channels, new PacketHatFragment(hatFullName, packetsToSend, packetCount, fileSize > 32000 ? 32000 : fileSize, fileBytes), player);
                    }
                    else
                    {
                        PacketHandler.sendToServer(Hats.channels, new PacketHatFragment(hatFullName, packetsToSend, packetCount, fileSize > 32000 ? 32000 : fileSize, fileBytes));
                    }

                    packetCount++;
                    fileSize -= 32000;
                }

                fis.close();
            }
            catch(IOException e)
            {
            }
        }
        else if(player != null)
        {
            ArrayList<String> queuedLists = queuedHats.get(hatName.toLowerCase());
            if(queuedLists == null)
            {
                queuedLists = new ArrayList<String>();
                queuedHats.put(hatName, queuedLists);
            }
            queuedLists.add(player.getCommandSenderName());
        }
    }

    public static boolean hasHat(String name)
    {
        if(name.equalsIgnoreCase(""))
        {
            return true;
        }
        for(Entry<File, String> e : hatNames.entrySet())
        {
            if(e.getValue().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }

    public static String getHatStartingWith(String name)
    {
        for(Entry<File, String> e : hatNames.entrySet())
        {
            if(e.getValue().toLowerCase().startsWith(name.toLowerCase()))
            {
                return e.getValue();
            }
        }
        return name;
    }

    public static ArrayList<String> getAllHats()
    {
        ArrayList<String> hatList = new ArrayList<String>();

        Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
        while(ite.hasNext())
        {
            Entry<File, String> e = ite.next();
            hatList.add(e.getValue());
        }
        return hatList;
    }

    public static ArrayList<String> getHatsWithWeightedContributors()
    {
        ArrayList<String> hatList = new ArrayList<String>();

        Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
        while(ite.hasNext())
        {
            Entry<File, String> e = ite.next();
            if(e.getValue().startsWith("(C)".toLowerCase()) && rand.nextFloat() > ((float)Hats.config.getInt("useRandomContributorHats") / 100F))
            {
                continue;
            }
            hatList.add(e.getValue());
        }
        return hatList;
    }

    public static EnumChatFormatting getHatRarityColour(String hat)
    {
        if(Hats.config.getSessionInt("hatGenerationSeed") == 0)
        {
            return EnumChatFormatting.WHITE;
        }
        float rarity = HatHandler.getHatRarity(hat);
        if(rarity < 1F/7F)
        {
            return EnumChatFormatting.AQUA;
        }
        else if(rarity < 2F/7F)
        {
            return EnumChatFormatting.GOLD;
        }
        else if(rarity < 3F/7F)
        {
            return EnumChatFormatting.YELLOW;
        }
        else if(rarity < 4F/7F)
        {
            return EnumChatFormatting.LIGHT_PURPLE;
        }
        else if(rarity < 5F/7F)
        {
            return EnumChatFormatting.BLUE;
        }
        else if(rarity < 6F/7F)
        {
            return EnumChatFormatting.DARK_GREEN;
        }
        else if(rarity < 7F/7F)
        {
            return EnumChatFormatting.WHITE;
        }
        return EnumChatFormatting.WHITE;
    }

    public static float getHatRarity(String hatName)
    {
        hatGen.setSeed(Hats.config.getSessionInt("hatGenerationSeed"));
        int hash = Math.abs(hatName.toLowerCase().hashCode());
        int rand = hatGen.nextInt(hash);
        return (float)rand / (float)hash;
    }

    public static HatInfo getRandomHatFromList(ArrayList<String> list, boolean withRarity)
    {
        if(list.size() == 1)
        {
            return new HatInfo(list.get(0), 255, 255, 255, 255);
        }
        else if(list.size() == 0)
        {
            return new HatInfo();
        }

        if(withRarity)
        {
            HatInfo hat = null;

            final int triesPerPass = 500;
            float randAmp = 1.0F;

            int tries = 0;

            while(hat == null)
            {
                String hatName = list.get(rand.nextInt(list.size()));
                float rarity = getHatRarity(hatName);
                if(rand.nextFloat() < rarity * randAmp)
                {
                    hat = new HatInfo(hatName, 255, 255, 255, 255);
                }

                tries++;
                if(tries >= triesPerPass)
                {
                    tries = 0;
                    randAmp += 0.05F;
                }
            }

            return hat;
        }
        else
        {
            return new HatInfo(list.get(rand.nextInt(list.size())), 255, 255, 255, 255);
        }
    }

    public static ArrayList<String> getAllHatNamesAsList()
    {
        ArrayList<String> hatNameList = new ArrayList<String>();

        Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
        while(ite.hasNext())
        {
            Entry<File, String> e = ite.next();
            String name = e.getKey().getName().substring(0, e.getKey().getName().length() - 4);
            hatNameList.add(name);
        }
        Collections.sort(hatNameList);

        return hatNameList;
    }

    public static String[] getAllHatsAsArray()
    {
        ArrayList<String> hatNameList = getAllHatNamesAsList();

        String[] hatNameArray = new String[hatNameList.size()];

        hatNameList.toArray(hatNameArray);

        return hatNameArray;
    }

    public static void unlockHat(EntityPlayer player, String hat)
    {
        if(player == null || hat == null || hat.isEmpty())
        {
            return;
        }
        TreeMap<String, Integer> hats = Hats.proxy.tickHandlerServer.getPlayerHatsList(player.getCommandSenderName());
        Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
        while(ite.hasNext())
        {
            Entry<File, String> e = ite.next();
            String name = e.getKey().getName().substring(0, e.getKey().getName().length() - 4);
            if(name.equalsIgnoreCase(hat))
            {
                Integer hatCount = hats.get(name);
                if(hatCount == null)
                {
                    hatCount = 1;
                    hats.put(name, hatCount);
                }
                else
                {
                    hats.put(name, hatCount + 1);
                }

                StringBuilder sb = new StringBuilder();
                for(Map.Entry<String, Integer> e1 : hats.entrySet())
                {
                    String hatName = getNameForHat(e1.getKey());
                    sb.append(hatName);
                    if(e1.getValue() > 1)
                    {
                        sb.append(">" + e1.getValue());
                    }
                    sb.append(":");
                }

                NBTTagCompound persistentTag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                persistentTag.setString("Hats_unlocked", sb.toString().length() > 0 ? sb.toString().substring(0, sb.toString().length() - 1) : sb.toString());
                player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);

                PacketHandler.sendToPlayer(Hats.channels, new PacketString(0, name), player);

                break;
            }
        }
    }

    public static String getNameForHat(String hat)
    {
        for(Entry<File, String> e2 : hatNames.entrySet())
        {
            if(e2.getValue().equalsIgnoreCase(hat))
            {
                return e2.getKey().getName().substring(0, e2.getKey().getName().length() - 4);
            }
        }
        return hat;
    }

    @SideOnly(Side.CLIENT)
    public static void reloadAndOpenGui()
    {
        repopulateHatsList();
        if(Hats.config.getSessionInt("playerHatsMode") == 3)
        {
            PacketHandler.sendToServer(Hats.channels, new PacketPing(0, false));
        }
        else if(Hats.config.getSessionInt("playerHatsMode") != 2)
        {
            Hats.proxy.openHatsGui();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void repopulateHatsList()
    {
        Hats.proxy.tickHandlerClient.availableHats.clear();
        Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
        while(ite.hasNext())
        {
            Entry<File, String> e = ite.next();
            Hats.proxy.tickHandlerClient.availableHats.put(e.getKey().getName().substring(0, e.getKey().getName().length() - 4), 1);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void populateHatsList(String s)
    {
        Hats.proxy.tickHandlerClient.availableHats.clear();

        String[] split = s.split(":");
        for(String hatNameWithCount : split)
        {
            String[] hatNameAndCount = hatNameWithCount.split(">");
            if(!hatNameAndCount[0].trim().isEmpty())
            {
                try
                {
                    Hats.proxy.tickHandlerClient.availableHats.put(hatNameAndCount[0].trim(), hatNameAndCount.length == 1 ? 1 : Integer.parseInt(hatNameAndCount[1]));
                }
                catch(Exception e)
                {
                    Hats.proxy.tickHandlerClient.availableHats.put(hatNameAndCount[0].trim(), 1);
                }
            }
        }

        Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
        while(ite.hasNext())
        {
            Entry<File, String> e = ite.next();
            String name = e.getKey().getName().substring(0, e.getKey().getName().length() - 4);
            if(isPlayersContributorHat(name, Minecraft.getMinecraft().thePlayer.getCommandSenderName()))
            {
                Hats.proxy.tickHandlerClient.availableHats.put(name, Hats.proxy.tickHandlerClient.availableHats.get(name) == null ? 1 : Hats.proxy.tickHandlerClient.availableHats.get(name) + 1);
            }
        }

        Hats.proxy.tickHandlerClient.serverHats = new HashMap<String, Integer>(Hats.proxy.tickHandlerClient.availableHats);
    }

    public static boolean isPlayersContributorHat(String hatName, String playerName)
    {
        return hatName.toLowerCase().startsWith("(c)") && hatName.toLowerCase().contains(playerName.toLowerCase())
                || hatName.equalsIgnoreCase("(C) iChun") && playerName.equalsIgnoreCase("ohaiiChun") //special casing for initial contrib hats.
                || hatName.equalsIgnoreCase("(C) Mr. Haz") && playerName.equalsIgnoreCase("damien95")
                || hatName.equalsIgnoreCase("(C) Fridgeboy") && playerName.equalsIgnoreCase("lacsap32");
    }

    public static boolean canMobHat(EntityLivingBase ent)
    {
        return !ent.isDead && !ent.isChild() && getRenderHelper(ent.getClass()) != null && getRenderHelper(ent.getClass()).canWearHat(ent);
    }

    public static RenderOnEntityHelper getRenderHelper(Class clz)
    {
        if(EntityLivingBase.class.isAssignableFrom(clz) && clz != EntityLivingBase.class)
        {
            RenderOnEntityHelper helper = CommonProxy.renderHelpers.get(clz);
            if(helper == null)
            {
                return getRenderHelper(clz.getSuperclass());
            }
            return helper;
        }
        return null;
    }

    public static boolean isMobSpawner(Class clz, Class callingClz)
    {
        if(!TileEntity.class.isAssignableFrom(clz))
        {
            return false;
        }
        Boolean bool = mobSpawners.get(clz);
        if(bool == null)
        {
            try
            {
                Field[] fields = clz.getDeclaredFields();
                for(Field field : fields)
                {
                    field.setAccessible(true);
                    if(MobSpawnerBaseLogic.class.isAssignableFrom(field.getType()))
                    {
                        bool = true;
                        mobSpawnerLogic.put(callingClz, field);
                        mobSpawners.put(callingClz, bool);
                        break;
                    }
                }
                if(bool == null)
                {
                    bool = clz.getSuperclass() == TileEntity.class ? false : isMobSpawner(clz.getSuperclass(), callingClz);
                    mobSpawners.put(callingClz, bool);
                }
            }
            catch(Exception e)
            {
                bool = false;
                mobSpawners.put(callingClz, bool);
            }
        }
        return bool;
    }

    public static MobSpawnerBaseLogic getMobSpawnerLogic(Class<? extends TileEntity> clz, TileEntity instance)
    {
        try
        {
            Field field = mobSpawnerLogic.get(clz);
            if(field != null)
            {
                field.setAccessible(true);
                Object obj = field.get(instance);
                if(obj instanceof MobSpawnerBaseLogic)
                {
                    return (MobSpawnerBaseLogic)obj;
                }
            }
        }
        catch(Exception e)
        {
        }
        return null;
    }

    public static boolean reloadingHats;

    public static File hatsFolder;

    public static HashMap<String, ArrayList<String>> queuedHats = new HashMap<String, ArrayList<String>>();

    public static HashMap<String, ArrayList<byte[]>> hatParts = new HashMap<String, ArrayList<byte[]>>();

    public static HashMap<File, String> hatNames = new HashMap<File, String>();

    public static HashMap<String, File> checksums = new HashMap<String, File>();

    public static HashMap<String, ArrayList<String>> categories = new HashMap<String, ArrayList<String>>();

    public static Random rand = new Random();

    public static Random hatGen = new Random();

    private static HashMap<Class<? extends TileEntity>, Boolean> mobSpawners = new HashMap<Class<? extends TileEntity>, Boolean>();
    private static HashMap<Class<? extends TileEntity>, Field> mobSpawnerLogic = new HashMap<Class<? extends TileEntity>, Field>();
}
