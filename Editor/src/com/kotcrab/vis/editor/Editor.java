/*
 * Copyright 2014-2015 Pawel Pastuszak
 *
 * This file is part of VisEditor.
 *
 * VisEditor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VisEditor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VisEditor.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kotcrab.vis.editor;

import com.kotcrab.vis.editor.event.*;
import com.kotcrab.vis.editor.module.EditorModuleContainer;
import com.kotcrab.vis.editor.module.ProjectIOModule;
import com.kotcrab.vis.editor.module.project.*;
import com.kotcrab.vis.editor.module.project.SceneIOModule;
import com.kotcrab.vis.editor.module.project.SceneTabsModule;
import com.kotcrab.vis.editor.ui.EditorFrame;
import com.kotcrab.vis.editor.event.EventListener;
import com.kotcrab.vis.editor.event.ProjectStatusEvent;
import com.kotcrab.vis.editor.event.ProjectStatusEvent.Status;
import com.kotcrab.vis.editor.event.StatusBarEvent;
import com.kotcrab.vis.editor.module.MenuBarModule;
import com.kotcrab.vis.editor.module.StatusBarModule;
import com.kotcrab.vis.editor.module.TabsModule;
import com.kotcrab.vis.editor.module.ToolbarModule;
import com.kotcrab.vis.editor.module.project.AssetsManagerUIModule;
import com.kotcrab.vis.editor.module.project.FileAccessModule;
import com.kotcrab.vis.editor.module.project.Project;
import com.kotcrab.vis.editor.module.project.ProjectInfoTabModule;
import com.kotcrab.vis.editor.module.project.ProjectModuleContainer;
import com.kotcrab.vis.editor.module.project.TextureCacheModule;
import com.kotcrab.vis.editor.module.scene.EditorScene;
import com.kotcrab.vis.editor.ui.tab.Tab;
import com.kotcrab.vis.editor.ui.tab.TabViewMode;
import com.kotcrab.vis.editor.util.EditorException;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisTable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.DialogUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSplitPane;

public class Editor extends ApplicationAdapter implements EventListener {
	public static Editor instance;

	private EditorFrame frame;

	private Stage stage;
	private Table root;

	// TODO move to module
	private Table mainContentTable;
	private Table tabContentTable;
	private VisTable projectContentTable;
	private VisSplitPane splitPane;

	private EditorModuleContainer editorMC;
	private ProjectModuleContainer projectMC;

	private boolean projectLoaded = false;

	private Tab tab;

	public Editor (EditorFrame frame) {
		this.frame = frame;
	}

	@Override
	public void create () {
		instance = this;
		Assets.load();
		VisUI.load();
		VisUI.setDefaultTitleAlign(Align.center);

		App.eventBus.register(this);

		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		mainContentTable = new Table();
		tabContentTable = new Table();
		projectContentTable = new VisTable(true);
		splitPane = new VisSplitPane(null, null, true);
		splitPane.setSplitAmount(0.78f);

		projectContentTable.add(new VisLabel("Project Content Manager has not been loaded yet"));

		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);

		editorMC = new EditorModuleContainer();
		projectMC = new ProjectModuleContainer(editorMC);

		editorMC.add(new ProjectIOModule());

		// GUI modules
		editorMC.add(new MenuBarModule(projectMC));
		editorMC.add(new ToolbarModule());
		editorMC.add(new TabsModule());

		root.add(mainContentTable).expand().fill().row();
		root.row();

		editorMC.add(new StatusBarModule());

		editorMC.init();

		// debug section
		try {
			editorMC.get(ProjectIOModule.class).load((Gdx.files.absolute("F:\\Poligon\\TestProject")));
		} catch (EditorException e) {
			e.printStackTrace();
		}

		EditorScene testScene = projectMC.get(SceneIOModule.class).load(
			Gdx.files.absolute("F:\\Poligon\\TestProject\\vis\\assets\\scene\\test.scene"));
		projectMC.get(SceneTabsModule.class).open(testScene);
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
		editorMC.resize();
		projectMC.resize();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		if (tab != null) tab.render(stage.getBatch());
		stage.draw();
	}

	@Override
	public void dispose () {
		editorMC.dispose();
		if (projectLoaded) projectMC.dispose();

		stage.dispose();
		Assets.dispose();
		VisUI.dispose();

		frame.dispose();
	}

	public void requestExit () {
		// here will be fancy 'do you really want to exit' dialog
		exit();
	}

	private void exit () {
		Gdx.app.exit();
	}

	public Stage getStage () {
		return stage;
	}

	public Table getRoot () {
		return root;
	}

	@Override
	public boolean onEvent (Event e) {
		return false;
	}

	public void requestProjectUnload () {
		projectLoaded = false;
		projectMC.dispose();

		App.eventBus.post(new StatusBarEvent("Project unloaded"));
		App.eventBus.post(new ProjectStatusEvent(Status.Unloaded));
	}

	public boolean isProjectLoaded () {
		return projectLoaded;
	}

	public void projectLoaded (Project project) {
		// TODO unload previous project dialog
		if (projectLoaded) {
			DialogUtils.showErrorDialog(getStage(), "Other project is already loaded!");
			return;
		}

		projectLoaded = true;
		projectMC.setProject(project);

		projectMC.add(new FileAccessModule());
		projectMC.add(new AssetsWatcherModule());
		projectMC.add(new TextureCacheModule());
		projectMC.add(new SceneIOModule());

		projectMC.add(new SceneTabsModule());
		projectMC.add(new ProjectInfoTabModule());
		projectMC.add(new AssetsManagerUIModule());

		projectMC.init();

		App.eventBus.post(new StatusBarEvent("Project loaded"));
		App.eventBus.post(new ProjectStatusEvent(Status.Loaded));
	}

	public void tabChanged (Tab tab) {
		this.tab = tab;

		tabContentTable.clear();
		mainContentTable.clear();
		splitPane.setWidgets(null, null);

		if (tab != null) {
			tabContentTable.add(tab.getContentTable()).expand().fill();
			if (tab.getViewMode() == TabViewMode.TAB_ONLY)
				mainContentTable.add(tabContentTable).expand().fill();
			else {
				splitPane.setWidgets(tabContentTable, projectContentTable);
				mainContentTable.add(splitPane).expand().fill();
			}
		}
	}

	public VisTable getProjectContentTable () {
		return projectContentTable;
	}
}