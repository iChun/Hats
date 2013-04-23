package hats.client.core;

import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import hats.common.entity.EntityHat;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandlerClient
	implements IPacketHandler 
{
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		Minecraft mc = Minecraft.getMinecraft();
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		try
		{
			int id = stream.readByte();
			switch(id)
			{
				case 0:
				{
					Hats.proxy.tickHandlerClient.serverHasMod = true;
					Hats.proxy.tickHandlerClient.serverHatMode = stream.readByte();
					boolean hasVisited = !stream.readBoolean();
					Hats.proxy.tickHandlerClient.showHatHuntingMode = Hats.proxy.tickHandlerClient.serverHatMode == 4 && hasVisited;
					
					String availHats = stream.readUTF(); //ignored on Free Mode
					if(Hats.proxy.tickHandlerClient.serverHatMode == 4)
					{
						HatHandler.populateHatsList(availHats);
					}
					break;
				}
				case 1:
				{
					String name = stream.readUTF();
					while(!name.equalsIgnoreCase("#endPacket"))
					{
						String hatName = stream.readUTF();
						int r = stream.readInt();
						int g = stream.readInt();
						int b = stream.readInt();
						
						Hats.proxy.tickHandlerClient.playerWornHats.put(name, new HatInfo(hatName, r, g, b));
						
						EntityHat hat = Hats.proxy.tickHandlerClient.hats.get(name);
						if(hat != null)
						{
							if(hatName.equalsIgnoreCase(hat.hatName))
							{
								hat.reColour = 20;
							}
							hat.hatName = hatName;
							hat.setR(r);
							hat.setG(g);
							hat.setB(b);
						}
						
						name = stream.readUTF();
					}
					break;
				}
				case 2:
				{
					HatHandler.receiveHatData(stream, null, false);
					break;
				}
				case 3:
				{
					int idd = stream.readInt();
					String name;
					while(idd != -1)
					{
						name = stream.readUTF();
						
						System.out.println(idd);
						Entity ent = mc.theWorld.getEntityByID(idd);
						if(ent != null && ent instanceof EntityLiving)
						{
							HatInfo hatInfo = new HatInfo(name);
							EntityHat hat = new EntityHat(ent.worldObj, (EntityLiving)ent, hatInfo);
							Hats.proxy.tickHandlerClient.mobHats.put(ent.entityId, hat);
							ent.worldObj.spawnEntityInWorld(hat);
						}
						
						idd = stream.readInt();
					}
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}		
}
