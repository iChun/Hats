package hats.client.core;

import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import hats.common.core.SessionState;
import hats.common.entity.EntityHat;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
					SessionState.serverHasMod = true;
					SessionState.serverHatMode = (int)stream.readByte();
					SessionState.hasVisited = stream.readBoolean();
					SessionState.serverHat = stream.readUTF();
					SessionState.currentKing = stream.readUTF();
					
					SessionState.showJoinMessage = SessionState.serverHatMode >= 4 && !SessionState.hasVisited;
					
					String availHats = stream.readUTF(); //ignored on Free Mode
					if(SessionState.serverHatMode >= 4)
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
						
						Entity ent = mc.theWorld.getEntityByID(idd);
						if(ent != null && ent instanceof EntityLivingBase)
						{
							HatInfo hatInfo = new HatInfo(name);
							EntityHat hat = new EntityHat(ent.worldObj, (EntityLivingBase)ent, hatInfo);
							Hats.proxy.tickHandlerClient.mobHats.put(ent.entityId, hat);
							ent.worldObj.spawnEntityInWorld(hat);
						}
						
						idd = stream.readInt();
					}
					break;
				}
				case 4:
				{
					SessionState.currentKing = stream.readUTF();
					
					HatHandler.populateHatsList(stream.readUTF());
					
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}		
}
