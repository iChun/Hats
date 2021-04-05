package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.gui.window.element.ElementHatRender;
import me.ichun.mods.hats.client.gui.window.element.ElementHatsScrollView;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.sort.SortHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementScrollBar;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextField;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTexture;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WindowHatsList extends Window<WorkspaceHats>
{
    public WindowHatsList(WorkspaceHats parent)
    {
        super(parent); //TODO allow option for default MC texture pack rather than the player texture pack.

        setBorderSize(() -> (renderMinecraftStyle() ? 6 : 3));

        disableBringToFront();
        disableDocking();
        disableDockStacking();
        disableUndocking();
        disableDrag();
        disableTitle();

        setId("windowHatsList");

        setView(new ViewHatsList(this));
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        if(parent.age <= Hats.configClient.guiAnimationTime)
        {
            float prog = (float)Math.sin(Math.toRadians(MathHelper.clamp(((parent.age + partialTick) / Hats.configClient.guiAnimationTime), 0F, 1F) * 90F));
            float reverseProg = 1F - prog;
            posX = (int)(Math.floor((parent.getWidth() / 2F)) + (Math.ceil((parent.getWidth() / 2F)) + 2) * reverseProg);
            if(parent.age == Hats.configClient.guiAnimationTime)
            {
                resize(parent.getMinecraft(), parent.width, parent.height);
            }
            parent.windowSidebar.constraint.apply();
        }
        super.render(stack, mouseX, mouseY, partialTick);
    }

    @Override
    public void tick()
    {
        super.tick();

        if(parent.age == Hats.configClient.guiAnimationTime + 1) //Should fix things when FPS < tickRate
        {
            posX = (int)(Math.floor((parent.getWidth() / 2F)) + (Math.ceil((parent.getWidth() / 2F)) + 2) * 0F);
            resize(parent.getMinecraft(), parent.width, parent.height);
            parent.windowSidebar.constraint.apply();
        }
    }

    @Override
    public void setScissor()
    {
        if(parent.age < Hats.configClient.guiAnimationTime)
        {
            RenderHelper.startGlScissor(getLeft(), getTop(), width * 2, height);
        }
        else
        {
            super.setScissor();
        }
    }

    @Override
    public ViewHatsList getCurrentView()
    {
        return (ViewHatsList)currentView;
    }

    public static class ViewHatsList extends View<WindowHatsList>
    {
        public static ResourceLocation TEX_SEARCH = new ResourceLocation("hats", "textures/icon/search.png");

        public ElementHatsScrollView list;
        public ElementTextField textField;

        public ViewHatsList(@Nonnull WindowHatsList parent)
        {
            super(parent, "hats.gui.window.hatsList");

            int padding = 4;

            ElementTexture searchIcon = new ElementTexture(this, TEX_SEARCH);
            searchIcon.setSize(16, 16);
            searchIcon.constraints().left(this, Constraint.Property.Type.LEFT, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding);
            elements.add(searchIcon);

            textField = new ElementTextField(this);
            textField.setId("search");
            textField.setResponder(this::updateSearch).setSize(70, 12);
            textField.constraints().left(searchIcon, Constraint.Property.Type.RIGHT, 2).bottom(searchIcon, Constraint.Property.Type.BOTTOM, 1).top(searchIcon, Constraint.Property.Type.TOP, 1).width(this, Constraint.Property.Type.WIDTH, 40);
            elements.add(textField);

            //TODO sync with server regarding hats list.

            ElementScrollBar<?> sv = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            sv.constraints().top(this, Constraint.Property.Type.TOP, padding)
                    .bottom(searchIcon, Constraint.Property.Type.TOP, padding) // 10 + 20 + 10, bottom + button height + padding
                    .right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(sv);

            list = new ElementHatsScrollView(this).setScrollVertical(sv);
            list.constraints().top(this, Constraint.Property.Type.TOP, padding + 1)
                    .bottom(searchIcon, Constraint.Property.Type.TOP, padding + 1)
                    .left(this, Constraint.Property.Type.LEFT, padding + 1)
                    .right(sv, Constraint.Property.Type.LEFT, padding + 1);
            elements.add(list);

            updateSearch("");
        }

        public void updateSearch(String query)
        {
            List<HatsSavedData.HatPart> hatPartSource = ((WorkspaceHats)getWorkspace()).getHatPartSource();
            SortHandler.sort(Hats.configClient.filterSorters, hatPartSource, query.isEmpty());
            if(!query.isEmpty()) //we're searching for something.
            {
                hatPartSource = hatPartSource.stream().filter(hatPart -> hatPart.has(query.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
            }

            list.elements.clear();

            HatsSavedData.HatPart entityPart = HatHandler.getHatPart(parentFragment.parent.hatEntity);
            for(HatsSavedData.HatPart part : hatPartSource)
            {
                HatsSavedData.HatPart hatPart = part.createCopy();
                if(parentFragment.parent.hatLauncher != null && parentFragment.parent.usePlayerInventory())
                {
                    //Remove our worn hat from the count
                    hatPart.minusByOne(entityPart);
                }
                ElementHatRender<?> hat = new ElementHatRender<>(list, hatPart, hatPart, btn -> {
                    ElementHatsScrollView scrollView = (ElementHatsScrollView)btn.parentFragment;
                    if(btn.toggleState) //we're selected
                    {
                        for(Element<?> element : scrollView.elements)
                        {
                            if(element != btn)
                            {
                                ((ElementHatRender<?>)element).toggleState = false;
                            }
                        }

                        boolean notify = false;
                        if(btn.hatLevel.isNew)
                        {
                            btn.hatLevel.isNew = false;
                            notify = true;
                        }
                        parentFragment.parent.setNewHat(btn.hatOrigin.setModifier(btn.hatLevel), notify);
                    }
                }
                );
                HatsSavedData.HatPart parentPart = parentFragment.parent.hatLauncher != null ? HatHandler.getHatPart(parentFragment.parent.hatLauncher) : HatHandler.getHatPart(parentFragment.parent.hatEntity);
                hat.setToggled(parentPart.name.equals(part.name));
                hat.setSize(50, 70);
                list.addElement(hat);
            }

            list.init();
            list.init();
        }
    }

    @Override
    public int getMinWidth()
    {
        return 140;
    }
}
