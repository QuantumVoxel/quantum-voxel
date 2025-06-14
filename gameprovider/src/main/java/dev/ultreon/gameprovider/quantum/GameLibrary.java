/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.gameprovider.quantum;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.game.LibClassifier.LibraryType;
import org.intellij.lang.annotations.Language;

enum GameLibrary implements LibraryType {
    QUANTUM_VXL_CLIENT("dev/ultreon/quantum/desktop/DesktopLauncher.class"),
    QUANTUM_VXL_SERVER("dev/ultreon/quantum/dedicated/Main.class"),
	LIBGDX("com/badlogic/gdx/Gdx.class"),
	GSON("com/google/gson/TypeAdapter.class"), // used by log4j plugins
	SLF4J_API("org/slf4j/Logger.class"),
	SLF4J_CORE("META-INF/services/org.slf4j.spi.SLF4JServiceProvider");

	static final GameLibrary[] GAME = {GameLibrary.QUANTUM_VXL_CLIENT, GameLibrary.QUANTUM_VXL_SERVER};
	static final GameLibrary[] LOGGING = {GameLibrary.GSON, GameLibrary.SLF4J_API, GameLibrary.SLF4J_CORE};

	private final EnvType env;
	private final String[] paths;

	GameLibrary(String path) {
		this(null, new String[] { path });
	}

	GameLibrary(String... paths) {
		this(null, paths);
	}

	GameLibrary(EnvType env, String... paths) {
		this.paths = paths;
		this.env = env;
	}

	@Override
	public boolean isApplicable(EnvType env) {
		return this.env == null || this.env == env;
	}

	@Override
	public String[] getPaths() {
		return paths;
	}
}
