/*
 * Copyright 2014-2017 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kotcrab.vis.editor.module.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.VisLabel;

/** @author Kotcrab */
public class EmptyMenuFillerModule extends EditorModule {
	private MenuBarModule menuBar;

	@Override
	public void postInit () {
		Gdx.app.postRunnable(() -> {
			Menu toolMenus = menuBar.getToolsMenu();
			if (toolMenus.getChildren().size == 0) {
				toolMenus.add(new VisLabel("No tools available", Color.GRAY)).pad(2, 5, 2, 3);
				toolMenus.pack();
			}
		});

	}
}
