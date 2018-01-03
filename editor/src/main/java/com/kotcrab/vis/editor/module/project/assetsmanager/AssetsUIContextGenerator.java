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

package com.kotcrab.vis.editor.module.project.assetsmanager;

import com.badlogic.gdx.files.FileHandle;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Provides additional UI context shown by AssetsUIModule. May use injected modules from project level scope.
 * @author Kotcrab
 */
public interface AssetsUIContextGenerator {
	/**
	 * Called when this provider shout init self, eg. create UI elements. When this method is called
	 * project modules were already injected.
	 */
	void init ();

	/** @return context table or null if this provider is not interested */
	VisTable provideContext (FileHandle fileHandle, String assetsFolderRelativePath);
}
