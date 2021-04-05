package me.ichun.mods.hats.common.packet;

import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowConfirmation;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


public class PacketHatsListResponse extends AbstractPacket
{
    public HashSet<String> serverMissing = new HashSet<>();
    public HashSet<String> clientMissing = new HashSet<>();

    public PacketHatsListResponse(){}

    public PacketHatsListResponse(HashSet<String> serverMissing, HashSet<String> clientMissing)
    {
        this.serverMissing = serverMissing;
        this.clientMissing = clientMissing;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(serverMissing.size());
        serverMissing.forEach(buf::writeString);
        buf.writeInt(clientMissing.size());
        clientMissing.forEach(buf::writeString);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        int count = buf.readInt();
        for(int i = 0; i < count; i++)
        {
            serverMissing.add(readString(buf));
        }
        count = buf.readInt();
        for(int i = 0; i < count; i++)
        {
            clientMissing.add(readString(buf));
        }
    }

    @Override
    public void process(NetworkEvent.Context context) //received by client
    {
        context.enqueueWork(this::handleClient);
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.currentScreen instanceof WorkspaceHats)
        {
            WorkspaceHats workspaceHats = (WorkspaceHats)mc.currentScreen;

            if(serverMissing.isEmpty() && clientMissing.isEmpty())
            {
                //we're synced up, yay!
                for(int i = 0; i < workspaceHats.windows.size(); i++)
                {
                    Window<?> window = workspaceHats.windows.get(i);
                    if(window instanceof WindowPopup)
                    {
                        workspaceHats.removeWindow(window);
                        break;
                    }
                }

                workspaceHats.popup(0.55D, 0.55D, w -> {}, I18n.format("hats.gui.window.management.sync.noDifferences"));
            }
            else
            {
                ArrayList<String> serverMissingList = new ArrayList<>(serverMissing);
                ArrayList<String> clientMissingList = new ArrayList<>(clientMissing);
                Collections.sort(serverMissingList);
                Collections.sort(clientMissingList);

                StringBuilder sb = new StringBuilder(I18n.format("hats.gui.window.management.sync.issue")).append("\n\n");
                if(!serverMissingList.isEmpty())
                {
                    sb.append(I18n.format("hats.gui.window.management.sync.serverMissing")).append("\n");

                    for(String s : serverMissingList)
                    {
                        sb.append(s);
                        sb.append("\n");
                    }

                    sb.append("\n");
                }

                if(!clientMissingList.isEmpty())
                {
                    sb.append(I18n.format("hats.gui.window.management.sync.clientMissing")).append("\n");

                    for(String s : clientMissingList)
                    {
                        sb.append(s);
                        sb.append("\n");
                    }

                    sb.append("\n");
                }

                sb.append(I18n.format("hats.gui.window.management.sync.syncReq"));

                WindowConfirmation.popup(workspaceHats, 0.8D, 0.8D, sb.toString(), w -> {
                    //Request each hat
                    Hats.channel.sendToServer(new PacketHatsList(clientMissing, true));

                    //Send each hat
                    for(String s : serverMissingList)
                    {
                        HatInfo info = HatResourceHandler.getInfoFromFullName(s);
                        if(info != null && info.project.saveFile != null)
                        {
                            try
                            {
                                final int maxFile = 31000; //smaller packet cause I'm worried about too much info carried over from the bloat vs hat info.

                                String origin = Minecraft.getInstance().getSession().getUsername();
                                String md5 = IOUtil.getMD5Checksum(info.project.saveFile);
                                byte[] data = Files.readAllBytes(info.project.saveFile.toPath());

                                String fileName = info.project.saveFile.getName();
                                int fileSize = data.length;

                                int packetsToSend = (int)Math.ceil((float)fileSize / (float)maxFile);

                                int packetCount = 0;
                                int offset = 0;
                                while(fileSize > 0)
                                {
                                    byte[] fileBytes = new byte[Math.min(fileSize, maxFile)];
                                    int index = 0;
                                    while(index < fileBytes.length) //from index 0 to 30999
                                    {
                                        fileBytes[index] = data[index + offset];
                                        index++;
                                    }

                                    Hats.channel.sendToServer(new PacketHatFragment(fileName, packetsToSend, packetCount, fileBytes, origin, md5));

                                    packetCount++;
                                    fileSize -= fileBytes.length;
                                    offset += index;
                                }

                            }
                            catch(Exception e)
                            {
                                Hats.LOGGER.error("Error creating hat as packet fragment: {}", s);
                                e.printStackTrace();
                            }
                        }
                    }
                }, w -> {});
            }
        }
    }
}
