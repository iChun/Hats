package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.gui.window.element.ElementHatRender;
import me.ichun.mods.hats.client.gui.window.element.ElementHatsScrollView;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WindowHatsList extends Window<WorkspaceHats>
{
    public WindowHatsList(WorkspaceHats parent) //TODO reduce renderCount by 1 to make sure we render the hat.
    {
        super(parent);

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

    public static class ViewHatsList extends View<WindowHatsList>
    {
        public static ResourceLocation TEX_SEARCH = new ResourceLocation("hats", "textures/icon/search.png");

        public ElementHatsScrollView list;

        public ViewHatsList(@Nonnull WindowHatsList parent)
        {
            super(parent, "hats.gui.window.hatsList");

            int padding = 4;

            ElementTexture searchIcon = new ElementTexture(this, TEX_SEARCH);
            searchIcon.setSize(16, 16);
            searchIcon.setConstraint(new Constraint(searchIcon).left(this, Constraint.Property.Type.LEFT, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding));
            elements.add(searchIcon);

            ElementTextField textField = new ElementTextField(this);
            textField.setId("search");
            textField.setResponder(this::updateSearch).setSize(70, 12);
            textField.setConstraint(new Constraint(textField).left(searchIcon, Constraint.Property.Type.RIGHT, 2).bottom(searchIcon, Constraint.Property.Type.BOTTOM, 1).top(searchIcon, Constraint.Property.Type.TOP, 1).width(this, Constraint.Property.Type.WIDTH, 40));
            elements.add(textField);

            //TODO sort menu - alphabetical / rarity / count
            //TODO in sort menu - show all hats?
            //TODO show unowned hats at the bottom?
            //TODO sync with server regarding hats list.

//
//            ArrayList<String> test = new ArrayList<>();
//            test.add("ABC");
//            test.add("asdas");
//            test.add("djdjdj");
//            ElementDropdownContextMenu<?> sortButton = new ElementDropdownContextMenu<>(this, "Sort by...", test, ((menu, item) -> {
//                if(item.selected)
//                {
//                    ElementDropdownContextMenu<?> contextMenu = (ElementDropdownContextMenu<?>)menu;
//                    contextMenu.text = item.getObject().toString().trim();
//
////                    changeProfile(contextMenu.text);
//                }
//            }));
//            sortButton.setSize(70, 16);
//            sortButton.setConstraint(new Constraint(sortButton).right(this, Constraint.Property.Type.RIGHT, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding).width(this, Constraint.Property.Type.WIDTH, 40));
//            elements.add(sortButton);

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

        public void updateSearch(String query) //TODO hide contributor hats filter
        {
            List<HatsSavedData.HatPart> hatPartSource = ((WorkspaceHats)getWorkspace()).getHatPartSource();
            if(!query.isEmpty())
            {
                hatPartSource = hatPartSource.stream().filter(hatPart -> hatPart.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
            }
            Collections.sort(hatPartSource);

            list.elements.clear();
            for(HatsSavedData.HatPart part : hatPartSource) //TODO how to render hats without accessories???
            {
                ElementHatRender<?> hat = new ElementHatRender<>(list, part.createCopy(), btn -> {
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

                        HatHandler.assignSpecificHat(parentFragment.parent.hatEntity, btn.hatDetails);
                    }
                }, btn -> {
                }
                );
                hat.setToggled(parentFragment.parent.hatDetails.name.equals(part.name));
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
        return 80;
    }
}
