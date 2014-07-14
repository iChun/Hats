package hats.addons.hatstand.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import hats.addons.hatstand.client.render.BlockRenderHatStand;
import hats.addons.hatstand.client.render.TileRendererHatStand;
import hats.addons.hatstand.common.HatStand;
import hats.addons.hatstand.common.block.BlockHatStand;
import hats.addons.hatstand.common.packet.PacketStandHatInfo;
import hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import hats.client.core.HatInfoClient;
import hats.common.Hats;
import hats.common.core.HatHandler;
import ichun.client.gui.GuiSlider;
import ichun.client.gui.ISlider;
import ichun.common.core.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.*;

public class GuiHatSelection extends GuiScreen 
	implements ISlider
{
	private final int ID_PAGE_LEFT = 1;
	private final int ID_DONE_SELECT = 2;
	private final int ID_PAGE_RIGHT = 3;
	private final int ID_CLOSE = 4;
	// 5 6 7 8 are slider IDs

	private final int ID_NONE = 8;
	private final int ID_HAT_COLOUR_SWAP = 9;
	private final int ID_RANDOM = 10;
	
	private final int ID_HEAD = 11;
	private final int ID_BASE = 12;
	private final int ID_STAND = 13;
	
	private final int ID_HAT_START_ID = 600;
	
	private final int VIEW_HATS = 0;
	private final int VIEW_COLOURIZER = 1;
	
	private String currentDisplay;
	private GuiTextField searchBar;
	private String selectedButtonName = "";
	private int favourite;
	
	private boolean hasClicked = false;
	private boolean confirmed = false;
	private boolean justClickedButton = false;
	
	private boolean enabledSearchBar = false;
	
	public TileEntityHatStand stand;
	public List<String> availableHats;
	public List<String> hatsToShow;
	
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
    public int alpha;
	
	public int head;
	public boolean base;
	public boolean standPost;
	
	private String prevHatName;
	private int prevColourR;
	private int prevColourG;
	private int prevColourB;
    private int prevAlpha;
	
	private int prevHead;
	private GameProfile prevGameProfile;
	private boolean prevBase;
	private boolean prevStandPost;
	
	private HatInfoClient tempInfo;
	
	public int view;
	
	public Random rand;
	
	public GuiHatSelection(TileEntityHatStand hatStand)
	{
		if(Hats.config.getSessionInt("playerHatsMode") == 4)
		{
			if(Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode)
			{
				HatHandler.repopulateHatsList();
			}
			else
			{
				Hats.proxy.tickHandlerClient.availableHats = new HashMap<String, Integer>(Hats.proxy.tickHandlerClient.serverHats);
			}
		}
        ArrayList<String> list = new ArrayList<String>();

        for(Map.Entry<String, Integer> e : Hats.proxy.tickHandlerClient.availableHats.entrySet())
        {
            list.add(e.getKey());
        }
        Collections.sort(list);

        availableHats = ImmutableList.copyOf(list);
		hatsToShow = new ArrayList<String>(availableHats);
		Collections.sort(hatsToShow);
		stand = hatStand;
		prevHatName = stand.hatName;
		prevGameProfile = stand.gameProfile;
		prevColourR = colourR = stand.colourR;
		prevColourG = colourG = stand.colourG;
		prevColourB = colourB = stand.colourB;
        prevAlpha = alpha = stand.alpha;
		prevHead = head = stand.head;
		prevBase = base = stand.hasBase;
		prevStandPost = standPost = stand.hasStand;
		pageNumber = 0;
		view = VIEW_HATS;
		rand = new Random();
		enabledSearchBar = true;
	}
	
	@Override
	public void initGui()
	{
        super.initGui();

        if(stand == null)
		{
			mc.displayGuiScreen(null);
		}
		else
		{
	        Keyboard.enableRepeatEvents(true);

			buttonList.clear();
			
	        this.guiLeft = (this.width - this.xSize) / 2;
	        this.guiTop = (this.height - this.ySize) / 2;
	        
	        buttonList.add(new GuiButton(ID_PAGE_LEFT, width / 2 - 6, height / 2 + 54, 20, 20, "<"));
	        buttonList.add(new GuiButton(ID_PAGE_RIGHT, width / 2 + 62, height / 2 + 54, 20, 20, ">"));
	        buttonList.add(new GuiButton(ID_DONE_SELECT, width / 2 + 16, height / 2 + 54, 44, 20, I18n.format("gui.done")));
	        
	        //4, 5, 6, 7, 8 = taken.
	        
	        addToolButton(ID_NONE, 0);
	        addToolButton(ID_HAT_COLOUR_SWAP, 1);
	        addToolButton(ID_RANDOM, 2);
	        
	        buttonList.add(new GuiButton(ID_CLOSE, width - 22, 2, 20, 20, "X"));
	        
	        pageNumber = 0;
	        
	        if(!stand.hatName.equalsIgnoreCase(""))
	        {
	        	for(int i = 0; i < hatsToShow.size(); i++)
		        {
		        	String hatName = (String)hatsToShow.get(i);
		        	if(hatName.equalsIgnoreCase(stand.hatName))
		        	{
		        		i -= i % 6;
		        		pageNumber = i / 6;
		        		break;
		        	}
		        }
	        }
	        
	        updateButtonList();
	        
			searchBar = new GuiTextField(this.fontRendererObj, this.width / 2 - 65, height - 24, 150, 20);
			searchBar.setMaxStringLength(255);
			searchBar.setText(StatCollector.translateToLocal("hats.gui.search"));
			searchBar.setTextColor(0xAAAAAA);
			searchBar.setVisible(enabledSearchBar);
		}
	}
	
	public void addToolButton(int id, int i)
	{
		buttonList.add(new GuiButton(id, width / 2 + 89, height / 2 - 85 + (i * 21), 20, 20, ""));
	}
	
	@Override
    public void updateScreen()
    {
        searchBar.updateCursorCounter();
        if(searchBar.isFocused())
        {
        	searchBar.setVisible(true);
        }
        else
        {
        	searchBar.setVisible(enabledSearchBar);
        }
        if(favourite > 0)
        {
        	favourite--;
        }
    }
	
	@Override
	public void onGuiClosed() 
	{
		if(!confirmed)
		{
			stand.hatName = prevHatName;
			stand.colourR = prevColourR;
			stand.colourG = prevColourG;
			stand.colourB = prevColourB;
            stand.alpha = prevAlpha;
			
			stand.head = prevHead;
			stand.gameProfile = prevGameProfile;
			stand.hasBase = prevBase;
			stand.hasStand = prevStandPost;

			stand.getWorldObj().markBlockForUpdate(stand.xCoord, stand.yCoord, stand.zCoord);
		}
        Keyboard.enableRepeatEvents(false);
	}
	
    @Override
    protected void keyTyped(char c, int i)
    {
    	searchBar.textboxKeyTyped(c, i);
    	if(searchBar.isFocused())
    	{
    		onSearch();
    	}
        if (i == 1)
        {
	    	if(searchBar.isFocused())
	    	{
	    		searchBar.setText("");
	    		searchBar.setFocused(false);
	    		onSearchBarInteract();
	    	}
	    	else
	    	{
	        	exitWithoutUpdate();
	        	
	            this.mc.setIngameFocus();
	    	}
        }
        if(!searchBar.isFocused())
        {
        	GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
	        if(i == Keyboard.KEY_R)
	        {
	        	mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
	        	randomize();
	        }
	        else if(i == Keyboard.KEY_TAB || i == gameSettings.keyBindChat.getKeyCode())
	        {
	        	searchBar.setFocused(true);
	        	onSearchBarInteract();
	        }
	        else if(i == Keyboard.KEY_H && !(stand.hatName.equalsIgnoreCase("") && view == VIEW_HATS))
	        {
	        	mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
	        	toggleHatsColourizer();
	        }
	        else if(i == Keyboard.KEY_N && !stand.hatName.equalsIgnoreCase(""))
	        {
	        	mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
	        	removeHat();
	        }
	        if(view == VIEW_HATS)
	        {
		        if((i == gameSettings.keyBindLeft.getKeyCode() || i == Keyboard.KEY_LEFT) && pageNumber > 0)
		        {
		        	mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
		        	switchPage(true);
		        }
		        else if((i == gameSettings.keyBindRight.getKeyCode() || i == Keyboard.KEY_RIGHT) && !((pageNumber + 1) * 6 >= hatsToShow.size()))
		        {
		        	mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
		        	switchPage(false);
		        }
	        }
        }
    }
    
    @Override
    protected void mouseClicked(int par1, int par2, int par3)
    {
    	super.mouseClicked(par1, par2, par3);
    	boolean flag = par1 >= (this.width / 2 - 65) && par1 < (this.width / 2 - 65) + this.width && par2 >= (height - 24) && par2 < (height - 24) + this.height;
    	if(enabledSearchBar)
    	{
    		searchBar.mouseClicked(par1, par2, par3);

    		if(par3 == 1 && flag)
    		{
    			searchBar.setText("");
    			onSearch();
    		}
    		onSearchBarInteract();
    	}
    }
    
    @Override
    protected void mouseMovedOrUp(int par1, int par2, int par3)
    {
    	super.mouseMovedOrUp(par1, par2, par3);
    	justClickedButton = false;
    }
    
    public void onSearch()
    {
    	if(searchBar.getText().equalsIgnoreCase("") || !hasClicked && searchBar.getText().equalsIgnoreCase("Search"))
    	{
    		searchBar.setTextColor(14737632);
   			hatsToShow = new ArrayList<String>(availableHats);
    		Collections.sort(hatsToShow);
    	}
    	else
    	{
    		String query = searchBar.getText();
    		ArrayList<String> matches = new ArrayList<String>();
    		for(String s : (availableHats))
    		{
    			if(s.toLowerCase().startsWith(query.toLowerCase()))
    			{
					if(!matches.contains(s))
					{
						matches.add(s);
					}
    			}
    			else
    			{
	    			String[] split = s.split(" ");
	    			for(String s1 : split)
	    			{
	    				if(s1.toLowerCase().startsWith(query.toLowerCase()))
	    				{
	    					if(!matches.contains(s))
	    					{
	    						matches.add(s);
	    					}
	    					break;
	    				}
	    			}
    			}
    		}
    		if(matches.size() == 0)
    		{
    			searchBar.setTextColor(0xFF5555);
        		hatsToShow = new ArrayList<String>(availableHats);
        		Collections.sort(hatsToShow);
    		}
    		else
    		{
    			searchBar.setTextColor(14737632);
    			pageNumber = 0;
    			hatsToShow = new ArrayList<String>(matches);
    			Collections.sort(hatsToShow);
    		}
    	}
    	
    	updateButtonList();
    }
    
    public void onSearchBarInteract()
    {
        if(searchBar.isFocused())
        {
        	searchBar.setTextColor(14737632);
        	if(!hasClicked && searchBar.getText().equalsIgnoreCase(StatCollector.translateToLocal("hats.gui.search")))
        	{
        		hasClicked = true;
        		searchBar.setText("");
        		onSearch();
        	}
        }
        else
        {
        	searchBar.setTextColor(0xAAAAAA);
        	if(searchBar.getText().equalsIgnoreCase(""))
        	{
        		hasClicked = false;
    			searchBar.setText(StatCollector.translateToLocal("hats.gui.search"));
        	}
        }    	
    }
    
    @Override
    protected void actionPerformed(GuiButton btn)
    {
    	if(!justClickedButton)
    	{
	    	if(btn.id == ID_DONE_SELECT)
	    	{
	   			exitAndUpdate();
	    	}
	    	if(btn.id == ID_PAGE_LEFT)
	    	{
	    		switchPage(true);
	    	}
	    	else if(btn.id == ID_PAGE_RIGHT)
	    	{
	    		switchPage(false);
	    	}
	    	else if(btn.id == ID_CLOSE)
	    	{
	    		exitWithoutUpdate();
	    	}
	    	else if(btn.id == ID_NONE)
	    	{
	    		removeHat();
	    	}
	    	else if(btn.id == ID_HAT_COLOUR_SWAP)
	    	{
	    		toggleHatsColourizer();
	    	}
	    	else if(btn.id == ID_RANDOM)
	    	{
	    		randomize();
	    	}
	    	else if(btn.id == ID_HEAD)
	    	{
	    		stand.head++;
	    		if(stand.head == TileEntityHatStand.headNames.length)
	    		{
	    			stand.head = 0;
	    		}
	    		if(stand.head == 4)
	    		{
	    			stand.gameProfile = Minecraft.getMinecraft().thePlayer.getGameProfile();
	    		}
	    		head = stand.head;
	    		justClickedButton = true;
	    		
	    		updateButtonList();
	    	}	
	    	else if(btn.id == ID_BASE)
	    	{
	    		base = !base;
	    		stand.hasBase = base;
	    		justClickedButton = true;
	    		
	    		if(!base)
	    		{
	    			stand.hasStand = standPost = false;
	    		}
	    		
	    		stand.getWorldObj().markBlockForUpdate(stand.xCoord, stand.yCoord, stand.zCoord);

	    		updateButtonList();
	    	}
	    	else if(btn.id == ID_STAND)
	    	{
	    		standPost = !standPost;
	    		stand.hasStand = standPost;
	    		justClickedButton = true;
	    		
	    		stand.getWorldObj().markBlockForUpdate(stand.xCoord, stand.yCoord, stand.zCoord);
	    		
	    		updateButtonList();
	    	}
	    	else if(btn.id >= ID_HAT_START_ID)
	    	{
	    		justClickedButton = true;
	    		stand.hatName = btn.displayString.toLowerCase();
	    		
	    		colourR = colourG = colourB = alpha = 255;
	    		stand.colourR = 255;
	    		stand.colourG = 255;
	    		stand.colourB = 255;
                stand.alpha = 255;
	    		
	    		updateButtonList();
	    	}
    	}
    }
    
    public void exitAndUpdate()
    {
    	confirmed = true;
    	
		mc.displayGuiScreen(null);

        PacketHandler.sendToServer(HatStand.channels, new PacketStandHatInfo(stand.xCoord, stand.yCoord, stand.zCoord, stand.hatName, colourR, colourG, colourB, alpha, head, base, standPost));
    }
    
    public void exitWithoutUpdate()
    {
		mc.displayGuiScreen(null);
    	
		stand.hatName = prevHatName;
		stand.colourR = prevColourR;
		stand.colourG = prevColourG;
		stand.colourB = prevColourB;
		
		stand.head = prevHead;
		stand.gameProfile = prevGameProfile;
		stand.hasBase = prevBase;
		stand.hasStand = prevStandPost;
		
		stand.getWorldObj().markBlockForUpdate(stand.xCoord, stand.yCoord, stand.zCoord);
    }
    
    public void updateButtonList()
    {
        for (int k1 = buttonList.size() - 1; k1 >= 0; k1--)
        {
            GuiButton btn = (GuiButton)this.buttonList.get(k1);
            
            if(btn.id >= 5 && btn.id <= 7 || btn.id == 15 || btn.id >= 11 && btn.id <= 13 || btn.id >= ID_HAT_START_ID)
            {
            	buttonList.remove(k1);
            }
            else if(btn.id == ID_PAGE_LEFT)
            {
	            if(pageNumber == 0 || view == VIEW_COLOURIZER)
	            {
	            	btn.enabled = false;
	            }
	            else
	            {
	            	btn.enabled = true;
	            }
            }
            else if(btn.id == ID_PAGE_RIGHT)
            {
        		if((pageNumber + 1) * 6 >= hatsToShow.size() || view == VIEW_COLOURIZER)
        		{
        			btn.enabled = false;
        		}
	            else
	            {
	            	btn.enabled = true;
	            }
            }
            else if(btn.id == ID_NONE)
            {
            	if(stand.hatName.equalsIgnoreCase(""))
            	{
            		btn.enabled = false;
            	}
            	else
            	{
            		btn.enabled = true;
            	}
            }
        }
        
    	if(view == VIEW_HATS)
    	{
	    	int button = 0;
	
	        for(int i = pageNumber * 6; i < hatsToShow.size() && i < (pageNumber + 1) * 6; i++)
	        {
	        	GuiButton btn;
	        	String hatName = (String)hatsToShow.get(i);
	        	
	        	btn = new GuiButton(ID_HAT_START_ID + i, width / 2 - 6, height / 2 - 78 + (22 * button), 88, 20, hatName);
	        	
	        	if((view == VIEW_HATS) && hatName.toLowerCase().equalsIgnoreCase(stand.hatName))
	        	{
	        		btn.enabled = false;
	        	}
	        
	        	buttonList.add(btn);
	        	
	        	button++;
	        	if(button == 6)
	        	{
	        		button = 0;
	        		break;
	        	}
	        }
	        
	        int pageCount = (int)Math.ceil((float)hatsToShow.size() / 6F);
	        if(pageCount <= 0)
	        {
	        	pageCount = 1;
	        }
	        currentDisplay = StatCollector.translateToLocal("hats.gui.allHats") + " (" + (pageNumber + 1) + "/" + (pageCount) + ")";
    	}
    	else if(view == VIEW_COLOURIZER)
    	{
    		int button = 0;
    		
    		for(int i = 0; i < 4; i++)
    		{
                GuiButton btn = new GuiSlider(i == 3 ? 15 : 5 + i, width / 2 - 6, height / 2 - 78 + (22 * button), 88, i == 0 ? StatCollector.translateToLocal("item.fireworksCharge.red") + ": " : i == 1 ? StatCollector.translateToLocal("item.fireworksCharge.green") + ": " : i == 2 ? StatCollector.translateToLocal("item.fireworksCharge.blue") + ": " : StatCollector.translateToLocal("hats.gui.alpha") + ": ", "", 0, 255, i == 0 ? colourR : i == 1 ? colourG : i == 2 ? colourB : alpha, false, true, this);
    			buttonList.add(btn);
    			
    			button++;
    		}
    		
    		buttonList.add(new GuiButton(ID_HEAD, width / 2 - 6, height / 2 - 78 + (22 * button), 88, 20, StatCollector.translateToLocal("item.skull.char.name") + ": " + TileEntityHatStand.headNames[stand.head]));
    		button++;
    		
    		buttonList.add(new GuiButton(ID_BASE, width / 2 - 6, height / 2 - 78 + (22 * button), 88, 20, StatCollector.translateToLocal("hats.addon.hatstands.gui.base") + ": " + (base ? StatCollector.translateToLocal("gui.yes") : StatCollector.translateToLocal("gui.no"))));
    		button++;
    		
    		GuiButton btn;
    		btn = new GuiButton(ID_STAND, width / 2 - 6, height / 2 - 78 + (22 * button), 88, 20, StatCollector.translateToLocal("hats.addon.hatstands.gui.stand") + ": " + (standPost ? StatCollector.translateToLocal("gui.yes") : StatCollector.translateToLocal("gui.no")));
    		
    		if(!stand.isOnFloor || !base)
    		{
    			btn.enabled = false;
    		}
    		
    		buttonList.add(btn);
			
			button++;
    		
    		currentDisplay = StatCollector.translateToLocal("hats.gui.personalize");
    	}
    }
    
    public void removeHat()
    {
		stand.hatName = "";
		
		updateButtonList();
    }
    
    public void switchPage(boolean left)
    {
    	if(left)
    	{
    		pageNumber--;
    		if(pageNumber < 0)
    		{
    			pageNumber = 0;
    		}
    		updateButtonList();
    	}
    	else
    	{
    		pageNumber++;
    		if(pageNumber * 6 >= hatsToShow.size())
    		{
    			pageNumber--;
    		}
    		updateButtonList();
    	}
    }
    
    public void toggleHatsColourizer()
    {
    	view = view > VIEW_HATS ? VIEW_HATS : VIEW_COLOURIZER;
		
		hatsToShow = new ArrayList<String>(availableHats);
		Collections.sort(hatsToShow);

		searchBar.setText("");
		onSearchBarInteract();

		updateButtonList();
    }
    
    public void randomize()
    {
		if(view == VIEW_HATS)
		{
	    	if(hatsToShow.size() > 0)
	    	{
				int randVal = rand.nextInt(hatsToShow.size());
	        	String hatName = (String)hatsToShow.get(randVal);
	        	
	        	stand.hatName = hatName.toLowerCase();
	        	
	    		colourR = colourG = colourB = alpha = 255;
	    		stand.colourR = 255;
	    		stand.colourG = 255;
	    		stand.colourB = 255;
                stand.alpha = 255;
	        	
	    		pageNumber = randVal / 6;
	    		
	    		if(isShiftKeyDown())
	    		{
	    			view = VIEW_COLOURIZER;
	    			
	    			updateButtonList();
	    			
	    			randomizeColour();
	    			
	    			view = VIEW_HATS;
	    		}
	    		
	    		updateButtonList();
	    	}
		}
		else if(view == VIEW_COLOURIZER)
		{
			if(isShiftKeyDown())
			{
	    		colourR = colourG = colourB = alpha = 255;
	    		stand.colourR = 255;
	    		stand.colourG = 255;
	    		stand.colourB = 255;
                stand.alpha = 255;

	    		updateButtonList();
			}
			else
			{
				randomizeColour();
			}
		}
    }
    
    public void randomizeColour()
    {
		for (int k1 = buttonList.size() - 1; k1 >= 0; k1--)
		{
			GuiButton btn1 = (GuiButton)this.buttonList.get(k1);
			if(btn1 instanceof GuiSlider)
			{
				GuiSlider slider = (GuiSlider)btn1;
				if(slider.id >= 5 && slider.id <= 7)
				{
					if(stand.hatName.equalsIgnoreCase(""))
					{
						slider.sliderValue = 0.0F;
					}
					else
					{
						slider.sliderValue = rand.nextFloat();
					}
					slider.updateSlider();
				}
			}
		}
    }
    
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
    	drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(hats.client.gui.GuiHatSelection.texChooser);
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
        
        this.mc.getTextureManager().bindTexture(hats.client.gui.GuiHatSelection.texIcons);

        for (int k1 = 0; k1 < this.buttonList.size(); ++k1)
        {
            GuiButton btn = (GuiButton)this.buttonList.get(k1);
            
            String disp = btn.displayString;
            
            if(btn.id >= ID_HAT_START_ID)
            {
            	int id = btn.id - ID_HAT_START_ID; 
            	if(!((pageNumber) * 6 <= id && (pageNumber + 1) * 6 > id))
            	{
            		continue;
            	}
            	
            	if(btn.displayString.length() > 16)
            	{
            		btn.displayString = btn.displayString.substring(0, 13) + "...";
            	}
            }
            btn.drawButton(this.mc, par1, par2);
            
            if(!(btn instanceof GuiSlider))
            {
            	btn.displayString = disp;
            }
            
            if(btn.id == ID_HAT_COLOUR_SWAP || btn.id == ID_NONE || btn.id == ID_RANDOM)
            {
            	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            GL11.glEnable(GL11.GL_BLEND);
	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	            
	            this.mc.getTextureManager().bindTexture(hats.client.gui.GuiHatSelection.texIcons);

	            if(btn.visible)
	            {
	            	if(btn.id == ID_HAT_COLOUR_SWAP)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, (view == VIEW_HATS ? 176 : 0), 0, 16, 16);
	            	}
	            	else if(btn.id == ID_NONE)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 32, 0, 16, 16);
	            	}
	            	else if(btn.id == ID_RANDOM)
	            	{
	            		drawTexturedModalRect(btn.xPosition + 2, btn.yPosition + 2, 80, 0, 16, 16);
	            	}
	            }

            	GL11.glDisable(GL11.GL_BLEND);
            }
        }
        
        drawString(fontRendererObj, "Viewing: " + currentDisplay, this.guiLeft, this.guiTop - 9, 0xffffff);
    	
        this.mouseX = (float)par1;
        this.mouseY = (float)par2;

        drawSearchBar();
        
        drawPlayerOnGui(k + 42, l + 155, 55, (float)(k + 42) - (float)mouseX, (float)(l + 155 - 42) - (float)mouseY);

        drawForeground(par1, par2, par3);
    }
    
    public void drawForeground(int par1, int par2, float par3)
    {
        for (int k1 = 0; k1 < this.buttonList.size(); ++k1)
        {
            GuiButton btn = (GuiButton)this.buttonList.get(k1);
            if(btn.func_146115_a())
            {
	            if(btn.id >= ID_HAT_START_ID && btn.displayString.length() > 16)
	            {
	            	drawTooltip(Arrays.asList(new String[] {"\u00a77" + btn.displayString}), par1, par2);
	            }
	            else if(btn.id == ID_CLOSE)
	            {
	            	drawTooltip(Arrays.asList(new String[] {StatCollector.translateToLocal("hats.gui.discardChanges")}), par1, par2);
	            }
	            else if(btn.id == ID_NONE)
	            {
	            	drawTooltip(Arrays.asList(new String[] {StatCollector.translateToLocal("hats.gui.removeHat") + " (N)"}), par1, par2);
	            }
	            else if(btn.id == ID_HAT_COLOUR_SWAP)
	            {
	            	drawTooltip(Arrays.asList(new String[] {(view == VIEW_HATS ? StatCollector.translateToLocal("hats.gui.personalize") : StatCollector.translateToLocal("hats.gui.hatsList")) + " (H)"}), par1, par2);
	            }
	            else if(btn.id == ID_RANDOM)
	            {
	            	drawTooltip(Arrays.asList(new String[] {(view == VIEW_HATS ? StatCollector.translateToLocal("hats.gui.randomHat") : isShiftKeyDown() ? StatCollector.translateToLocal("hats.gui.resetColor"): StatCollector.translateToLocal("hats.gui.randomColor")) + " (R)"}), par1, par2);
	            }
            }
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

    
    public void drawPlayerOnGui(int par1, int par2, int par3, float par4, float par5)
    {
    	if(stand != null)
    	{
	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	        GL11.glPushMatrix();
	        
//	        GL11.glDisable(GL11.GL_ALPHA_TEST);
	        
	        GL11.glTranslatef((float)par1, (float)par2, 50.0F);
	        
	        GL11.glTranslatef(23F, -20.0F, 0.0F);
	        GL11.glScalef((float)(-par3), (float)par3, (float)par3);
	        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
	        
	        GL11.glRotatef(-80.0F, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(-10.0F, 0.0F, 0.0F, 1.0F);
//	        GL11.glRotatef(-((float)Math.atan((double)(0.0F / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
	        
	        RenderManager.instance.playerViewY = 180.0F;
	        
	        GL11.glTranslated(0.5D, 0.5D, 0.5D);
	        GL11.glTranslated(0.0D, -0.5D, 0.0D);
	        GL11.glRotatef(-((float)Math.atan((double)(par4 / 40.0F))) * 20.0F, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(-((float)Math.atan((double)(par5 / 40.0F))) * 20.0F, 0.0F, 0.0F, -1.0F);
	        GL11.glTranslated(0.0D, 0.5D, 0.0D);
	        if(stand.orientation == 0)
	        {
	        	GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
	        }
	        else if(stand.orientation == 1)
	        {
	        	GL11.glRotatef(-90F, 0.0F, 1.0F, 0.0F);
	        }
	        else if(stand.orientation == 3)
	        {
	        	GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
	        }
	        
	        TileRendererHatStand.renderBlocks.blockAccess = stand.getWorldObj();
	        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
	        BlockRenderHatStand.instance.renderInventoryBlockWithTileEntity((BlockHatStand)HatStand.blockHatStand, stand, TileRendererHatStand.renderBlocks);
//	        GL11.glTranslated(stand.xCoord, stand.yCoord, stand.zCoord);
	        
	        GL11.glTranslated(-0.5D, -0.5D, -0.5D);
	        
	        HatInfoClient info = stand.info;
	        
	        if(tempInfo == null || info == null || !(tempInfo.hatName.equalsIgnoreCase(stand.hatName) && tempInfo.colourR == stand.colourR && tempInfo.colourG == stand.colourG && tempInfo.colourB == stand.colourB && tempInfo.alpha == stand.alpha))
	        {
	        	tempInfo = new HatInfoClient(stand.hatName, stand.colourR, stand.colourG, stand.colourB, stand.alpha);
	        }
	        
	        stand.info = tempInfo;
	        
	        TileRendererHatStand.renderer.renderHatStand(stand, 0, 0, 0, 1.0F, stand.gameProfile);

	        stand.info = info;
	        
//	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	        
	        GL11.glPopMatrix();
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    	}
    }
    
    public void drawSearchBar()
    {
    	if(searchBar.getVisible())
    	{
	    	searchBar.drawTextBox();
	    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	
	        this.mc.getTextureManager().bindTexture(hats.client.gui.GuiHatSelection.texIcons);
	    	
	    	drawTexturedModalRect(this.width / 2 - 85, height - 22, 128, 0, 16, 16);
	    	
	    	GL11.glDisable(GL11.GL_BLEND);
    	}
    }

	@Override
	public void onChangeSliderValue(GuiSlider slider)
	{
		if(slider.id == 5)
		{
			colourR = (int)Math.round(slider.sliderValue * (slider.maxValue - slider.minValue) + slider.minValue);
			stand.colourR = colourR;
		}
		else if(slider.id == 6)
		{
			colourG = (int)Math.round(slider.sliderValue * (slider.maxValue - slider.minValue) + slider.minValue);
			stand.colourG = colourG;
		}
		else if(slider.id == 7)
		{
			colourB = (int)Math.round(slider.sliderValue * (slider.maxValue - slider.minValue) + slider.minValue);
			stand.colourB = colourB;
		}
        else if(slider.id == 15)
        {
            alpha = (int)Math.round(slider.sliderValue * (slider.maxValue - slider.minValue) + slider.minValue);
            stand.alpha = alpha;
        }
	}
}
