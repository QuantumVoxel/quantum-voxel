/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package dev.ultreon.mixinprovider;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GL32;

/**
 * Define a shader stage preset including a shader type, name and file extension.
 * <p>
 * This class contains several shader stage presets for convenience and mainly used by ShaderProgramLoader.
 * You're not limited to the predefined ones, you can create your own to change naming convention
 * or in order to support other shader stage type (eg. Compute Shader available with OpenGL 4.3+).
 * <p>
 * Note that geometry and tesselation shaders require GLES 3.2+ and are not supported by WebGL. 
 * <p>
 * Shader file extension is following official convention
 * (https://www.khronos.org/opengles/sdk/tools/Reference-Compiler/) :
 * <ul>
 *   <li>.vert - a vertex shader</li>
 *   <li>.frag - a fragment shader</li>
 *   <li>.geom - a geometry shader</li>
 *   <li>.tesc - a tessellation control shader</li>
 *   <li>.tese - a tessellation evaluation shader</li>
 * </ul>
 * 
 * @author mgsx
 *
 */
public class ShaderStage {
	
	public static final ShaderStage vertex = new ShaderStage(GL20.GL_VERTEX_SHADER, "Vertex", ".vert");
	public static final ShaderStage fragment = new ShaderStage(GL20.GL_FRAGMENT_SHADER, "Fragment", ".frag");
	public static final ShaderStage geometry = new ShaderStage(GL32.GL_GEOMETRY_SHADER, "Geometry", ".geom");
	public static final ShaderStage tesselationControl = new ShaderStage(GL32.GL_TESS_CONTROL_SHADER, "Tesslation Control", ".tesc");
	public static final ShaderStage tesselationEvaluation = new ShaderStage(GL32.GL_TESS_EVALUATION_SHADER, "Tesslation Evaluation", ".tese");
	
	public static final ShaderStage [] stages = {vertex, fragment, geometry, tesselationControl, tesselationEvaluation};
	
	public final int type;
	public final String name;
	public final String suffix;
	
	/** code that is always added to shader code for this stage, typically used to inject a #version line. Note that this is added
	 * as-is, you should include a newline (`\n`) if needed. */
	public String prependCode;
	
	public ShaderStage (int type, String name, String suffix) {
		super();
		this.type = type;
		this.name = name;
		this.suffix = suffix;
	}
}