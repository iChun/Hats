package hats.common.core;

import hats.client.gui.GuiHatSelection;
import hats.common.Hats;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MapPacketHandler
	implements ITinyPacketHandler
{
	@Override
	public void handle(NetHandler handler, Packet131MapData mapData) 
	{
		int id = mapData.uniqueID;
		if(handler instanceof NetServerHandler)
		{
			handleServerPacket((NetServerHandler)handler, mapData.uniqueID, mapData.itemData, (EntityPlayerMP)handler.getPlayer());
		}
		else
		{
			handleClientPacket((NetClientHandler)handler, mapData.uniqueID, mapData.itemData);
		}
	}

	public void handleServerPacket(NetServerHandler handler, short id, byte[] data, EntityPlayerMP player)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
		try
		{
			switch(id)
			{
				case 0:
				{
					String hatName = stream.readUTF();
					int r = stream.readInt();
					int g = stream.readInt();
					int b = stream.readInt();
					
					Hats.proxy.playerWornHats.put(player.username, new HatInfo(hatName, r, g, b));
					
					if(HatHandler.hasHat(hatName))
					{
						Hats.proxy.saveData(DimensionManager.getWorld(0));
						
						Hats.proxy.sendPlayerListOfWornHats(player, false);
					}
					else
					{
						HatHandler.requestHat(hatName, player);
					}
					
					break;
				}
				case 1:
				{
					String hatName = stream.readUTF();
					
					HatHandler.sendHat(hatName, player);
					
					break;
				}
				case 2:
				{
			        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			        DataOutputStream stream1 = new DataOutputStream(bytes);

			        try
			        {
			        	stream1.writeBoolean(FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().areCommandsAllowed(player.username.toLowerCase().trim()));
			        	
			        	PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)0, bytes.toByteArray()), (Player)player);
			        }
			        catch(IOException e)
			        {}
			        break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}

	//TODO Side Split
	
	@SideOnly(Side.CLIENT)
	public void handleClientPacket(NetClientHandler handler, short id, byte[] data)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
		try
		{
			switch(id)
			{
				case 0:
				{
					if(stream.readBoolean())
					{
						FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiHatSelection(Minecraft.getMinecraft().thePlayer));
					}
					else
					{
						Minecraft.getMinecraft().thePlayer.addChatMessage("Server has hats set to Command Giver Mode. You can not give commands!");
					}
					break;
				}
				case 1:
				{
					String hatName = stream.readUTF();
					
					HatHandler.sendHat(hatName, null);
					
					break;
				}
				case 2:
				{
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}
	
}
