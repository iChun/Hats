package me.ichun.mods.hats.client.gui.window.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementFertile;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementScrollBar;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ElementHatsScrollView extends ElementFertile
{
    public List<Element<?>> elements = new ArrayList<>();
    private @Nullable ElementScrollBar<?> scrollVert;

    public boolean hasInit;

    public ElementHatsScrollView(@Nonnull Fragment parent)
    {
        super(parent);
    }

    public <T extends ElementHatsScrollView> T setScrollVertical(ElementScrollBar<?> scroll)
    {
        scrollVert = scroll;
        scrollVert.setCallback((scr) -> alignItems());
        return (T)this;
    }

    public Element<?> addElement(Element<?> e)
    {
        elements.add(e);

        if(hasInit)
        {
            alignItems();
            updateScrollBarSizes();
        }
        return e;
    }

    @Override
    public void init()
    {
        super.init();
        hasInit = true;
        alignItems();
        updateScrollBarSizes();
    }

    @Override
    public void resize(Minecraft mc, int width, int height)
    {
        super.resize(mc, width, height);//code is here twice to fix resizing when init
        alignItems();
        super.resize(mc, width, height);
        alignItems();
        updateScrollBarSizes();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        if(renderMinecraftStyle())
        {
            bindTexture(Fragment.VANILLA_HORSE);
            cropAndStitch(stack, getLeft() - 1, getTop() - 1, width + 2, height + 2, 2, 79, 17, 90, 54, 256, 256);
        }
        else
        {
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getLeft() - 1, getTop() - 1, width + 2, 1, 0); //top
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getLeft() - 1, getTop() - 1, 1, height + 2, 0); //left
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getLeft() - 1, getBottom(), width + 2, 1, 0); //bottom
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getRight(), getTop() - 1, 1, height + 2, 0); //right
        }

        setScissor();
        elements.forEach(item -> {
            if(item.getBottom() >= getTop() && item.getTop() < getBottom())
            {
                item.render(stack, mouseX, mouseY, partialTick);
            }
        });
        resetScissorToParent();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dist)
    {
        if(isMouseOver(mouseX, mouseY))
        {
            boolean defaultScroll = super.mouseScrolled(mouseX, mouseY, dist);
            if(defaultScroll)
            {
                return true;
            }
            else
            {
                if(scrollVert != null)
                {
                    scrollVert.secondHandScroll((dist * 70 / getTotalItemHeight()) * 2D);
                    return true;
                }
            }
        }
        return false;
    }

    public int getTotalItemHeight()
    {
        //calculate the total item width/height
        int padding = 3;
        int useableWidth = getWidth() - padding;
        int maxPerRow = (int)Math.floor(useableWidth / 50F); //this max hat renders we can have a row. 50 = min hat render width
        if(maxPerRow > elements.size())
        {
            maxPerRow = elements.size();
        }
        int rowCount = (int)Math.ceil(elements.size() / (float)maxPerRow);
        return (70 + padding) * rowCount + padding; //70 = min hat render height
    }

    public void alignItems() //TODO total item height func
    {
        //MATCH getTotalItemHeight()
        int padding = 3;
        int useableWidth = getWidth() - padding;
        int maxPerRow = (int)Math.floor(useableWidth / 50F); //this max hat renders we can have a row. 50 = min hat render width
        if(maxPerRow > elements.size())
        {
            maxPerRow = elements.size();
        }
        int rowCount = (int)Math.ceil(elements.size() / (float)maxPerRow);
        int totalItemHeight = (70 + padding) * rowCount + padding; //70 = min hat render height

        int offsetY = 0;
        if(scrollVert != null)
        {
            offsetY = (int)(Math.max(0, totalItemHeight - height) * scrollVert.scrollProg);
        }
        int offsetX = 0;

        int widthPerItem = (int)Math.floor(useableWidth / (float)maxPerRow) - padding;

        int currentWidth = 0; //we add some padding between elements
        int currentHeight = padding; //we add some padding between elements
        for(Element<?> item : elements)
        {
            currentWidth += padding;

            item.posX = currentWidth - offsetX;
            item.posY = currentHeight - offsetY;

            if(item.width != widthPerItem)
            {
                item.width = widthPerItem;
            }
            currentWidth += item.width;
            if(currentWidth + padding + widthPerItem > useableWidth) //time to reset
            {
                currentWidth = 0;
                currentHeight += padding;
                currentHeight += item.getHeight(); //should be 70
            }
        }
    }

    public void updateScrollBarSizes()
    {
        if(scrollVert != null)
        {
            scrollVert.setScrollBarSize(height / (float)getTotalItemHeight()); //if items height is higher than ours, scroll bar should appear
        }
    }

    @Override
    public List<? extends Element<?>> getEventListeners()
    {
        return elements;
    }

    @Override
    public boolean requireScissor()
    {
        return true;
    }

    @Override
    public boolean changeFocus(boolean direction) //we can't change focus on this
    {
        return false;
    }

    @Override
    public int getMinWidth()
    {
        return 58; // 50 + padding x2 + 1px border
    }

    @Override
    public int getMinHeight()
    {
        if(scrollVert != null)
        {
            return 14;
        }
        return super.getMinHeight();
    }

    @Override
    public int getBorderSize()
    {
        return 0;
    }
}
