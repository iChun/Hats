package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;

import javax.annotation.Nonnull;

public class WindowSidebar extends Window<WorkspaceHats>
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
    {
        public ViewSidebar(@Nonnull WindowSidebar parent)
        {
            super(parent, "hats.gui.window.sidebar");

            ElementButton<?> btnRemoveHat = new ElementButton<>(this, "X", btn -> {});
            btnRemoveHat.setSize(20, 20);
            btnRemoveHat.constraints().left(this, Constraint.Property.Type.LEFT, 0).top(this, Constraint.Property.Type.TOP, 0);
            elements.add(btnRemoveHat);

            ElementButton<?> btnConfirm = new ElementButton<>(this, "O", btn -> {});
            btnConfirm.setSize(20, 20);
            btnConfirm.constraints().left(this, Constraint.Property.Type.LEFT, 0).bottom(this, Constraint.Property.Type.BOTTOM, 0);
            elements.add(btnConfirm);
        }
    }
}
