package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.toast.Toast;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.ichunutil.common.network.PacketDataFragment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PacketHatFragment extends PacketDataFragment
{
    public String origin;
    public String md5;

    public PacketHatFragment(){}

    public PacketHatFragment(String fileName, int packetTotal, int packetNumber, byte[] data, String origin, String md5)
    {
        super(fileName, packetTotal, packetNumber, data);
        this.origin = origin;
        this.md5 = md5;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeString(origin);
        buf.writeString(md5);
        super.writeTo(buf);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        origin = readString(buf);
        md5 = readString(buf);
        super.readFrom(buf);
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            if(Hats.configServer.allowFileTransfer)
            {
                byte[] data = process(context.getDirection().getReceptionSide());
                if(data != null) //we have all the fragments
                {
                    try
                    {
                        Path saveDir = HatResourceHandler.getHatsDir().resolve("Received").resolve(origin).resolve(md5);
                        if(!Files.exists(saveDir)) Files.createDirectories(saveDir);

                        File hatFile = new File(saveDir.toFile(), fileName);
                        FileOutputStream fos = new FileOutputStream(hatFile);
                        fos.write(data);
                        fos.close();

                        HatResourceHandler.loadSingularHat(hatFile);
                        HatHandler.allocateHatPools();

                        if(FMLEnvironment.dist.isClient())
                        {
                            handleClient();
                        }
                    }
                    catch(Exception e)
                    {
                        Hats.LOGGER.error("Error writing received hat {} from {}", fileName, origin);
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                Hats.LOGGER.warn("Received file fragment from {} despite config being disabled.", origin);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient()
    {
        Hats.eventHandlerClient.resetSyncToast();

        Minecraft.getInstance().getToastGui().add(new Toast(new TranslationTextComponent("hats.toast.sync.receivedHat"), new StringTextComponent(fileName.substring(0, fileName.length() - 4)), 2));

        if(Minecraft.getInstance().currentScreen instanceof WorkspaceHats)
        {
            ((WorkspaceHats)Minecraft.getInstance().currentScreen).refreshHats();
        }
    }
}
