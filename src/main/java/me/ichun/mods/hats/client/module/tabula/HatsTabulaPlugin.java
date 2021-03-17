package me.ichun.mods.hats.client.module.tabula;

import com.google.common.collect.Ordering;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.common.module.tabula.TabulaPlugin;
import me.ichun.mods.ichunutil.common.module.tabula.formats.ImportList;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;

@OnlyIn(Dist.CLIENT)
public class HatsTabulaPlugin extends TabulaPlugin //TODO ghost hat project
{
    @Override
    public ElementButtonTextured<?> onPopulateToolbar(@Nonnull TabulaWorkspace workspace, @Nonnull View<?> toolbarView, @Nonnull ElementButtonTextured<?> lastToolbarButton, @Nullable Project currentProject)
    {
        ElementButtonTextured<?> btn = new ElementButtonTextured<>(toolbarView, new ResourceLocation("hats", "textures/icon/tabula_open.png"), button -> {
            Window<?> window = new WindowOpenProject(toolbarView.getWorkspace(), workspace);
            toolbarView.getWorkspace().openWindowInCenter(window, 0.4D, 0.6D);
            window.init();
        });
        btn.setSize(20,20).setTooltip(I18n.format("hats.plugin.tabula.button"));
        btn.setConstraint(new Constraint(btn).left(lastToolbarButton, Constraint.Property.Type.RIGHT, 0));
        toolbarView.elements.add(btn);
        return btn;
    }

    public static class WindowOpenProject extends Window<Workspace>
    {
        public TabulaWorkspace tabulaWorkspace;

        public WindowOpenProject(Workspace parent, TabulaWorkspace tabulaWorkspace)
        {
            super(parent);
            this.tabulaWorkspace = tabulaWorkspace;

            setView(new ViewOpenProject(this));
            disableDockingEntirely();
        }

        public static class ViewOpenProject extends View<WindowOpenProject>
        {
            public TreeSet<File> files = new TreeSet<>(Ordering.natural());
            public ElementList<?> list;

            public ViewOpenProject(@Nonnull WindowOpenProject parent)
            {
                super(parent, "window.open.title");

                ElementScrollBar<?> sv = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
                sv.setConstraint(new Constraint(sv).top(this, Constraint.Property.Type.TOP, 0)
                        .bottom(this, Constraint.Property.Type.BOTTOM, 40) // 10 + 20 + 10, bottom + button height + padding
                        .right(this, Constraint.Property.Type.RIGHT, 0)
                );
                elements.add(sv);

                list = new ElementList<>(this).setScrollVertical(sv);
                list.setConstraint(new Constraint(list).bottom(this, Constraint.Property.Type.BOTTOM, 40)
                        .left(this, Constraint.Property.Type.LEFT, 0).right(sv, Constraint.Property.Type.LEFT, 0)
                        .top(this, Constraint.Property.Type.TOP, 0));
                elements.add(list);

                ArrayList<File> allFiles = new ArrayList<>();
                scourForHats(allFiles, HatResourceHandler.getHatsDir().toFile());

                files.addAll(allFiles);

                ElementButtonTextured<?> openDir = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/info.png"), btn -> {
                    Util.getOSType().openFile(HatResourceHandler.getHatsDir().toFile());
                });
                openDir.setTooltip(I18n.format("topdock.openWorkingDir")).setSize(20, 20);
                openDir.setConstraint(new Constraint(openDir).bottom(this, Constraint.Property.Type.BOTTOM, 10).left(this, Constraint.Property.Type.LEFT, 10));
                elements.add(openDir);

                ElementNumberInput input = new ElementNumberInput(this, false);
                input.setMin(0).setMax(100).setDefaultText("20").setTooltip(I18n.format("window.controls.opacity")).setSize(60, 20).setId("inputOpacity");
                input.setConstraint(new Constraint(input).bottom(this, Constraint.Property.Type.BOTTOM, 10).left(openDir, Constraint.Property.Type.RIGHT, 10));
                elements.add(input);


                ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), btn ->
                {
                    getWorkspace().removeWindow(parent);
                });
                button.setSize(60, 20);
                button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
                elements.add(button);

                ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), btn ->
                {
                    for(ElementList.Item<?> item : list.items)
                    {
                        if(item.selected)
                        {
                            loadFile((File)item.getObject(), ((ElementNumberInput)getById("inputOpacity")).getInt() / 100F);
                            return;
                        }
                    }
                });
                button1.setSize(60, 20);
                button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
                elements.add(button1);

                ElementTextField textField = new ElementTextField(this);
                textField.setId("search");
                textField.setResponder(s -> {
                    this.list.items.clear();
                    this.populateList(s);
                }).setHeight(14);
                textField.setConstraint(new Constraint(textField).left(input, Constraint.Property.Type.RIGHT, 10).right(button1, Constraint.Property.Type.LEFT, 10).bottom(button1, Constraint.Property.Type.BOTTOM, 3));
                elements.add(textField);

                populateList("");
            }

            public void populateList(String query)
            {
                for(File file : files)
                {
                    if(query.isEmpty() || file.getName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
                    {
                        list.addItem(file).setDefaultAppearance().setDoubleClickHandler(item -> {
                            if(item.selected)
                            {
                                loadFile(item.getObject(), ((ElementNumberInput)getById("inputOpacity")).getInt() / 100F);
                            }
                        });
                    }
                }

                list.init();
                list.init();
            }

            public void loadFile(File file, float ghostOpacity)
            {
                parentFragment.parent.removeWindow(parentFragment);

                Project project = ImportList.createProjectFromFile(file);
                if(project == null)
                {
                    WindowPopup.popup(parentFragment.parent, 0.4D, 140, null, I18n.format("window.open.failed"));
                }
                else
                {
                    if(project.tampered)
                    {
                        WindowPopup.popup(parentFragment.parent, 0.5D, 160, null, I18n.format("window.open.tampered"));
                    }

                    if(project.isOldTabula)
                    {
                        WindowPopup.popup(parentFragment.parent, 0.5D, 300, null, I18n.format("window.open.oldTabula1"), I18n.format("window.open.oldTabula2"), I18n.format("window.open.oldTabula3"), I18n.format("window.open.oldTabula4"));
                    }

                    Project ghost = null;
                    if(ghostOpacity > 0F)
                    {
                        InputStream in = Hats.class.getResourceAsStream("/HatTemplate.tbl");
                        if(in != null)
                        {
                            ghost = ImportList.IMPORTER_TABULA.createProject(in);
                            if(ghost != null)
                            {
                                ghost.projVersion = ImportList.IMPORTER_TABULA.getProjectVersion();
                                ghost.load();
                            }
                        }
                    }
                    parentFragment.tabulaWorkspace.openProject(project, ghost, ghost == null ? 0 : ghostOpacity);
                }
            }

            private static void scourForHats(ArrayList<File> allFiles, File dir)
            {
                File[] files = dir.listFiles();
                for(File file : files)
                {
                    if(file.isDirectory())
                    {
                        scourForHats(allFiles, file);
                    }
                    else if(ImportList.isFileSupported(file)) //tabula format
                    {
                        allFiles.add(file);
                    }
                }
            }
        }
    }
}
