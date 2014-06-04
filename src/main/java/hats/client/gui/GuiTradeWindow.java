package hats.client.gui;

import hats.client.core.HatInfoClient;
import hats.client.render.HatRendererHelper;
import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.packet.PacketPing;
import hats.common.packet.PacketString;
import hats.common.packet.PacketTradeOffers;
import ichun.client.gui.GuiSlider;
import ichun.client.gui.ISlider;
import ichun.client.render.RendererHelper;
import ichun.common.core.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GuiTradeWindow extends GuiScreen
	implements ISlider
{
	public static final ResourceLocation texIcons = new ResourceLocation("hats", "textures/gui/icons.png");
	public static final ResourceLocation texTradeWindow = new ResourceLocation("hats", "textures/gui/tradewindow.png");
	
	private final int ID_TOGGLE_HATINV = 1;
	private final int ID_SLIDER_INV = 2;
	private final int ID_MAKE_READY = 3;
	private final int ID_MAKE_TRADE = 4;
	
	protected int xSize = 256;
	protected int ySize = 230;
	
	public float mouseX;
	public float mouseY;
	
	protected int guiLeft;
	protected int guiTop;

    public ArrayList<ItemStack> items;
    public ArrayList<String> hats;
    
    public ArrayList<ItemStack> ourItemsForTrade;
    public ArrayList<String> ourHatsForTrade;

    public ArrayList<ItemStack> theirItemsForTrade;
    public ArrayList<String> theirHatsForTrade;
    
    public ItemStack grabbedStack;

	public int hatSlots = 3;
	public int invSlots = 12;
	public double sliderProg = 0.0D;
	public RenderItem itemRenderer = new RenderItem();
	
	public boolean selfCanScroll = false;
	public boolean theirCanScroll = false;
	public boolean selfIsScrolling = false;
	public boolean theirIsScrolling = false;
	
	public float selfScrollProg;
	public float theirScrollProg;
	
	public boolean selfReady;
	public boolean theirReady;
	
	public String lastClicked = "";
	public int clickTimeout;
	
	public Minecraft mc;
	
	public boolean showInv;
	
	public long rotationalClock;
	
	public String trader;
	
	public GuiTextField searchBar;
	
	public GuiTextField chatBar;
    public ArrayList<String> chatMessages;
    public float chatScroll;
    
    public boolean chatScrolling;
    
    public boolean updateOffers;
    
    public boolean pointOfNoReturn;
    
    public boolean clickedMakeTrade;
	
	public GuiTradeWindow(String trader1)
	{
		trader = trader1;
		
		mc = Minecraft.getMinecraft();
		showInv = false;
		
        items = new ArrayList<ItemStack>();
        hats = new ArrayList<String>(Hats.proxy.tickHandlerClient.availableHats);
        
    	invSlots = 0;
    	for(ItemStack is : mc.thePlayer.inventory.mainInventory)
    	{
    		if(is != null)
    		{
    			items.add(is.copy());
    			invSlots++;
    		}
    	}
    	
    	if(invSlots < 12)
    	{
    		invSlots = 12;
    	}
    	
    	hatSlots = 0;
    	for(int i = hats.size() - 1; i >= 0; i--)
    	{
    		String hatName = hats.get(i);
    		if(HatHandler.isPlayersContributorHat(hatName, mc.thePlayer.getCommandSenderName()))
    		{
    			hats.remove(i);
    		}
    		else
    		{
    			hatSlots++;
    		}
    	}
    	if(hatSlots < 3)
    	{
    		hatSlots = 3;
    	}

    	ourItemsForTrade = new ArrayList<ItemStack>();
    	ourHatsForTrade = new ArrayList<String>();
    	
    	theirItemsForTrade = new ArrayList<ItemStack>();
    	theirHatsForTrade = new ArrayList<String>();
    	
    	chatMessages = new ArrayList<String>();
	}
	
	@Override
	public void initGui()
	{
        Keyboard.enableRepeatEvents(true);

        rotationalClock = Minecraft.getSystemTime();
        
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        buttonList.clear();
        
        buttonList.add(new GuiButton(ID_TOGGLE_HATINV, guiLeft + 6, guiTop + 6, 108, 20, showInv ? StatCollector.translateToLocal("hats.trade.yourInventory") : StatCollector.translateToLocal("hats.trade.yourHats")));
        buttonList.add(new GuiSlider(ID_SLIDER_INV, guiLeft + 6, guiTop + 65, 108, "", "", 0D, 1D, sliderProg, false, false, this));
        
        buttonList.add(new GuiButton(ID_MAKE_READY, guiLeft + 128, guiTop + 77, 120, 20, ""));//text is custom rendered
        buttonList.add(new GuiButton(ID_MAKE_TRADE, guiLeft + 148, guiTop + ySize - 32, 80, 20, StatCollector.translateToLocal("hats.trade.makeTrade")));
        
    	searchBar = new GuiTextField(mc.fontRenderer, this.guiLeft + 21, this.guiTop + 90, 93, mc.fontRenderer.FONT_HEIGHT);
    	searchBar.setMaxStringLength(15);
    	searchBar.setEnableBackgroundDrawing(false);
    	searchBar.setTextColor(16777215);
    	
    	chatBar = new GuiTextField(mc.fontRenderer, this.guiLeft + 6, this.guiTop + ySize - 15, 103, mc.fontRenderer.FONT_HEIGHT);
    	chatBar.setMaxStringLength(80);
    	chatBar.setEnableBackgroundDrawing(false);
    	chatBar.setTextColor(16777215);
	}

	@Override
	public void onGuiClosed() 
	{
        Keyboard.enableRepeatEvents(false);

        PacketHandler.sendToServer(Hats.channels, new PacketPing(2, false));
	}
	
	@Override
	public void updateScreen() 
	{
		if(updateOffers)
		{
			updateOffers = false;

            PacketHandler.sendToServer(Hats.channels, new PacketTradeOffers(ourHatsForTrade, ourItemsForTrade));
		}
		if(clickTimeout > 0)
		{
			clickTimeout--;
			if(clickTimeout == 0)
			{
				lastClicked = "";
			}
		}
		searchBar.updateCursorCounter();
	}
	
	public boolean handleClickStack(ItemStack is, ArrayList<ItemStack> refList, int btn)
	{
		int tradeSize = ourItemsForTrade.size();
		boolean flag = false;
		boolean scroll = true;
		if(grabbedStack == null)
		{
			if(is != null)
			{
				if(btn == 0)
				{
					grabbedStack = is;
					if(GuiScreen.isShiftKeyDown())
					{
						ArrayList<ItemStack> transferList = refList == items ? ourItemsForTrade : items;
						handleClickStack(is, transferList, btn);
						scroll = false;
					}
					refList.remove(is);
					updateOffers = true;
				}
				else if(btn == 1)
				{
					ItemStack is1 = is.splitStack(is.stackSize / 2);
					if(is1.stackSize == 0)
					{
						grabbedStack = is;
						refList.remove(is);
					}
					else
					{
						grabbedStack = is1;
					} 
					if(is.stackSize <= 0)
					{
						refList.remove(is);
					}
					updateOffers = true;
				}
			}
		}
		else
		{
			boolean added = false;
			for(ItemStack is1 : refList)
			{
				if(is1.isItemEqual(is) && ItemStack.areItemStackTagsEqual(is, is1) && is1.stackSize < is1.getMaxStackSize() && is.stackSize > 0)
				{
					if(btn == 0)
					{
						while(is1.stackSize < is1.getMaxStackSize() && is.stackSize > 0)
						{
							is1.stackSize++;
							is.stackSize--;
						}
					}
					else if(btn == 1)
					{
						is1.stackSize++;
						is.stackSize--;
						added = true;
					}
				}
			}
			if(is.stackSize <= 0)
			{
				grabbedStack = null;
			}
			else if(btn == 0)
			{
				refList.add(is);
				grabbedStack = null;
			}
			else if(btn == 1 && !added)
			{
				refList.add(is.splitStack(1));
				if(is.stackSize <= 0)
				{
					grabbedStack = null;
				}
			}
			updateOffers = true;
			flag = true;
		}
		selfCanScroll = ourHatsForTrade.size() > 3 || ourItemsForTrade.size() > 6;
		if(!selfCanScroll)
		{
			selfScrollProg = 0.0F;
		}
		else if(scroll && tradeSize != ourItemsForTrade.size() && (ourItemsForTrade.size() % 6 == 1 && tradeSize % 6 == 0 || ourItemsForTrade.size() % 6 == 0 && tradeSize % 6 == 1))
		{
			float currentBoxes = (float)Math.ceil((float)Math.max(ourHatsForTrade.size(), 3) / 3F) * 2 + (float)Math.ceil((float)Math.max(tradeSize, 6) / 6F) - 3;
			if(currentBoxes > 0)
			{
				selfScrollProg = MathHelper.clamp_float(selfScrollProg * (ourItemsForTrade.size() > tradeSize ? ((currentBoxes) / (currentBoxes + 1)) : ((currentBoxes) / (currentBoxes - 1))), 0.0F, 1.0F);
			}
		}
		if(items.size() <= 12 && showInv)
		{
    		for(int ii = 0; ii < buttonList.size(); ii++)
    		{
    			GuiButton btn1 = (GuiButton)buttonList.get(ii);
    			if(btn1 instanceof GuiSlider)
    			{
    				((GuiSlider)btn1).sliderValue = 0.0D;
    				((GuiSlider)btn1).updateSlider();
    			}
    		}
		}
		return flag;
	}
	
    @Override
    protected void mouseClicked(int x, int y, int btn)
    {
    	super.mouseClicked(x, y, btn);
    	
    	if(!selfReady)
    	{
	    	boolean isInInv = x >= guiLeft + 6 && x < guiLeft + 6 + 108 && y >= guiTop + 29 && y < guiTop + 29 + 36;
	    	
	    	if(isInInv)
	    	{
	    		if(!handleClickStack(grabbedStack, items, btn))
	    		{
		    		int mouseProg = x - (guiLeft + 6);
		    		
		            ArrayList<ItemStack> itemsList = new ArrayList<ItemStack>(items);
		            ArrayList<String> hatsList = new ArrayList<String>(hats);
		
		            hatsList.removeAll(ourHatsForTrade);
		            
		    		String query = searchBar.getText();
		    		if(showInv)
		    		{
		    			for(int i = itemsList.size() - 1; i >= 0; i--)
		    			{
		    				ItemStack is = itemsList.get(i);
		    				if(is.getDisplayName().toLowerCase().startsWith(query.toLowerCase()))
		    				{
		    					continue;
		    				}
		    				boolean remove = true;
		    				String[] split = is.getDisplayName().split(" ");
		        			for(String s1 : split)
		        			{
		        				if(s1.toLowerCase().startsWith(query.toLowerCase()))
		        				{
		        					remove = false;
		        				}
		        			}
		        			if(remove)
		        			{
		        				itemsList.remove(i);
		        			}
		    			}
		    		}
		    		else
		    		{
		    			for(int i = hatsList.size() - 1; i >= 0; i--)
		    			{
		    				String s = hatsList.get(i);
		    				if(s.toLowerCase().startsWith(query.toLowerCase()))
		    				{
		    					continue;
		    				}
		    				String[] split = s.split(" ");
		    				boolean remove = true;
		        			for(String s1 : split)
		        			{
		        				if(s1.toLowerCase().startsWith(query.toLowerCase()))
		        				{
		        					remove = false;
		        				}
		        			}
		        			if(remove)
		        			{
		        				hatsList.remove(i);
		        			}
		    			}
		    		}
		            
		            invSlots = itemsList.size();
		            hatSlots = hatsList.size();
		            
		        	if(invSlots < 12)
		        	{
		        		invSlots = 12;
		        	}
		        	if(hatSlots < 3)
		        	{
		        		hatSlots = 3;
		        	}
		
			        int size = showInv ? 18 : 36;
			        int columnWidth = 108;
			        int slotsToDraw = showInv ? (int)Math.ceil((float)invSlots / 2F) : hatSlots;
			        
			        int overallLength = slotsToDraw * size;
			        int startX = guiLeft + 6 + (int)((overallLength - columnWidth) * sliderProg);
			        
			        if(showInv)
			        {
			        	for(int i = 0; i < itemsList.size(); i++)
			        	{
			        		if(guiLeft + 6 + (size * (int)Math.floor((float)i / 2F)) + size < startX)
			        		{
			        			continue;
			        		}
			        		if(guiLeft + 6 + (size * (int)Math.floor((float)i / 2F)) <= startX + mouseProg && guiLeft + 6 + (size * (int)Math.floor((float)i / 2F)) + size > startX + mouseProg && (y < guiTop + 29 + size && i % 2 == 0 || y >= guiTop + 29 + size && i % 2 == 1))
			        		{
			        			handleClickStack(itemsList.get(i), items, btn);
			        			break;
			        		}
			        	}
			        }
			        else if(btn == 0)
			        {
			        	for(int i = 0; i < hatsList.size(); i++)
			        	{
			        		if(guiLeft + 6 + (size * i) + size < startX)
			        		{
			        			continue;
			        		}
			        		if(guiLeft + 6 + (size * i) <= startX + mouseProg && guiLeft + 6 + (size * i) + size > startX + mouseProg)
			        		{
			        			String hatName = hatsList.get(i);
			        			if(hatName.equalsIgnoreCase(lastClicked))
			        			{
			        				ourHatsForTrade.add(hatName);
			        				selfCanScroll = ourHatsForTrade.size() > 3 || ourItemsForTrade.size() > 6;
			        				if(!selfCanScroll)
			        				{
			        					selfScrollProg = 0.0F;
			        				}
			        				else if(ourHatsForTrade.size() % 3 == 1)
			        				{
			        					float oldBoxes = (float)Math.floor((float)Math.max(ourHatsForTrade.size(), 3) / 3F) * 2 + (float)Math.ceil((float)Math.max(ourItemsForTrade.size(), 6) / 6F) - 3;
			        					if(oldBoxes > 0)
			        					{
			        						selfScrollProg = selfScrollProg * oldBoxes / (oldBoxes + 2);
			        					}
			        				}
			        				
		        		    		for(int ii = 0; ii < buttonList.size(); ii++)
		        		    		{
		        		    			GuiButton btn1 = (GuiButton)buttonList.get(ii);
		        		    			if(btn1 instanceof GuiSlider)
		        		    			{
		        		    				((GuiSlider)btn1).sliderValue = (double)MathHelper.clamp_float((float)((GuiSlider)btn1).sliderValue * (hatSlots - 3) / (hatSlots - 4), 0.0F, 1.0F);
		        		    				((GuiSlider)btn1).updateSlider();
		        		    			}
		        		    		}
		        		    		lastClicked = "";
		        		    		
		        		    		updateOffers = true;
			        			}
			        			else
			        			{
			        				lastClicked = hatName;	        				
			        			}
			        			break;
			        		}
			        	}
			        }
	    		}
	    	}
	    	
	    	boolean isInSelf = x >= guiLeft + 125 && x < guiLeft + 125 + 108 && y >= guiTop + 17 && y < guiTop + 17 + 54;
	    	
	    	if(isInSelf)
	    	{
	    		if(!handleClickStack(grabbedStack, ourItemsForTrade, btn))
	    		{
		    		int mouseProg = y - (guiTop + 17);
		
		    		int size = 36;
		    		int columnWidth = 18 * 3;
		    		int slotsToDraw = ourHatsForTrade.size();
		
		    		while(slotsToDraw % 3 != 0 || slotsToDraw < 3)
		    		{
		    			slotsToDraw++;
		    		}
		
		    		int hatLevels = (int)Math.ceil((float)slotsToDraw / 3F);
		
		    		int boxes = hatLevels * 2 + (int)Math.ceil((float)Math.max(ourItemsForTrade.size(), 6) / 6F);
		
			        int overallLength = boxes * 18;
		    		
			        int startY = guiTop + 17 + (int)((overallLength - columnWidth) * selfScrollProg);
			        
			        boolean clicked = false;
			        
			        if(btn == 0)
			        {
				        for(int i = 0; i < ourHatsForTrade.size(); i++)
				        {
			        		if(guiTop + 17 + (size * (int)Math.floor((float)i / 3F)) + size < startY)
			        		{
			        			continue;
			        		}
			        		if(guiTop + 17 + (size * (int)Math.floor((float)i / 3F)) <= startY + mouseProg && guiTop + 17 + (size * (int)Math.floor((float)i / 3F)) + size > startY + mouseProg && (x < guiLeft + 125 + size && i % 3 == 0 || x < guiLeft + 125 + size + size && i % 3 == 1 || x >= guiLeft + 125 + size + size && i % 3 == 2))
			        		{
			        			String hatName = ourHatsForTrade.get(i);
			        			if(hatName.equalsIgnoreCase(lastClicked))
			        			{
			        				ourHatsForTrade.remove(hatName);
			        				selfCanScroll = ourHatsForTrade.size() > 3 || ourItemsForTrade.size() > 6;
			        				if(!selfCanScroll)
			        				{
			        					selfScrollProg = 0.0F;
			        				}
			        				else if(ourHatsForTrade.size() % 3 == 0)
			        				{
			        					float oldBoxes = (float)Math.floor((float)Math.max(ourHatsForTrade.size(), 3) / 3F) * 2 + (float)Math.ceil((float)Math.max(ourItemsForTrade.size(), 6) / 6F) - 3;
			        					if(oldBoxes > 0)
			        					{
			        						selfScrollProg = MathHelper.clamp_float(selfScrollProg * (oldBoxes + 2) / (oldBoxes), 0.0F, 1.0F);
			        					}
			        				}
			        				
			    					if(!showInv)
			    					{
			        		    		for(int ii = 0; ii < buttonList.size(); ii++)
			        		    		{
			        		    			GuiButton btn1 = (GuiButton)buttonList.get(ii);
			        		    			if(btn1 instanceof GuiSlider)
			        		    			{
			        		    				((GuiSlider)btn1).sliderValue = (double)MathHelper.clamp_float((float)((GuiSlider)btn1).sliderValue * (hatSlots - 3) / (hatSlots + 1 - 3), 0.0F, 1.0F);
			        		    				((GuiSlider)btn1).updateSlider();
			        		    			}
			        		    		}
			    					}
			    					lastClicked = "";
			    					
			    					updateOffers = true;
			        			}
			        			else
			        			{
			        				lastClicked = hatName;	        				
			        			}
			        			clicked = true;
			        			break;
			        		}
				        }
			        }
			        
		    		size = 18;
		    		columnWidth = 108 / size;
		    		slotsToDraw = 6;
		
		    		if(!clicked)
		    		{
			        	for(int i = 0; i < ourItemsForTrade.size(); i++)
			        	{
			        		if(guiTop + 17 + (size * (int)Math.floor((float)i / 6F)) + size + hatLevels * 36 < startY)
			        		{
			        			continue;
			        		}
			        		if(guiTop + 17 + (size * (int)Math.floor((float)i / 6F)) + hatLevels * 36 <= startY + mouseProg && guiTop + 17 + (size * (int)Math.floor((float)i / 6F)) + size + hatLevels * 36 > startY + mouseProg && x < guiLeft + 125 + size + (i % 6 * size))
			        		{
			        			handleClickStack(ourItemsForTrade.get(i), ourItemsForTrade, btn);
			        			break;
			        		}
			        	}
		    		}
	    		}
	    	}
    	}

    	if(grabbedStack == null)
    	{
	    	boolean isOnOurScroll = x >= guiLeft + 237 && x < guiLeft + 237 + 12 && y >= guiTop + 18 && y < guiTop + 18 + 37 + 15;
	    	if(isOnOurScroll && btn == 0 && selfCanScroll)
	    	{
	    		selfIsScrolling = true;
	    	}
	    	boolean isOnTheirScroll = x >= guiLeft + 237 && x < guiLeft + 237 + 12 && y >= guiTop + 117 && y < guiTop + 117 + 37 + 15;
	    	if(isOnTheirScroll && btn == 0 && theirCanScroll)
	    	{
	    		theirIsScrolling = true;
	    	}
	    	boolean isOnChatScroll = x >= guiLeft + 107 && x < guiLeft + 107 + 4 && y >= guiTop + 195 - 82 && y < guiTop + 195 + 15;
	    	if(isOnChatScroll)
	    	{
	    		chatScrolling = true;
	    	}
    	
	    	boolean isOnSearchBar = x >= guiLeft + 21 && x < guiLeft + 21 + 93 && y >= guiTop + 90 && y < guiTop + 90 + mc.fontRenderer.FONT_HEIGHT;
	    	
	    	searchBar.setFocused(isOnSearchBar);
	
	    	boolean isOnChatBar = x >= guiLeft + 6 && x < guiLeft + 6 + 106 && y >= guiTop + ySize - 15 && y < guiTop + ySize - 15 + mc.fontRenderer.FONT_HEIGHT;
	
	    	chatBar.setFocused(isOnChatBar);
    	}
    }
    
    @Override
    protected void keyTyped(char c, int i)
    {
    	searchBar.textboxKeyTyped(c, i);
    	chatBar.textboxKeyTyped(c, i);
    	if(searchBar.isFocused())
    	{
    		for(int ii = 0; ii < buttonList.size(); ii++)
    		{
    			GuiButton btn1 = (GuiButton)buttonList.get(ii);
    			if(btn1 instanceof GuiSlider)
    			{
    				((GuiSlider)btn1).sliderValue = 0.0D;
    				((GuiSlider)btn1).updateSlider();
    			}
    		}
    	}
    	if(chatBar.isFocused() && i == Keyboard.KEY_RETURN && !chatBar.getText().isEmpty())
    	{
            PacketHandler.sendToServer(Hats.channels, new PacketString(2, mc.thePlayer.getCommandSenderName() + ": " + chatBar.getText()));

    		chatMessages.add(mc.thePlayer.getCommandSenderName() + ": " + chatBar.getText());
    		chatBar.setText("");
    	}
        if (i == 1)
        {
	    	if(searchBar.isFocused())
	    	{
	    		searchBar.setText("");
	    		searchBar.setFocused(false);
	    	}
	    	else
	    	{
	            mc.displayGuiScreen((GuiScreen)null);
	            mc.setIngameFocus();
	    	}
        }
    }
    
    
    public void drawForeground(int x, int y, float par3)
    {
    	if(grabbedStack != null)
    	{
    		drawItemStack(grabbedStack, x - 8, y - 8);
    	}
    	else
    	{
        	boolean isInInv = x >= guiLeft + 6 && x < guiLeft + 6 + 108 && y >= guiTop + 29 && y < guiTop + 29 + 36;
        	boolean isInSelf = x >= guiLeft + 125 && x < guiLeft + 125 + 108 && y >= guiTop + 17 && y < guiTop + 17 + 54;
        	boolean isInThem = x >= guiLeft + 125 && x < guiLeft + 125 + 108 && y >= guiTop + 116 && y < guiTop + 116 + 54;
        	
	    	if(isInInv)
	    	{
	    		int mouseProg = x - (guiLeft + 6);
	    		
	            ArrayList<ItemStack> itemsList = new ArrayList<ItemStack>(items);
	            ArrayList<String> hatsList = new ArrayList<String>(hats);
	
	            hatsList.removeAll(ourHatsForTrade);
	            
	    		String query = searchBar.getText();
	    		if(showInv)
	    		{
	    			for(int i = itemsList.size() - 1; i >= 0; i--)
	    			{
	    				ItemStack is = itemsList.get(i);
	    				if(is.getDisplayName().toLowerCase().startsWith(query.toLowerCase()))
	    				{
	    					continue;
	    				}
	    				boolean remove = true;
	    				String[] split = is.getDisplayName().split(" ");
	        			for(String s1 : split)
	        			{
	        				if(s1.toLowerCase().startsWith(query.toLowerCase()))
	        				{
	        					remove = false;
	        				}
	        			}
	        			if(remove)
	        			{
	        				itemsList.remove(i);
	        			}
	    			}
	    		}
	    		else
	    		{
	    			for(int i = hatsList.size() - 1; i >= 0; i--)
	    			{
	    				String s = hatsList.get(i);
	    				if(s.toLowerCase().startsWith(query.toLowerCase()))
	    				{
	    					continue;
	    				}
	    				String[] split = s.split(" ");
	    				boolean remove = true;
	        			for(String s1 : split)
	        			{
	        				if(s1.toLowerCase().startsWith(query.toLowerCase()))
	        				{
	        					remove = false;;
	        				}
	        			}
	        			if(remove)
	        			{
	        				hatsList.remove(i);
	        			}
	    			}
	    		}
	            
	            invSlots = itemsList.size();
	            hatSlots = hatsList.size();
	            
	        	if(invSlots < 12)
	        	{
	        		invSlots = 12;
	        	}
	        	if(hatSlots < 3)
	        	{
	        		hatSlots = 3;
	        	}
	
		        int size = showInv ? 18 : 36;
		        int columnWidth = 108;
		        int slotsToDraw = showInv ? (int)Math.ceil((float)invSlots / 2F) : hatSlots;
		        
		        int overallLength = slotsToDraw * size;
		        int startX = guiLeft + 6 + (int)((overallLength - columnWidth) * sliderProg);
		        
		        if(showInv)
		        {
		        	for(int i = 0; i < itemsList.size(); i++)
		        	{
		        		if(guiLeft + 6 + (size * (int)Math.floor((float)i / 2F)) + size < startX)
		        		{
		        			continue;
		        		}
		        		if(guiLeft + 6 + (size * (int)Math.floor((float)i / 2F)) <= startX + mouseProg && guiLeft + 6 + (size * (int)Math.floor((float)i / 2F)) + size > startX + mouseProg && (y < guiTop + 29 + size && i % 2 == 0 || y >= guiTop + 29 + size && i % 2 == 1))
		        		{
		        			drawTooltip(itemsList.get(i).getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips), x, y);
		        			break;
		        		}
		        	}
		        }
		        else
		        {
		        	for(int i = 0; i < hatsList.size(); i++)
		        	{
		        		if(guiLeft + 6 + (size * i) + size < startX)
		        		{
		        			continue;
		        		}
		        		if(guiLeft + 6 + (size * i) <= startX + mouseProg && guiLeft + 6 + (size * i) + size > startX + mouseProg)
		        		{
		        			drawTooltip(Arrays.asList(hatsList.get(i)), x, y);
		        			break;
		        		}
		        	}
		        }
	    	}
	    	
	    	if(isInSelf)
	    	{
	    		int mouseProg = y - (guiTop + 17);
	
	    		int size = 36;
	    		int columnWidth = 18 * 3;
	    		int slotsToDraw = ourHatsForTrade.size();
	
	    		while(slotsToDraw % 3 != 0 || slotsToDraw < 3)
	    		{
	    			slotsToDraw++;
	    		}
	
	    		int hatLevels = (int)Math.ceil((float)slotsToDraw / 3F);
	
	    		int boxes = hatLevels * 2 + (int)Math.ceil((float)Math.max(ourItemsForTrade.size(), 6) / 6F);
	
		        int overallLength = boxes * 18;
	    		
		        int startY = guiTop + 17 + (int)((overallLength - columnWidth) * selfScrollProg);
		        
		        boolean clicked = false;
		        
		        for(int i = 0; i < ourHatsForTrade.size(); i++)
		        {
	        		if(guiTop + 17 + (size * (int)Math.floor((float)i / 3F)) + size < startY)
	        		{
	        			continue;
	        		}
	        		if(guiTop + 17 + (size * (int)Math.floor((float)i / 3F)) <= startY + mouseProg && guiTop + 17 + (size * (int)Math.floor((float)i / 3F)) + size > startY + mouseProg && (x < guiLeft + 125 + size && i % 3 == 0 || x < guiLeft + 125 + size + size && i % 3 == 1 || x >= guiLeft + 125 + size + size && i % 3 == 2))
	        		{
	        			String hatName = ourHatsForTrade.get(i);
	        			drawTooltip(Arrays.asList(ourHatsForTrade.get(i)), x, y);
	        			clicked = true;
	        			break;
	        		}
		        }
		        
	    		size = 18;
	    		columnWidth = 108 / size;
	    		slotsToDraw = 6;
	
	    		if(!clicked)
	    		{
		        	for(int i = 0; i < ourItemsForTrade.size(); i++)
		        	{
		        		if(guiTop + 17 + (size * (int)Math.floor((float)i / 6F)) + size + hatLevels * 36 < startY)
		        		{
		        			continue;
		        		}
		        		if(guiTop + 17 + (size * (int)Math.floor((float)i / 6F)) + hatLevels * 36 <= startY + mouseProg && guiTop + 17 + (size * (int)Math.floor((float)i / 6F)) + size + hatLevels * 36 > startY + mouseProg && x < guiLeft + 125 + size + (i % 6 * size))
		        		{
		        			drawTooltip(ourItemsForTrade.get(i).getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips), x, y);
		        			break;
		        		}
		        	}
	    		}
	    	}

	    	if(isInThem)
	    	{
	    		int mouseProg = y - (guiTop + 116);
	
	    		int size = 36;
	    		int columnWidth = 18 * 3;
	    		int slotsToDraw = theirHatsForTrade.size();
	
	    		while(slotsToDraw % 3 != 0 || slotsToDraw < 3)
	    		{
	    			slotsToDraw++;
	    		}
	
	    		int hatLevels = (int)Math.ceil((float)slotsToDraw / 3F);
	
	    		int boxes = hatLevels * 2 + (int)Math.ceil((float)Math.max(ourItemsForTrade.size(), 6) / 6F);
	
		        int overallLength = boxes * 18;
	    		
		        int startY = guiTop + 116 + (int)((overallLength - columnWidth) * theirScrollProg);
		        
		        boolean clicked = false;
		        
		        for(int i = 0; i < theirHatsForTrade.size(); i++)
		        {
	        		if(guiTop + 116 + (size * (int)Math.floor((float)i / 3F)) + size < startY)
	        		{
	        			continue;
	        		}
	        		if(guiTop + 116 + (size * (int)Math.floor((float)i / 3F)) <= startY + mouseProg && guiTop + 116 + (size * (int)Math.floor((float)i / 3F)) + size > startY + mouseProg && (x < guiLeft + 125 + size && i % 3 == 0 || x < guiLeft + 125 + size + size && i % 3 == 1 || x >= guiLeft + 125 + size + size && i % 3 == 2))
	        		{
	        			String hatName = theirHatsForTrade.get(i);
	        			drawTooltip(Arrays.asList(theirHatsForTrade.get(i)), x, y);
	        			clicked = true;
	        			break;
	        		}
		        }
		        
	    		size = 18;
	    		columnWidth = 108 / size;
	    		slotsToDraw = 6;
	
	    		if(!clicked)
	    		{
		        	for(int i = 0; i < theirItemsForTrade.size(); i++)
		        	{
		        		if(guiTop + 116 + (size * (int)Math.floor((float)i / 6F)) + size + hatLevels * 36 < startY)
		        		{
		        			continue;
		        		}
		        		if(guiTop + 116 + (size * (int)Math.floor((float)i / 6F)) + hatLevels * 36 <= startY + mouseProg && guiTop + 116 + (size * (int)Math.floor((float)i / 6F)) + size + hatLevels * 36 > startY + mouseProg && x < guiLeft + 125 + size + (i % 6 * size))
		        		{
		        			drawTooltip(theirItemsForTrade.get(i).getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips), x, y);
		        			break;
		        		}
		        	}
	    		}
	    	}
	    	
    	}
    }
    
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
//    	initGui();
    	if(mc == null)
    	{
    		mc = Minecraft.getMinecraft();
    		fontRendererObj = mc.fontRenderer;
    	}
    	drawDefaultBackground();
    	
        boolean flag = Mouse.isButtonDown(0);
        if(!flag)
        {
        	selfIsScrolling = false;
        	theirIsScrolling = false;
        	chatScrolling = false;
        }
        else if(selfIsScrolling)
        {
//        	this.drawTexturedModalRect(k + 237, l + 18, selfCanScroll ? 54 : 66, 45, 12, 15); //scroll button is 12 wide 15 long //scroll bar is 37 long
//        	37 pix long; 7 on one side, 8 on the top.
//        	y >= guiTop + 18 && y < guiTop + 18 + 37
        	selfScrollProg = MathHelper.clamp_float((float)(par2 - (guiTop + 18 + 7)) / 37F, 0.0F, 1.0F);
        }
        else if(theirIsScrolling)
        {
        	theirScrollProg = MathHelper.clamp_float((float)(par2 - (guiTop + 117 + 7)) / 37F, 0.0F, 1.0F);
        }
        else if(chatScrolling)
        {
        	chatScroll = MathHelper.clamp_float((float)((guiTop + 195 + 7) - par2) / 82F, 0.0F, 1.0F);
        }

        this.mouseX = (float)par1;
        this.mouseY = (float)par2;
    	
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = this.guiLeft;
        int l = this.guiTop;
        
        ArrayList<ItemStack> itemsList = new ArrayList<ItemStack>(items);
        ArrayList<String> hatsList = new ArrayList<String>(hats);

        hatsList.removeAll(ourHatsForTrade);
        
		String query = searchBar.getText();
		if(showInv)
		{
			for(int i = itemsList.size() - 1; i >= 0; i--)
			{
				ItemStack is = itemsList.get(i);
				if(is.getDisplayName().toLowerCase().startsWith(query.toLowerCase()))
				{
					continue;
				}
				boolean remove = true;
				String[] split = is.getDisplayName().split(" ");
    			for(String s1 : split)
    			{
    				if(s1.toLowerCase().startsWith(query.toLowerCase()))
    				{
    					remove = false;
    				}
    			}
    			if(remove)
    			{
    				itemsList.remove(i);
    			}
			}
		}
		else
		{
			for(int i = hatsList.size() - 1; i >= 0; i--)
			{
				String s = hatsList.get(i);
				if(s.toLowerCase().startsWith(query.toLowerCase()))
				{
					continue;
				}
				String[] split = s.split(" ");
				boolean remove = true;
    			for(String s1 : split)
    			{
    				if(s1.toLowerCase().startsWith(query.toLowerCase()))
    				{
    					remove = false;;
    				}
    			}
    			if(remove)
    			{
    				hatsList.remove(i);
    			}
			}
		}
        
        invSlots = itemsList.size();
        hatSlots = hatsList.size();
        
    	if(invSlots < 12)
    	{
    		invSlots = 12;
    	}
    	if(hatSlots < 3)
    	{
    		hatSlots = 3;
    	}
        
        
        this.mc.getTextureManager().bindTexture(texIcons);
        
        GL11.glPushMatrix();

        RendererHelper.startGlScissor(guiLeft + 6, guiTop + 29, 108, 36);

        if(mc.thePlayer != null)
        {
	        int size = showInv ? 18 : 36;
	        int columnWidth = 108 / size;
	        int slotsToDraw = showInv ? (int)Math.ceil((float)invSlots / 2F) : hatSlots;
	        
	        if(slotsToDraw > columnWidth)
	        {
	        	GL11.glTranslatef(-(float)(size * (slotsToDraw - columnWidth) * sliderProg), 0.0F, 0.0F);
	        }
	        
	        int lastInv = 0;
	        for(int i = 0; i < slotsToDraw; i++)
	        {
	        	if(((size * i) + size < (float)(size * (slotsToDraw - columnWidth) * sliderProg) || (size * i) > (float)(size * (slotsToDraw) * sliderProg + columnWidth * size)))
	        	{
	        		lastInv++;
	        		if(showInv)
	        		{
	        			lastInv++;
	        		}
	        		continue;
	        	}
	        	this.drawTexturedModalRect(k + 6 + (size * i), l + 29, size == 36 ? 0 : 36, 45, size, size);
	        	
	        	if(showInv)
	        	{
	        		if(lastInv < itemsList.size())
	        		{
	        			drawItemStack(itemsList.get(lastInv), k + 6 + (size * i) + 1, l + 29 + 1);
	        			lastInv++;
	        		}
	        	}
	        	else
	        	{
	        		if(lastInv < hatsList.size())
	        		{
	        			GL11.glPushMatrix();

	                    GL11.glTranslatef((float)(k + 6 + (size * i) - 2), (float)(l + 29 + 14), -3.0F + this.zLevel);

	    	            GL11.glScalef(20.0F, 20.0F, 20.0F);

	                    GL11.glTranslatef(1.0F, 0.5F, 1.0F);
	                    GL11.glScalef(1.0F, 1.0F, -1.0F);
	                    GL11.glRotatef(190.0F, 1.0F, 0.0F, 0.0F);
	                    GL11.glRotatef(45.0F + (Minecraft.getSystemTime() - this.rotationalClock) / 6F, 0.0F, 1.0F, 0.0F);
	        			
	        			HatInfoClient info = new HatInfoClient(hatsList.get(lastInv).toLowerCase());
	        			HatRendererHelper.renderHat(info, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0000000F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, true, true, 1.0F);
	        			lastInv++;
	        			GL11.glPopMatrix();
	        		}
	        	}
	        	this.mc.getTextureManager().bindTexture(texIcons);
	        	if(size == 18)
	        	{
	        		this.drawTexturedModalRect(k + 6 + (size * i), l + 29 + size, size == 36 ? 0 : 36, 45, size, size);
	        		
	               	if(showInv)
	            	{
		        		if(lastInv < itemsList.size())
		        		{
	        				drawItemStack(itemsList.get(lastInv), k + 6 + (size * i) + 1, l + 29 + size + 1);
	        		        lastInv++;
		            		this.mc.getTextureManager().bindTexture(texIcons);
		        		}
	            	}
	        	}
	        }
        }
        GL11.glPopMatrix();

        RendererHelper.startGlScissor(guiLeft + 125, guiTop + 17, 108, 54);

        int size = 36;
        int columnWidth = 108 / size;
        int slotsToDraw = ourHatsForTrade.size();
        
        while(slotsToDraw % 3 != 0 || slotsToDraw < 3)
        {
        	slotsToDraw++;
        }
        
        int hatLevels = (int)Math.ceil((float)slotsToDraw / 3F);
        
        GL11.glPushMatrix();
        
        int boxes = hatLevels * 2 + (int)Math.ceil((float)Math.max(ourItemsForTrade.size(), 6) / 6F);
        
        GL11.glTranslatef(0.0F, (-boxes * 18F + 54) * selfScrollProg, 0.0F);
        
        for(int i = 0; i < slotsToDraw; i++)
        {
        	this.drawTexturedModalRect(k + 125 + (size * (i % columnWidth)), l + 17 + (size * (int)(Math.floor(i / columnWidth))), size == 36 ? 0 : 36, 45, size, size);
        	
        	if(i < ourHatsForTrade.size())
        	{
    			GL11.glPushMatrix();

                GL11.glTranslatef((float)(k + 125 + (size * (i % columnWidth)) - 2), (float)(l + 17 + (size * (int)(Math.floor(i / columnWidth))) + 14), -3.0F + this.zLevel);

	            GL11.glScalef(20.0F, 20.0F, 20.0F);

                GL11.glTranslatef(1.0F, 0.5F, 1.0F);
                GL11.glScalef(1.0F, 1.0F, -1.0F);
                GL11.glRotatef(190.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
    			
    			HatInfoClient info = new HatInfoClient(ourHatsForTrade.get(i).toLowerCase());
    			HatRendererHelper.renderHat(info, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0000000F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, true, true, 1.0F);
    			GL11.glPopMatrix();
        	}
        	
        	this.mc.getTextureManager().bindTexture(texIcons);
        }
        
        size = 18;
        columnWidth = 108 / size;
        slotsToDraw = ourItemsForTrade.size();
        
        while(slotsToDraw % 6 != 0 || slotsToDraw < 6)
        {
        	slotsToDraw++;
        }
        
        for(int i = 0; i < slotsToDraw; i++)
        {
        	this.drawTexturedModalRect(k + 125 + (size * (i % columnWidth)), l + 17 + (size * (int)(Math.floor(i / columnWidth)) + 36 * hatLevels), size == 36 ? 0 : 36, 45, size, size);
        	
        	if(i < ourItemsForTrade.size())
        	{
				drawItemStack(ourItemsForTrade.get(i), k + 125 + (size * (i % columnWidth)) + 1, l + 17 + (size * (int)(Math.floor(i / columnWidth)) + 36 * hatLevels + 1));
	        	this.mc.getTextureManager().bindTexture(texIcons);
        	}
        }

        GL11.glPopMatrix();

        RendererHelper.startGlScissor(guiLeft + 125, guiTop + 116, 108, 54);
        
        size = 36;
        columnWidth = 108 / size;
        slotsToDraw = theirHatsForTrade.size();
        
        while(slotsToDraw % 3 != 0 || slotsToDraw < 3)
        {
        	slotsToDraw++;
        }
        
        hatLevels = (int)Math.ceil((float)slotsToDraw / 3F);
        
        GL11.glPushMatrix();
        
        boxes = hatLevels * 2 + (int)Math.ceil((float)Math.max(theirItemsForTrade.size(), 6) / 6F);
        
        GL11.glTranslatef(0.0F, (-boxes * 18F + 54) * theirScrollProg, 0.0F);
        
        for(int i = 0; i < slotsToDraw; i++)
        {
        	this.drawTexturedModalRect(k + 125 + (size * (i % columnWidth)), l + 116 + (size * (int)(Math.floor(i / columnWidth))), size == 36 ? 0 : 36, 45, size, size);
        	
        	if(i < theirHatsForTrade.size())
        	{
    			GL11.glPushMatrix();

                GL11.glTranslatef((float)(k + 125 + (size * (i % columnWidth)) - 2), (float)(l + 116 + (size * (int)(Math.floor(i / columnWidth))) + 14), -3.0F + this.zLevel);

	            GL11.glScalef(20.0F, 20.0F, 20.0F);

                GL11.glTranslatef(1.0F, 0.5F, 1.0F);
                GL11.glScalef(1.0F, 1.0F, -1.0F);
                GL11.glRotatef(190.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
    			
    			HatInfoClient info = new HatInfoClient(theirHatsForTrade.get(i).toLowerCase());
    			HatRendererHelper.renderHat(info, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0000000F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, true, true, 1.0F);
    			GL11.glPopMatrix();
        	}
        	
        	this.mc.getTextureManager().bindTexture(texIcons);
        }
        
        size = 18;
        columnWidth = 108 / size;
        slotsToDraw = theirItemsForTrade.size();
        
        while(slotsToDraw % 6 != 0 || slotsToDraw < 6)
        {
        	slotsToDraw++;
        }
        
        for(int i = 0; i < slotsToDraw; i++)
        {
        	this.drawTexturedModalRect(k + 125 + (size * (i % columnWidth)), l + 116 + (size * (int)(Math.floor(i / columnWidth)) + 36 * hatLevels), size == 36 ? 0 : 36, 45, size, size);
        	
        	if(i < theirItemsForTrade.size())
        	{
				drawItemStack(theirItemsForTrade.get(i), k + 125 + (size * (i % columnWidth)) + 1, l + 116 + (size * (int)(Math.floor(i / columnWidth)) + 36 * hatLevels + 1));
	        	this.mc.getTextureManager().bindTexture(texIcons);
        	}
        }

        GL11.glPopMatrix();

        RendererHelper.endGlScissor();

        this.mc.getTextureManager().bindTexture(texTradeWindow);
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
         
        this.mc.getTextureManager().bindTexture(texIcons);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 37F * selfScrollProg, 0.0F);
        this.drawTexturedModalRect(k + 237, l + 18, selfCanScroll ? 54 : 66, 45, 12, 15); //scroll button is 15 wide //scroll bar is 37 long
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 37F * theirScrollProg, 0.0F);
        this.drawTexturedModalRect(k + 237, l + 117, theirCanScroll ? 54 : 66, 45, 12, 15); //scroll button is 15 wide //scroll bar is 37 long
        GL11.glPopMatrix();
        
        super.drawScreen(par1, par2, par3);
        
        fontRendererObj.drawString(StatCollector.translateToLocal("hats.trade.yourOfferings"), (int)(guiLeft + 125), (int)(guiTop + 6), 0x2c2c2c, false);
        fontRendererObj.drawString(StatCollector.translateToLocalFormatted("hats.trade.theirOfferings", trader ), (int)(guiLeft + 125), (int)(guiTop + 105), 0x2c2c2c, false);
        
        fontRendererObj.drawString(selfReady ? StatCollector.translateToLocal("hats.trade.tradeReady") : StatCollector.translateToLocal("hats.trade.tradeNotReady"), (int)((guiLeft + 187) - (fontRendererObj.getStringWidth(selfReady ? StatCollector.translateToLocal("hats.trade.tradeReady") : StatCollector.translateToLocal("hats.trade.tradeNotReady")) / 2)), (int)((guiTop + 83)), selfReady ? 0x81b63a : 0x790000, false); //0x790000
        
        fontRendererObj.drawString(theirReady ? StatCollector.translateToLocal("hats.trade.tradeReady") : StatCollector.translateToLocal("hats.trade.tradeNotReady"), (int)(guiLeft + 187 - (fontRendererObj.getStringWidth(theirReady ? StatCollector.translateToLocal("hats.trade.tradeReady") : StatCollector.translateToLocal("hats.trade.tradeNotReady")) / 2)), (int)(guiTop + 176), theirReady ? 0x517924 : 0x790000, false); //0x790000
        
        boolean hasItem = !(ourHatsForTrade.size() == 0 && ourItemsForTrade.size() == 0 && theirHatsForTrade.size() == 0 && theirItemsForTrade.size() == 0);

        GL11.glPushMatrix();
        float scale = 0.5F;
        GL11.glScalef(scale, scale, scale);
        fontRendererObj.drawString(hasItem ? (selfReady && theirReady ? (pointOfNoReturn ? (clickedMakeTrade ? StatCollector.translateToLocal("hats.trade.waitingForThem") : StatCollector.translateToLocal("hats.trade.waitingForYou")) : StatCollector.translateToLocal("hats.trade.bothReady")) : StatCollector.translateToLocal("hats.trade.waitingForReady") ) : StatCollector.translateToLocal("hats.trade.waitingForOffer"), (int)((guiLeft + 187) / scale - (fontRendererObj.getStringWidth(hasItem ? (selfReady && theirReady ? (pointOfNoReturn ? (clickedMakeTrade ? StatCollector.translateToLocal("hats.trade.waitingForThem") : StatCollector.translateToLocal("hats.trade.waitingForYou")) : StatCollector.translateToLocal("hats.trade.bothReady")) : StatCollector.translateToLocal("hats.trade.waitingForReady") ) : StatCollector.translateToLocal("hats.trade.waitingForOffer")) / 2)), (int)((guiTop + ySize - 10) / scale), -16777216, false);
        GL11.glPopMatrix();
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.0F, 0.0F, 75F);
        zLevel += 100F;
        if(selfReady)
        {
        	drawSolidRect(guiLeft + 6, guiTop + 29, 108, 36, 0, 0.4F);
        	drawSolidRect(guiLeft + 125, guiTop + 17, 108, 54, 0, 0.4F);
        }
        if(theirReady)
        {
        	drawSolidRect(guiLeft + 125, guiTop + 116, 108, 54, 0, 0.4F);
        }
        zLevel -= 100F;
        GL11.glTranslatef(0.0F, 0.0F, -75F);
        GL11.glDisable(GL11.GL_BLEND);
        
		for(int i = 0; i < buttonList.size(); i++)
		{
			GuiButton btn = (GuiButton)buttonList.get(i);
			if(btn.id == ID_MAKE_TRADE)
			{
				btn.enabled = selfReady && theirReady && !clickedMakeTrade;
			}
		}
        
        drawSearchBar();
        
        drawChat();
        
        drawForeground(par1, par2, par3);
    }
    
    public void drawItemStack(ItemStack itemstack, int par2, int par3)
    {
        if (itemstack != null)
        {
        	GL11.glTranslatef(0.0F, 0.0F, 50.0F);
        	if(itemstack == grabbedStack)
        	{
        		GL11.glTranslatef(0.0F, 0.0F, 50.0F);
        	}
	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	        RenderHelper.enableGUIStandardItemLighting();
            itemRenderer.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), itemstack, par2, par3);
            itemRenderer.renderItemOverlayIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), itemstack, par2, par3);
	        RenderHelper.disableStandardItemLighting();
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        	if(itemstack == grabbedStack)
        	{
        		GL11.glTranslatef(0.0F, 0.0F, -50.0F);
        	}
        	GL11.glTranslatef(0.0F, 0.0F, -50.0F);
        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton btn)
    {
    	if(grabbedStack == null)
    	{
	    	if(btn.id == ID_TOGGLE_HATINV)
	    	{
	    		showInv = !showInv;
	    		btn.displayString = showInv ? StatCollector.translateToLocal("hats.trade.yourInventory") : StatCollector.translateToLocal("hats.trade.yourHats");
	    		searchBar.setText("");
	    		for(int i = 0; i < buttonList.size(); i++)
	    		{
	    			GuiButton btn1 = (GuiButton)buttonList.get(i);
	    			if(btn1 instanceof GuiSlider)
	    			{
	    				((GuiSlider)btn1).sliderValue = 0.0D;
	    				((GuiSlider)btn1).updateSlider();
	    			}
	    		}
	    	}
	    	else if(btn.id == ID_MAKE_READY)
	    	{
	            boolean hasItem = !(ourHatsForTrade.size() == 0 && ourItemsForTrade.size() == 0 && theirHatsForTrade.size() == 0 && theirItemsForTrade.size() == 0);
	
	            if(hasItem && !pointOfNoReturn)
	            {
	            	selfReady = !selfReady;

                    PacketHandler.sendToServer(Hats.channels, new PacketPing(3, selfReady));
	            }
	    	}
	    	else if(btn.id == ID_MAKE_TRADE)
	    	{
	    		pointOfNoReturn = true;
	    		clickedMakeTrade = true;

                PacketHandler.sendToServer(Hats.channels, new PacketPing(4, false));
	    	}
    	}
    }
    
	@Override
	public void onChangeSliderValue(GuiSlider slider) 
	{
		sliderProg = slider.getValue();
	}
	
    public void drawSolidRect(int par0, int par1, int par2, int par3, int par4, float alpha)
    {
        float f1 = (float)(par4 >> 16 & 255) / 255.0F;
        float f2 = (float)(par4 >> 8 & 255) / 255.0F;
        float f3 = (float)(par4 & 255) / 255.0F;
        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(f1, f2, f3, alpha);
        tessellator.startDrawingQuads();
        tessellator.addVertex((double)(par0 + 0), (double)(par1 + par3), (double)this.zLevel);
        tessellator.addVertex((double)(par0 + par2), (double)(par1 + par3), (double)this.zLevel);
        tessellator.addVertex((double)(par0 + par2), (double)(par1 + 0), (double)this.zLevel);
        tessellator.addVertex((double)(par0 + 0), (double)(par1 + 0), (double)this.zLevel);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
    
    public void drawSearchBar()
    {
    	if(searchBar.getVisible())
    	{
	    	searchBar.drawTextBox();
    	}
    }
    
    public void drawChat()
    {
    	if(chatBar.getVisible())
    	{
	    	chatBar.drawTextBox();
    	}

        RendererHelper.startGlScissor(guiLeft + 6, guiTop + 113, 101, 97);

		GL11.glPushMatrix();
		float scale = 0.5F; 
        GL11.glScalef(scale, scale, scale);
        int lines = 0; //max 12 before require scroll
    	for(int i = 0; i < chatMessages.size(); i++)
    	{
     		String msg = chatMessages.get(i);
    		
            List list = fontRendererObj.listFormattedStringToWidth(msg, 196);
    		
            lines += list.size();
    	}
    	
    	if(lines > 19)
    	{
    		GL11.glTranslatef(0.0F, -(lines - 19) * 10.0F * (1.0F - chatScroll), 0.0F);
    	}
    	
    	lines = 0;
    	
    	for(int i = 0; i < chatMessages.size(); i++)
    	{
     		String msg = chatMessages.get(i);
    		
            List list = fontRendererObj.listFormattedStringToWidth(msg, 196);
    		
    		for(int kk = 0; kk < list.size(); kk++)
    		{
    			String[] split = msg.split(" ");
    			
    			if(msg.startsWith(StatCollector.translateToLocal("hats.trade.terminatePrefix")))
    			{
    				if(kk == 0)
    				{
    					fontRendererObj.drawString(" " + (String)list.get(kk), (int)((guiLeft + 8) / scale), (int)((guiTop + 115 + (lines * 5)) / scale), 0x790000, false);
    				}
    				else
    				{
    					fontRendererObj.drawString(" " + (String)list.get(kk), (int)((guiLeft + 8) / scale), (int)((guiTop + 115 + (lines * 5)) / scale), 0x790000, false);
    				}
    			}
    			else
    			{
	    			if(kk == 0)
	    			{
	    				String line = (String)list.get(kk);
			    		String prefix = "";
			    		if(line.startsWith(mc.thePlayer.getCommandSenderName()) && line.substring(mc.thePlayer.getCommandSenderName().length()).startsWith(":"))
			    		{
			    			prefix = mc.thePlayer.getCommandSenderName() + ":";
			    		}
			    		if(line.startsWith(trader) && line.substring(trader.length()).startsWith(":"))
			    		{
			    			prefix = trader + ":";
			    		}
			
			    		fontRendererObj.drawString(prefix, (int)((guiLeft + 8) / scale), (int)((guiTop + 115 + (lines * 5)) / scale), 0, false);
			    		fontRendererObj.drawString(line.substring(prefix.length()), (int)((guiLeft + 8) / scale + fontRendererObj.getStringWidth(prefix)), (int)((guiTop + 115 + (lines * 5)) / scale), 14737632, false);
	    			}
	    			else
	    			{
	    				fontRendererObj.drawString(" " + (String)list.get(kk), (int)((guiLeft + 8) / scale), (int)((guiTop + 115 + (lines * 5)) / scale), 14737632, false);
	    			}
    			}
    	   		lines++;
    		}
    	}

        RendererHelper.endGlScissor();

    	GL11.glPopMatrix();
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	if(lines > 19)
    	{
    		GL11.glPushMatrix();
    		GL11.glTranslatef(0.0F, -82F * chatScroll, 0.0F);
    		this.mc.getTextureManager().bindTexture(texIcons);
    		drawTexturedModalRect(guiLeft + 107, guiTop + 195, 78, 45, 4, 15);
    		GL11.glPopMatrix();
    	}
    }
    
    protected void drawTooltip(List par1List, int par2, int par3)
    {
        if (!par1List.isEmpty())
        {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;
            Iterator iterator = par1List.iterator();

            while (iterator.hasNext())
            {
                String s = (String)iterator.next();
                int l = this.fontRendererObj.getStringWidth(s);

                if (l > k)
                {
                    k = l;
                }
            }

            int i1 = par2 + 12;
            int j1 = par3 - 12;
            int k1 = 8;

            if (par1List.size() > 1)
            {
                k1 += 2 + (par1List.size() - 1) * 10;
            }

            if (i1 + k > this.width)
            {
                i1 -= 28 + k;
            }

            if (j1 + k1 + 6 > this.height)
            {
                j1 = this.height - k1 - 6;
            }

            this.zLevel = 300.0F;
            int l1 = -267386864;
            this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
            this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
            int i2 = 1347420415;
            int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
            this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
            this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

            for (int k2 = 0; k2 < par1List.size(); ++k2)
            {
                String s1 = (String)par1List.get(k2);
                this.fontRendererObj.drawStringWithShadow(s1, i1, j1, -1);

                if (k2 == 0)
                {
                    j1 += 2;
                }

                j1 += 10;
            }

            this.zLevel = 0.0F;
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }
}
