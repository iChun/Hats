package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementList;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Consumer;

public class WindowHatSorter extends Window<Workspace>
{
    //title will be localised, text won't.
    public WindowHatSorter(Workspace parent)
    {
        super(parent);

        disableDockingEntirely();
        disableDrag();
        disableTitle();

        setView(new ViewHatSorter(this));
    }

    public static class ViewHatSorter extends View<WindowHatSorter>
    {
        public ElementList<?> filters;
        public ElementList<?> sorters;

        public ViewHatSorter(@Nonnull WindowHatSorter parent)
        {
            super(parent, "hats.gui.window.sorter.title");

            int padding = 8;

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), btn ->
            {
                parent.parent.removeWindow(parent);
            });
            button.setSize(60, 20);
            button.constraints().bottom(this, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), btn ->
            {
                parent.parent.removeWindow(parent);
            });
            button1.setSize(60, 20);
            button1.constraints().right(button, Constraint.Property.Type.LEFT, padding);
            elements.add(button1);

            ElementTextWrapper textFilters = new ElementTextWrapper(this);
            textFilters.setText(I18n.format("hats.gui.window.sorter.filters"));
            textFilters.constraints().left(this, Constraint.Property.Type.LEFT, padding).top(this, Constraint.Property.Type.TOP, 4).width(this, Constraint.Property.Type.WIDTH, 45);
            elements.add(textFilters);

            filters = new ElementList<>(this);
            filters.constraints().top(textFilters, Constraint.Property.Type.BOTTOM, 2).left(this, Constraint.Property.Type.LEFT, padding).bottom(button, Constraint.Property.Type.TOP, padding).width(this, Constraint.Property.Type.WIDTH, 45); //45%
            elements.add(filters);

            sorters = new ElementList<>(this);
            sorters.constraints().left(filters, Constraint.Property.Type.RIGHT, padding).top(filters, Constraint.Property.Type.TOP, 0).bottom(filters, Constraint.Property.Type.BOTTOM, 0). right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(sorters);

            ElementTextWrapper textSorters = new ElementTextWrapper(this);
            textSorters.setText(I18n.format("hats.gui.window.sorter.sorters"));
            textSorters.constraints().left(sorters, Constraint.Property.Type.LEFT, 0).bottom(sorters, Constraint.Property.Type.TOP, 3).right(sorters, Constraint.Property.Type.RIGHT, 0);
            elements.add(textSorters);

            ElementButton<?> buttonTip = new ElementButton<>(this, "?", btn -> {});
            buttonTip.setSize(20, 20);
            buttonTip.setTooltip(I18n.format("hats.gui.window.sorter.dragTip"));
            buttonTip.constraints().left(this, Constraint.Property.Type.LEFT, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding);
            elements.add(buttonTip);
        }

        @Override
        public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
        {
            stack.push();
            stack.translate(0F, 0F, 375F); //silly ElementHatRender

            super.render(stack, mouseX, mouseY, partialTick);

            stack.pop();
        }
    }
}
