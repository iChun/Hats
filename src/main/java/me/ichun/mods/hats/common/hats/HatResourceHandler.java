package me.ichun.mods.hats.common.hats;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.common.module.tabula.formats.ImportList;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class HatResourceHandler
{
    //Make it concurrent just in case we need to reload hats on client whilst integrated server is accessing?? Just in case
    public static final ConcurrentHashMap<String, HatInfo> HATS = new ConcurrentHashMap<>(); //Our reliance on Tabula is staggering.
    private static final ArrayList<HatInfo> HAT_ACCESSORIES = new ArrayList<>(); //Only used when loading all the hats.

    public static ArrayList<HatsSavedData.HatPart> HAT_PARTS = new ArrayList<>();

    private static Path hatsDir;
    private static boolean init;
    public static synchronized boolean init()
    {
        if(!init)
        {
            init = true;

            try
            {
                hatsDir = FMLPaths.MODSDIR.get().resolve(Hats.MOD_ID);
                if(!Files.exists(hatsDir)) Files.createDirectory(hatsDir);

                File extractedMarker = new File(hatsDir.toFile(), "files.extracted");
                if(!extractedMarker.exists()) //presume we haven't extracted anything yet
                {
                    InputStream in = Hats.class.getResourceAsStream("/hats.zip");
                    if(in != null)
                    {
                        ZipInputStream zipStream = new ZipInputStream(in);
                        ZipEntry entry = null;

                        while((entry = zipStream.getNextEntry()) != null)
                        {
                            File file = new File(hatsDir.toFile(), entry.getName());
                            if(file.exists() && file.length() > 3L)
                            {
                                continue;
                            }

                            if(entry.isDirectory())
                            {
                                if(!file.exists())
                                {
                                    file.mkdirs();
                                }
                            }
                            else
                            {
                                FileOutputStream out = new FileOutputStream(file);

                                byte[] buffer = new byte[8192];
                                int len;
                                while((len = zipStream.read(buffer)) != -1)
                                {
                                    out.write(buffer, 0, len);
                                }
                                out.close();
                            }
                        }
                        zipStream.close();
                    }

                    FileUtils.writeStringToFile(extractedMarker, "", StandardCharsets.UTF_8);
                }
            }
            catch(IOException e)
            {
                Hats.LOGGER.fatal("Error initialising Hats resources!");
                e.printStackTrace();
                return false;
            }
        }
        return init;
    }

    public static Path getHatsDir()
    {
        return hatsDir;
    }

    public static synchronized void loadAllHats() //TODO hat accessory clash layer
    {
        HATS.clear();
        HAT_ACCESSORIES.clear();

        int count = 0;
        File dir = getHatsDir().toFile();
        count += scourForHats(dir);

        int accessoryCount = HAT_ACCESSORIES.size();

        accessoriseHatInfos();

        HAT_PARTS = getAllHatsAsHatParts(1);

        Hats.LOGGER.info("Loaded {} files. {} are accessories.", count, accessoryCount);
    }

    private static int scourForHats(File dir)
    {
        int count = 0;
        File[] files = dir.listFiles();
        for(File file : files)
        {
            if(file.isDirectory())
            {
                count += scourForHats(file);
            }
            else if(file.getName().endsWith(".tbl") && readHat(file)) //tabula format
            {
                count++;
            }
        }
        return count;
    }

    public static boolean readHat(File file)
    {
        Project project = ImportList.createProjectFromFile(file);
        if(project == null)
        {
            Hats.LOGGER.warn("Error reading Tabula file: {}", file);
            return false;
        }
        else
        {
            if(project.tampered)
            {
                Hats.LOGGER.warn("This hat file was tampered (which will be loaded anyway): {}", file);
            }

            if(project.isOldTabula)
            {
                repairOldHat(file, project);
                Hats.LOGGER.warn("Loaded an old Tabula file. Updating to new Tabula & Hats format: {}", file);
            }

            //            if(file.getName().startsWith("(C) ")) //it's a contributor hat
            //            {
            //                parseMeta(file, project);
            //            }

            String hatName = file.getName().substring(0, file.getName().length() - 4);

            HatInfo hatInfo = createHatInfoFor(hatName, project);

            if(hatInfo.accessoryFor == null) //it is a base had
            {
                HATS.put(hatName, hatInfo);
            }
            else
            {
                HAT_ACCESSORIES.add(hatInfo);
            }

            return true;
        }
    }

    private static HatInfo createHatInfoFor(@Nonnull String name, @Nonnull Project project)
    {
        return new HatInfo(name, project);
    }

    public static void accessoriseHatInfos() //TODO sticky piston hat?
    {
        HashMap<String, ArrayList<HatInfo>> accessoriesByHat = new HashMap<>();
        for(HatInfo hatAccessory : HAT_ACCESSORIES)
        {
            accessoriesByHat.computeIfAbsent(hatAccessory.accessoryFor, k -> new ArrayList<>()).add(hatAccessory);
        }

        accessoriesByHat.forEach((hatName, hatInfos) -> {
            if(HATS.containsKey(hatName))
            {
                HATS.get(hatName).accessorise(hatInfos);

                if(!hatInfos.isEmpty())
                {
                    for(HatInfo orphan : hatInfos)
                    {
                        Hats.LOGGER.warn("We couldn't find the hat parent for {}", orphan.project.saveFile);
                    }
                }
            }
            else
            {
                for(HatInfo orphan : hatInfos)
                {
                    Hats.LOGGER.warn("We couldn't find the hat base for {}", orphan.project.saveFile);
                }
            }
        });

        HAT_ACCESSORIES.clear(); //we don't need you anymore
    }

    public static HatInfo getInfoAndSetToPart(HatsSavedData.HatPart part)
    {
        HatInfo hatInfo = HATS.get(part.name);
        if(hatInfo != null)
        {
            hatInfo.matchPart(part);
            return hatInfo;
        }
        return null;
    }

    public static ArrayList<HatsSavedData.HatPart> getAllHatsAsHatParts(int count)
    {
        ArrayList<HatsSavedData.HatPart> hatParts = new ArrayList<>();
        HATS.forEach((s, info) -> hatParts.add(info.getAsHatPart(count)));
        return hatParts;
    }

    public static ArrayList<HatsSavedData.HatPart> getAllHatPartsWithInventory(ArrayList<HatsSavedData.HatPart> inventory) //TODO sorting, loading all hat parts with user customisation, synching
    {
        ArrayList<HatsSavedData.HatPart> hatParts = getAllHatsAsHatParts(0);
        for(HatsSavedData.HatPart invPart : inventory)
        {
            for(HatsSavedData.HatPart hatPart : hatParts)
            {
                if(hatPart.add(invPart))
                {
                    break;
                }
            }
        }
        return hatParts;
    }

    private static void repairOldHat(File file, Project project)
    {
        project.name = file.getName().substring(0, file.getName().length() - 4);
        project.author = "";
        project.isOldTabula = false;
        project.isDirty = true;
        ArrayList<Project.Part> allParts = project.getAllParts();
        for(Project.Part part : allParts)
        {
            part.rotPY -= 16F; //we move the hat template up
        }
        project.save(file);
    }

    private static void parseMeta(File file, Project project)
    {
        boolean hasForcePool = false;
        boolean hasPool = false;
        for(int i = project.notes.size() - 1; i >= 0; i--)
        {
            String note = project.notes.get(i);
            if(note.startsWith("Hats:{"))//we found the meta
            {
                project.notes.remove(i);
                try
                {
                    JsonObject element = new JsonParser().parse(note.substring(5).trim()).getAsJsonObject();
                    if(element.has("uuid"))
                    {
                        project.notes.add("hats-contributor-uuid:" + element.get("uuid").getAsString());
                    }
                    if(element.has("isMiniMe"))
                    {
                        project.notes.add("hats-contributor-mini-me:" + element.get("isMiniMe").getAsBoolean());
                    }
                }
                catch(Throwable t)
                {
                    t.printStackTrace();
                }
            }
            if(note.startsWith("hats-rarity"))
            {
                hasForcePool = true;
            }
            if(note.startsWith("hats-pool"))
            {
                hasPool = true;
            }
        }
        if(!hasForcePool)
        {
            project.notes.add("hats-rarity:legendary");
        }
        if(!hasPool)
        {
            project.notes.add("hats-pool:contributors");
        }
        project.save(file);
    }
}
