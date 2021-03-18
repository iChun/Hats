package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.client.gui.IHatSetter;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.client.gui.window.element.ElementHatRender;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButtonTextured;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

public class WindowSidebar extends Window<WorkspaceHats>
    implements IHatSetter
{
    public WindowSidebar(WorkspaceHats parent)
    {
        super(parent);

        setBorderSize(() -> 0);

        disableBringToFront();
        disableDocking();
        disableDockStacking();
        disableUndocking();
        disableDrag();
        disableDragResize();
        disableTitle();

        setId("windowSidebar");

        setView(new ViewSidebar(this));
    }

    @Override
    public void renderBackground(MatrixStack stack){} //no BG

    public static class ViewSidebar extends View<WindowSidebar>
            implements IHatSetter
    {
        public static ResourceLocation TEX_CANCEL = new ResourceLocation("hats", "textures/icon/cancel.png");
        public static ResourceLocation TEX_RANDOMISE = new ResourceLocation("hats", "textures/icon/randomise.png");
        public static ResourceLocation TEX_CATEGORIES = new ResourceLocation("hats", "textures/icon/categories.png");
        public static ResourceLocation TEX_RELOAD = new ResourceLocation("hats", "textures/icon/reload.png");
        public static ResourceLocation TEX_CONFIRM = new ResourceLocation("hats", "textures/icon/confirm.png");

        public ElementButtonTextured<?> cancelButton;

        public ViewSidebar(@Nonnull WindowSidebar parent) //TODO head analyser for Tabula
        {
            super(parent, "hats.gui.window.sidebar");

            ElementButtonTextured<?> btnStack;
            ElementButtonTextured<?> btnStackLast;

            int padding = 2;

            //CANCEL button
            cancelButton = btnStack = new ElementButtonTextured<>(this, TEX_CANCEL, btn -> {
                parent.parent.setNewHat(null, false);
            });
            if(parent.parent.hatDetails.name.isEmpty())
            {
                cancelButton.disabled = true;
            }
            btnStack.setTooltip(I18n.format("hats.gui.button.removeHat"));
            btnStack.setSize(20, 20);
            btnStack.constraints().left(this, Constraint.Property.Type.LEFT, 0).top(this, Constraint.Property.Type.TOP, 0);
            elements.add(btnStack);
            btnStackLast = btnStack;

            //RANDOMISE
            btnStack = new ElementButtonTextured<>(this, TEX_RANDOMISE, btn -> {
                List<Element<?>> elements = parent.parent.windowHatsList.getCurrentView().list.elements;
                Element<?> element1 = elements.get(parentFragment.parent.hatEntity.getRNG().nextInt(elements.size()));
                if(element1 instanceof ElementHatRender)
                {
                    ((ElementHatRender<?>)element1).onClickRelease();
                    ((ElementHatRender)element1).callback.accept(element1); //TODO shift + ctrl randomisation
                }
            });
            btnStack.setTooltip(I18n.format("hats.gui.button.randomHat"));
            btnStack.setSize(20, 20);
            btnStack.constraints().left(this, Constraint.Property.Type.LEFT, 0).top(btnStackLast, Constraint.Property.Type.BOTTOM, padding);
            elements.add(btnStack);
            btnStackLast = btnStack;

            if(parent.parent.hatLauncher == null)
            {
                //SORTING OPTIONS
                btnStack = new ElementButtonTextured<>(this, TEX_CATEGORIES, btn -> {
                });
                btnStack.setTooltip(I18n.format("hats.gui.button.sortingOptions"));
                btnStack.setSize(20, 20);
                btnStack.constraints().left(this, Constraint.Property.Type.LEFT, 0).top(btnStackLast, Constraint.Property.Type.BOTTOM, padding);
                elements.add(btnStack);
                btnStackLast = btnStack;

                //RELOAD
                btnStack = new ElementButtonTextured<>(this, TEX_RELOAD, btn -> {
                });
                btnStack.setTooltip(I18n.format("hats.gui.button.hatResourceManagement"));
                btnStack.setSize(20, 20);
                btnStack.constraints().left(this, Constraint.Property.Type.LEFT, 0).top(btnStackLast, Constraint.Property.Type.BOTTOM, padding);
                elements.add(btnStack);
                btnStackLast = btnStack;
            }

            //CONFIRM button
            ElementButtonTextured<?> btnConfirm = new ElementButtonTextured<>(this, TEX_CONFIRM, btn -> {
                parent.parent.confirmed = true;
                parent.parent.closeScreen();
            });
            btnConfirm.setSize(20, 20);
            btnConfirm.constraints().left(this, Constraint.Property.Type.LEFT, 0).bottom(this, Constraint.Property.Type.BOTTOM, 0);
            elements.add(btnConfirm);
        }

        @Override
        public void onNewHatSet(HatsSavedData.HatPart newHat)
        {
            cancelButton.disabled = newHat == null;
        }

        @Override
        public void renderBackground(MatrixStack stack){}
    }
}
