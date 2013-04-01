package hats.client.gui;

import hats.common.Hats;
import hats.common.core.HatInfo;
import hats.common.entity.EntityHat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet131MapData;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiHatSelection extends GuiScreen 
	implements ISlider
{

	public EntityPlayer player;
	public EntityHat hat;
	public List availableHats;
	
	protected int xSize = 176;
	protected int ySize = 170;
	
	public float mouseX;
	public float mouseY;
	
	protected int guiLeft;
	protected int guiTop;
	
	public int pageNumber;
	public int colourR;
	public int colourG;
	public int colourB;
	
	public int view;
	
	public GuiHatSelection(EntityPlayer ply)
	{
		player = ply;
		hat = Hats.proxy.tickHandlerClient.hats.get(player.username);
		availableHats = ImmutableList.copyOf(Hats.proxy.tickHandlerClient.availableHats);
		colourR = hat.getR();
		colourG = hat.getG();
		colourB = hat.getB();
		pageNumber = 0;
		view = 0;
	}
	
	@Override
	public void initGui()
	{
		buttonList.clear();
		
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        
        buttonList.add(new GuiButton(1, width / 2 - 6, height / 2 + 54, 20, 20, "<"));
        buttonList.add(new GuiButton(2, width / 2 + 62, height / 2 + 54, 20, 20, ">"));
        buttonList.add(new GuiButton(3, width / 2 + 16, height / 2 + 54, 44, 20, "Done"));
        
        //4, 5, 6, 7 = taken.
        
        buttonList.add(new GuiButton(8, width / 2 + 89, height / 2 - 85, 20, 20, "C"));
        
        pageNumber = 0;
        
        if(hat.hatName.equalsIgnoreCase(""))
        {
        	int i = availableHats.size();
    		i -= i % 6;
    		pageNumber = i / 6;
        }
        else
        {
	        for(int i = 0; i < availableHats.size(); i++)
	        {
	        	String hatName = (String)availableHats.get(i);
	        	if(hatName.equalsIgnoreCase(hat.hatName))
	        	{
	        		i -= i % 6;
	        		pageNumber = i / 6;
	        		break;
	        	}
	        }
        }
        
        updateButtonList();
	}
	
    @Override
    protected void keyTyped(char c, int i)
    {
        if (c == 1)
        {
        	exitAndUpdate();
        	
            this.mc.setIngameFocus();
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
    	if(guibutton.id == 1)
    	{
    		pageNumber--;
    		if(pageNumber < 0)
    		{
    			pageNumber = 0;
    		}
    		updateButtonList();
    	}
    	else if(guibutton.id == 2)
    	{
    		pageNumber++;
    		if(pageNumber * 6 >= availableHats.size())
    		{
    			pageNumber--;
    		}
    		updateButtonList();
    	}
    	else if(guibutton.id == 3)
    	{
    		exitAndUpdate();
    	}
    	else if(guibutton.id == 4)
    	{
    		hat.hatName = "";
    		
    		updateButtonList();
    	}
    	else if(guibutton.id == 8)
    	{
    		view = view == 1 ? 0 : 1;
    		guibutton.displayString = view == 1 ? "H" : "C";
    		
    		updateButtonList();
    	}
    	else if(guibutton.id >= 10)
    	{
    		hat.hatName = guibutton.displayString.toLowerCase();
    		
    		colourR = colourG = colourB = 255;
    		hat.setR(255);
    		hat.setG(255);
    		hat.setB(255);
    		
    		updateButtonList();
    	}
    }
    
    public void exitAndUpdate()
    {
		mc.displayGuiScreen(null);
		
		if(!Hats.proxy.tickHandlerClient.serverHasMod)
		{
    		Hats.favouriteHat = hat.hatName;
    		
    		String r = Integer.toHexString(colourR);
    		String b = Integer.toHexString(colourG);
    		String g = Integer.toHexString(colourB);
    		
    		if(r.length() < 2)
    		{
    			r = "0" + r;
    		}
    		if(g.length() < 2)
    		{
    			g = "0" + g;
    		}
    		if(b.length() < 2)
    		{
    			b = "0" + b;
    		}
    		
    		String name = "#" + r + g + b; 
    		
    		Hats.favouriteHatColourizer = name;
    		
    		Hats.favouriteHatInfo = new HatInfo(hat.hatName, colourR, colourG, colourB);
    		
    		Hats.handleConfig();
		}
		else if(!(player == null || player.isDead || !player.isEntityAlive()))
		{
	        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	        DataOutputStream stream = new DataOutputStream(bytes);

	        try
	        {
	        	stream.writeUTF(hat.hatName);
	        	stream.writeInt(colourR); //R
	        	stream.writeInt(colourG); //G
	        	stream.writeInt(colourB); //B
	        	
	        	PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Hats.getNetId(), (short)0, bytes.toByteArray()));
	        }
	        catch(IOException e)
	        {}
		}
    }
    
    public void updateButtonList()
    {
        for (int k1 = buttonList.size() - 1; k1 >= 0; k1--)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(k1);
            
            if(guibutton.id >= 10 || guibutton.id >= 4 && guibutton.id <= 7)
            {
            	buttonList.remove(k1);
            }
            if(guibutton.id == 1)
            {
	            if(pageNumber == 0 || view == 1)
	            {
	            	guibutton.enabled = false;
	            }
	            else
	            {
	            	guibutton.enabled = true;
	            }
            }
            if(guibutton.id == 2)
            {
        		if((pageNumber + 1) * 6 >= availableHats.size() || view == 1)
        		{
        			guibutton.enabled = false;
        		}
	            else
	            {
	            	guibutton.enabled = true;
	            }
            }
        }

    	if(view == 0)
    	{
	    	int button = 0;
	
	        for(int i = pageNumber * 6; i < (availableHats.size() + 1) && i < (pageNumber + 1) * 6; i++)
	        {
	        	GuiButton btn;
	        	if(i == availableHats.size())
	        	{
	            	btn = new GuiButton(4, width / 2 - 6, height / 2 - 78 + (22 * button), 88, 20, "None");
	            	
	            	if(hat.hatName.equalsIgnoreCase(""))
	            	{
	            		btn.enabled = false;
	            	}
		
	        	}
	        	else
	        	{
		        	String hatName = (String)availableHats.get(i);
		        	
		        	btn = new GuiButton(10 + i, width / 2 - 6, height / 2 - 78 + (22 * button), 88, 20, hatName);
		        	
		        	if(hatName.toLowerCase().equalsIgnoreCase(hat.hatName))
		        	{
		        		btn.enabled = false;
		        	}
	        	}
	        
	        	buttonList.add(btn);
	        	
	        	button++;
	        	if(button == 6)
	        	{
	        		button = 0;
	        		break;
	        	}
	        }
    	}
    	else if(view == 1)
    	{
    		int button = 0;
    		
    		for(int i = 0; i < 3; i++)
    		{
    			GuiButton btn = new GuiSlider(5 + i, width / 2 - 6, height / 2 - 78 + (22 * button), i == 0 ? "Red: " : i == 1 ? "Green: " : "Blue: ", 0, 255, i == 0 ? colourR : i == 1 ? colourG : colourB, this);
    			buttonList.add(btn);
    			
    			button++;
    		}
    	}
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
    	drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture("/mods/hats/textures/gui/hatchooser.png");
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
        
        for (int k1 = 0; k1 < this.buttonList.size(); ++k1)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(k1);
            
            String disp = guibutton.displayString;
            
            if(guibutton.id >= 10)
            {
            	int id = guibutton.id - 10;
            	if(!((pageNumber) * 6 <= id && (pageNumber + 1) * 6 > id))
            	{
            		continue;
            	}
            	
            	if(guibutton.displayString.length() > 17)
            	{
            		guibutton.displayString = guibutton.displayString.substring(0, 14) + "...";
            	}
            }
            guibutton.drawButton(this.mc, par1, par2);
            
            if(!(guibutton instanceof GuiSlider))
            {
            	guibutton.displayString = disp;
            }
            
            if(guibutton.id == 8)
            {
            	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            GL11.glEnable(GL11.GL_BLEND);
	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            	Minecraft.getMinecraft().renderEngine.bindTexture("/mods/hats/textures/gui/icons.png");
            	drawTexturedModalRect(guibutton.xPosition + 2, guibutton.yPosition + 2, (view == 0 ? 16 : 0), 0, 16, 16);

            	GL11.glDisable(GL11.GL_BLEND);
            }
        }
    	
        this.mouseX = (float)par1;
        this.mouseY = (float)par2;

        drawPlayerOnGui(k + 42, l + 155, 55, (float)(k + 42) - (float)mouseX, (float)(l + 155 - 92) - (float)mouseY);

    }
    
    public void drawPlayerOnGui(int par1, int par2, int par3, float par4, float par5)
    {
    	if(player != null)
    	{
	    	hat = Hats.proxy.tickHandlerClient.hats.get(player.username);
	    	if(hat == null)
	    	{
	    		return;
	    	}
	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	        GL11.glPushMatrix();
	        
	        GL11.glDisable(GL11.GL_ALPHA_TEST);
	        
	        GL11.glTranslatef((float)par1, (float)par2, 50.0F);
	        GL11.glScalef((float)(-par3), (float)par3, (float)par3);
	        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
	        float f2 = player.renderYawOffset;
	        float f3 = player.rotationYaw;
	        float f4 = player.rotationPitch;
	        
	        float ff3 = hat.rotationYaw;
	        float ff4 = hat.rotationPitch;
	        
	        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
	        RenderHelper.enableStandardItemLighting();
	        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(-((float)Math.atan((double)(par5 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
	        
	        player.renderYawOffset = (float)Math.atan((double)(par4 / 40.0F)) * 20.0F;
	        player.rotationYaw = hat.rotationYaw = (float)Math.atan((double)(par4 / 40.0F)) * 40.0F;
	        player.rotationPitch = hat.rotationPitch = -((float)Math.atan((double)(par5 / 40.0F))) * 20.0F;
	        player.rotationYawHead = player.rotationYaw;
	        GL11.glTranslatef(0.0F, player.yOffset, 0.0F);
	        
	        RenderManager.instance.playerViewY = 180.0F;
	        RenderManager.instance.renderEntityWithPosYaw(player, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
	        GL11.glTranslatef(0.0F, -0.22F, 0.0F);
	        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 255.0F * 0.8F, 255.0F * 0.8F);
	        RenderManager.instance.renderEntityWithPosYaw(hat, 0.0D, 0.0D, 0.0D, hat.rotationYaw, 1.0F);
	        
	        player.renderYawOffset = f2;
	        player.rotationYaw = f3;
	        player.rotationPitch = f4;
	        
	        hat.rotationYaw = ff3;
	        hat.rotationPitch = ff4;
	        
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	        
	        GL11.glPopMatrix();
	        RenderHelper.disableStandardItemLighting();
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    	}
    }

	@Override
	public void onChangeSliderValue(GuiSlider slider)
	{
		if(slider.id == 5)
		{
			colourR = (int)Math.round(slider.sliderValue * (slider.maxValue - slider.minValue) + slider.minValue);
			hat.setR(colourR);
		}
		else if(slider.id == 6)
		{
			colourG = (int)Math.round(slider.sliderValue * (slider.maxValue - slider.minValue) + slider.minValue);
			hat.setG(colourG);
		}
		else if(slider.id == 7)
		{
			colourB = (int)Math.round(slider.sliderValue * (slider.maxValue - slider.minValue) + slider.minValue);
			hat.setB(colourB);
		}
	}

}
