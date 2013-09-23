package hats.addons.hatstand.common.core;

import hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import hats.common.Hats;
import hats.common.core.HatHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;
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
					int x = stream.readInt();
					int y = stream.readInt();
					int z = stream.readInt();
					
					TileEntity te = DimensionManager.getWorld(player.dimension).getBlockTileEntity(x, y, z);
					
					if(te instanceof TileEntityHatStand)
					{
						TileEntityHatStand stand = (TileEntityHatStand)te;
						
						String hatName = stream.readUTF();

						int r = stream.readInt();
						int g = stream.readInt();
						int b = stream.readInt();

						stand.hatName = hatName;
						stand.colourR = r;
						stand.colourG = g;
						stand.colourB = b;
						
						stand.head = stream.readInt();
						stand.hasBase = stream.readBoolean();
						stand.hasStand = stream.readBoolean();
						
						if(stand.head == 4)
						{
							stand.headName = player.username;
						}
						else
						{
							stand.headName = "";
						}
						
						if(!HatHandler.hasHat(hatName))
						{
							HatHandler.requestHat(hatName, player);
						}
						
						DimensionManager.getWorld(player.dimension).markBlockForUpdate(x, y, z);
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
			        	stream1.writeBoolean(FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().isPlayerOpped(player.username.toLowerCase().trim()));
			        	
			        	PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)0, bytes.toByteArray()), (Player)player);
			        }
			        catch(IOException e)
			        {}
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
	}
	
}
