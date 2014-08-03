package hats.addons.hatstand.common.tileentity;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hats.client.core.HatInfoClient;
import ichun.common.core.EntityHelperBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityHatStand extends TileEntity 
{

	public boolean hasBase;
	public int head;
	public boolean hasStand;
	public boolean isOnFloor;
	public int orientation;
	public int sideOn;
	
	public String hatName;
	public int colourR;
	public int colourG;
	public int colourB;
    public int alpha;

    public GameProfile gameProfile;
	
	public static final String[] headNames = new String[] { "None", "Skeleton", "W. Skele", "Zombie", "Player", "Steve", "Creeper", "Wither", "Wither (I)", "Pigman", "Blaze", "Invisible" };
	
	public HatInfoClient info;
	
	public TileEntityHatStand()
	{
		hasBase = true;
		head = 0;
		hasStand = false;
		isOnFloor = true;
		orientation = 0;
		sideOn = 1;
		colourR = 255;
		colourG = 255;
		colourB = 255;
        alpha = 255;
		hatName = "".toLowerCase();
		
		info = null;
	}

    @SideOnly(Side.CLIENT)
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
    	readFromNBT(pkt.func_148857_g());
    	
    	worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
	
    @Override
    public Packet getDescriptionPacket()
    {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
    	super.writeToNBT(tag);
    	tag.setBoolean("hasBase", hasBase);
    	tag.setInteger("head", head);
    	tag.setBoolean("hasStand", hasStand);
    	tag.setBoolean("isOnFloor", isOnFloor);
    	tag.setInteger("orientation", orientation);
    	tag.setInteger("sideOn", sideOn);
    	
    	tag.setString("hatName", hatName);
    	tag.setInteger("colourR", colourR);
    	tag.setInteger("colourG", colourG);
    	tag.setInteger("colourB", colourB);
        tag.setInteger("alpha", alpha);

        if (this.gameProfile != null)
        {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            NBTUtil.func_152460_a(nbttagcompound1, this.gameProfile);
            tag.setTag("headNameProfile", nbttagcompound1);
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
		hasBase = tag.getBoolean("hasBase");
		head = tag.getInteger("head");
		hasStand = tag.getBoolean("hasStand");
		isOnFloor = tag.getBoolean("isOnFloor");
		orientation = tag.getInteger("orientation");
		sideOn = tag.getInteger("sideOn");

        if(head == 4)
        {
            if(tag.hasKey("headName") && !tag.getString("headName").isEmpty())
            {
                this.gameProfile = EntityHelperBase.getFullGameProfileFromName(tag.getString("headName"));
            }
            else if (tag.hasKey("headNameProfile", 10))
            {
                this.gameProfile = NBTUtil.func_152459_a(tag.getCompoundTag("headNameProfile"));
            }
        }
		
		hatName = tag.getString("hatName");
		colourR = tag.getInteger("colourR");
		colourG = tag.getInteger("colourG");
		colourB = tag.getInteger("colourB");
        alpha = tag.getInteger("alpha");

        if(alpha == 0)
        {
            alpha = 255;
        }

		info = new HatInfoClient(hatName, colourR, colourG, colourB, alpha);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }
	
}
