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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;

import java.nio.*;
import java.lang.StringBuilder;

/** <p>
 * A shader program encapsulates several shader parts (typically a vertex and fragment shader) linked to form a shader program.
 * </p>
 * 
 * <p>
 * After construction a ShaderProgram can be used to draw {@link Mesh}. To make the GPU use a specific ShaderProgram the programs
 * {@link NewShaderProgram#bind()} method must be used which effectively binds the program.
 * </p>
 * 
 * <p>
 * When a ShaderProgram is bound one can set uniforms, vertex attributes and attributes as needed via the respective methods.
 * </p>
 * 
 * <p>
 * A ShaderProgram must be disposed via a call to {@link NewShaderProgram#dispose()} when it is no longer needed
 * </p>
 * 
 * <p>
 * ShaderPrograms are managed. In case the OpenGL context is lost all shaders get invalidated and have to be reloaded. This
 * happens on Android when a user switches to another application or receives an incoming call. Managed ShaderPrograms are
 * automatically reloaded when the OpenGL context is recreated so you don't have to do this manually.
 * </p>
 * 
 * @author mzechner */
public class NewShaderProgram extends ShaderProgram implements Disposable {
	/** default name for position attributes **/
	public static final String POSITION_ATTRIBUTE = "a_position";
	/** default name for normal attributes **/
	public static final String NORMAL_ATTRIBUTE = "a_normal";
	/** default name for color attributes **/
	public static final String COLOR_ATTRIBUTE = "a_color";
	/** default name for texcoords attributes, append texture unit number **/
	public static final String TEXCOORD_ATTRIBUTE = "a_texCoord";
	/** default name for tangent attribute **/
	public static final String TANGENT_ATTRIBUTE = "a_tangent";
	/** default name for binormal attribute **/
	public static final String BINORMAL_ATTRIBUTE = "a_binormal";
	/** default name for boneweight attribute **/
	public static final String BONEWEIGHT_ATTRIBUTE = "a_boneWeight";

	/** flag indicating whether attributes &amp; uniforms must be present at all times **/
	public static boolean pedantic = true;

	/** the list of currently available shaders **/
	private final static ObjectMap<Application, Array<NewShaderProgram>> shaders = new ObjectMap<Application, Array<NewShaderProgram>>();

	/** the log **/
	private String log = "";

	/** whether this program compiled successfully **/
	private boolean isCompiled;

	/** uniform lookup **/
	private final ObjectIntMap<String> uniforms = new ObjectIntMap<String>();

	/** uniform types **/
	private final ObjectIntMap<String> uniformTypes = new ObjectIntMap<String>();

	/** uniform sizes **/
	private final ObjectIntMap<String> uniformSizes = new ObjectIntMap<String>();

	/** uniform names **/
	private String[] uniformNames;

	/** attribute lookup **/
	private final ObjectIntMap<String> attributes = new ObjectIntMap<String>();

	/** attribute types **/
	private final ObjectIntMap<String> attributeTypes = new ObjectIntMap<String>();

	/** attribute sizes **/
	private final ObjectIntMap<String> attributeSizes = new ObjectIntMap<String>();

	/** attribute names **/
	private String[] attributeNames;

	/** program handle **/
	private int program;

	/** matrix float buffer **/
	private final FloatBuffer matrix;

	/** whether this shader was invalidated **/
	private boolean invalidated;

	/** reference count **/
	private int refCount = 0;

	/** all shaders making this shader program */
	private Array<ShaderPart> parts;
	
	/** Constructs a new ShaderProgram and immediately compiles it.
	 * <p>
	 * Caller is responsible to provide a consistent list shaders, typically a vertex shader and
	 * a fragment shader. Several shaders can be used for a particular stage but each stages needs one and only
	 * one main function, this can be helpful if you want to include some library parts.
	 * GLES 2.0 only permit a vertex shader and a fragment shader pair.
	 * </p>
	 * @param parts shaders
	 */
	public NewShaderProgram(Array<ShaderPart> parts) {
        super("\7\7\7", "\7\7\7");
        this.matrix = BufferUtils.newFloatBuffer(16);
		this.parts = parts;
		
		// prepend code
		for(ShaderPart part : parts) {
			String prependCode = part.stage.prependCode;
			if(prependCode != null){
				part.finalCode = prependCode + part.source;
			}else{
				part.finalCode = part.source;
			}
		}

		compileShaders();
		if (isCompiled()) {
			fetchAttributes();
			fetchUniforms();
			addManagedShader(Gdx.app, this);
		}
	}

	public NewShaderProgram(ShaderPart ...parts) {
		this(new Array<ShaderPart>(parts));
	}

	/** Constructs a new ShaderProgram and immediately compiles it.
	 * Convenient constructor for typical vertex and fragment shader pair.
	 * @param vertexShader the vertex shader
	 * @param fragmentShader the fragment shader */

	public NewShaderProgram(String vertexShader, String fragmentShader) {
		this(new ShaderPart(ShaderStage.vertex, vertexShader), new ShaderPart(ShaderStage.fragment, fragmentShader));
	}

	public NewShaderProgram(FileHandle vertexShader, FileHandle fragmentShader) {
		this(vertexShader.readString(), fragmentShader.readString());
	}

	/** Loads and compiles the shaders, creates a new program and links the shaders.
	 */
	private void compileShaders () {
		isCompiled = true;
		for(ShaderPart part : parts){
			part.handle = loadShader(part.stage, part.finalCode);
			if(part.handle == -1){
				isCompiled = false;
			}
		}

		if (!isCompiled) {
			return;
		}

		program = linkProgram(createProgram());
		if (program == -1) {
			isCompiled = false;
			return;
		}

		isCompiled = true;
	}

	private int loadShader (ShaderStage stage, String source) {
		GL20 gl = Gdx.gl20;
		IntBuffer intbuf = BufferUtils.newIntBuffer(1);

		int shader = gl.glCreateShader(stage.type);
		if (shader == 0) return -1;

		gl.glShaderSource(shader, source);
		gl.glCompileShader(shader);
		gl.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, intbuf);

		int compiled = intbuf.get(0);
		if (compiled == 0) {
// gl.glGetShaderiv(shader, GL20.GL_INFO_LOG_LENGTH, intbuf);
// int infoLogLength = intbuf.get(0);
// if (infoLogLength > 1) {
			String infoLog = gl.glGetShaderInfoLog(shader);
			log += stage.name + " shader\n";
			log += infoLog;
// }
			return -1;
		}

		return shader;
	}

	protected int createProgram () {
		GL20 gl = Gdx.gl20;
		int program = gl.glCreateProgram();
		return program != 0 ? program : -1;
	}

	private int linkProgram (int program) {
		GL20 gl = Gdx.gl20;
		if (program == -1) return -1;

		for(ShaderPart part : parts){
			gl.glAttachShader(program, part.handle);
		}

		gl.glLinkProgram(program);

		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intbuf = tmp.asIntBuffer();

		gl.glGetProgramiv(program, GL20.GL_LINK_STATUS, intbuf);
		int linked = intbuf.get(0);
		if (linked == 0) {
// Gdx.gl20.glGetProgramiv(program, GL20.GL_INFO_LOG_LENGTH, intbuf);
// int infoLogLength = intbuf.get(0);
// if (infoLogLength > 1) {
			log = Gdx.gl20.glGetProgramInfoLog(program);
// }
			return -1;
		}

		return program;
	}

	final static IntBuffer intbuf = BufferUtils.newIntBuffer(1);

	/** @return the log info for the shader compilation and program linking stage. The shader needs to be bound for this method to
	 *         have an effect. */
	public String getLog () {
		if (isCompiled) {
// Gdx.gl20.glGetProgramiv(program, GL20.GL_INFO_LOG_LENGTH, intbuf);
// int infoLogLength = intbuf.get(0);
// if (infoLogLength > 1) {
			log = Gdx.gl20.glGetProgramInfoLog(program);
// }
			return log;
		} else {
			return log;
		}
	}

	/** @return whether this ShaderProgram compiled successfully. */
	public boolean isCompiled () {
		return isCompiled;
	}

	private int fetchAttributeLocation (String name) {
		GL20 gl = Gdx.gl20;
		// -2 == not yet cached
		// -1 == cached but not found
		int location;
		if ((location = attributes.get(name, -2)) == -2) {
			location = gl.glGetAttribLocation(program, name);
			attributes.put(name, location);
		}
		return location;
	}

	private int fetchUniformLocation (String name) {
		return fetchUniformLocation(name, pedantic);
	}

	public int fetchUniformLocation (String name, boolean pedantic) {
		GL20 gl = Gdx.gl20;
		// -2 == not yet cached
		// -1 == cached but not found
		int location;
		if ((location = uniforms.get(name, -2)) == -2) {
			location = gl.glGetUniformLocation(program, name);
			if (location == -1 && pedantic) {
				if (isCompiled) throw new IllegalArgumentException("no uniform with name '" + name + "' in shader");
				throw new IllegalStateException("An attempted fetch uniform from uncompiled shader \n" + getLog());
			}
			uniforms.put(name, location);
		}
		return location;
	}

	/** Sets the uniform with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param value the value */
	public void setUniformi (String name, int value) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform1i(location, value);
	}

	public void setUniformi (int location, int value) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform1i(location, value);
	}

	/** Sets the uniform with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value */
	public void setUniformi (String name, int value1, int value2) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform2i(location, value1, value2);
	}

	public void setUniformi (int location, int value1, int value2) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform2i(location, value1, value2);
	}

	/** Sets the uniform with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value */
	public void setUniformi (String name, int value1, int value2, int value3) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform3i(location, value1, value2, value3);
	}

	public void setUniformi (int location, int value1, int value2, int value3) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform3i(location, value1, value2, value3);
	}

	/** Sets the uniform with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 * @param value4 the fourth value */
	public void setUniformi (String name, int value1, int value2, int value3, int value4) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform4i(location, value1, value2, value3, value4);
	}

	public void setUniformi (int location, int value1, int value2, int value3, int value4) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform4i(location, value1, value2, value3, value4);
	}

	/** Sets the uniform with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param value the value */
	public void setUniformf (String name, float value) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform1f(location, value);
	}

	public void setUniformf (int location, float value) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform1f(location, value);
	}

	/** Sets the uniform with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value */
	public void setUniformf (String name, float value1, float value2) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform2f(location, value1, value2);
	}

	public void setUniformf (int location, float value1, float value2) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform2f(location, value1, value2);
	}

	/** Sets the uniform with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value */
	public void setUniformf (String name, float value1, float value2, float value3) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform3f(location, value1, value2, value3);
	}

	public void setUniformf (int location, float value1, float value2, float value3) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform3f(location, value1, value2, value3);
	}

	/** Sets the uniform with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 * @param value4 the fourth value */
	public void setUniformf (String name, float value1, float value2, float value3, float value4) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform4f(location, value1, value2, value3, value4);
	}

	public void setUniformf (int location, float value1, float value2, float value3, float value4) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform4f(location, value1, value2, value3, value4);
	}

	public void setUniform1fv (String name, float[] values, int offset, int length) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform1fv(location, length, values, offset);
	}

	public void setUniform1fv (int location, float[] values, int offset, int length) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform1fv(location, length, values, offset);
	}

	public void setUniform2fv (String name, float[] values, int offset, int length) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform2fv(location, length / 2, values, offset);
	}

	public void setUniform2fv (int location, float[] values, int offset, int length) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform2fv(location, length / 2, values, offset);
	}

	public void setUniform3fv (String name, float[] values, int offset, int length) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform3fv(location, length / 3, values, offset);
	}

	public void setUniform3fv (int location, float[] values, int offset, int length) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform3fv(location, length / 3, values, offset);
	}

	public void setUniform4fv (String name, float[] values, int offset, int length) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchUniformLocation(name);
		gl.glUniform4fv(location, length / 4, values, offset);
	}

	public void setUniform4fv (int location, float[] values, int offset, int length) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniform4fv(location, length / 4, values, offset);
	}

	/** Sets the uniform matrix with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param matrix the matrix */
	public void setUniformMatrix (String name, Matrix4 matrix) {
		setUniformMatrix(name, matrix, false);
	}

	/** Sets the uniform matrix with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param matrix the matrix
	 * @param transpose whether the matrix should be transposed */
	public void setUniformMatrix (String name, Matrix4 matrix, boolean transpose) {
		setUniformMatrix(fetchUniformLocation(name), matrix, transpose);
	}

	public void setUniformMatrix (int location, Matrix4 matrix) {
		setUniformMatrix(location, matrix, false);
	}

	public void setUniformMatrix (int location, Matrix4 matrix, boolean transpose) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniformMatrix4fv(location, 1, transpose, matrix.val, 0);
	}

	/** Sets the uniform matrix with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param matrix the matrix */
	public void setUniformMatrix (String name, Matrix3 matrix) {
		setUniformMatrix(name, matrix, false);
	}

	/** Sets the uniform matrix with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param matrix the matrix
	 * @param transpose whether the uniform matrix should be transposed */
	public void setUniformMatrix (String name, Matrix3 matrix, boolean transpose) {
		setUniformMatrix(fetchUniformLocation(name), matrix, transpose);
	}

	public void setUniformMatrix (int location, Matrix3 matrix) {
		setUniformMatrix(location, matrix, false);
	}

	public void setUniformMatrix (int location, Matrix3 matrix, boolean transpose) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniformMatrix3fv(location, 1, transpose, matrix.val, 0);
	}

	/** Sets an array of uniform matrices with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param buffer buffer containing the matrix data
	 * @param transpose whether the uniform matrix should be transposed */
	public void setUniformMatrix3fv (String name, FloatBuffer buffer, int count, boolean transpose) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		buffer.position(0);
		int location = fetchUniformLocation(name);
		gl.glUniformMatrix3fv(location, count, transpose, buffer);
	}

	/** Sets an array of uniform matrices with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param buffer buffer containing the matrix data
	 * @param transpose whether the uniform matrix should be transposed */
	public void setUniformMatrix4fv (String name, FloatBuffer buffer, int count, boolean transpose) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		buffer.position(0);
		int location = fetchUniformLocation(name);
		gl.glUniformMatrix4fv(location, count, transpose, buffer);
	}

	public void setUniformMatrix4fv (int location, float[] values, int offset, int length) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUniformMatrix4fv(location, length / 16, false, values, offset);
	}

	public void setUniformMatrix4fv (String name, float[] values, int offset, int length) {
		setUniformMatrix4fv(fetchUniformLocation(name), values, offset, length);
	}

	/** Sets the uniform with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param values x and y as the first and second values respectively */
	public void setUniformf (String name, Vector2 values) {
		setUniformf(name, values.x, values.y);
	}

	public void setUniformf (int location, Vector2 values) {
		setUniformf(location, values.x, values.y);
	}

	/** Sets the uniform with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param values x, y and z as the first, second and third values respectively */
	public void setUniformf (String name, Vector3 values) {
		setUniformf(name, values.x, values.y, values.z);
	}

	public void setUniformf (int location, Vector3 values) {
		setUniformf(location, values.x, values.y, values.z);
	}

	/** Sets the uniform with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the name of the uniform
	 * @param values r, g, b and a as the first through fourth values respectively */
	public void setUniformf (String name, Color values) {
		setUniformf(name, values.r, values.g, values.b, values.a);
	}

	public void setUniformf (int location, Color values) {
		setUniformf(location, values.r, values.g, values.b, values.a);
	}

	/** Sets the vertex attribute with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the attribute name
	 * @param size the number of components, must be &gt;= 1 and &lt;= 4
	 * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT,
	 *           GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
	 * @param normalize whether fixed point data should be normalized. Will not work on the desktop
	 * @param stride the stride in bytes between successive attributes
	 * @param buffer the buffer containing the vertex attributes. */
	public void setVertexAttribute (String name, int size, int type, boolean normalize, int stride, Buffer buffer) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchAttributeLocation(name);
		if (location == -1) return;
		gl.glVertexAttribPointer(location, size, type, normalize, stride, buffer);
	}

	public void setVertexAttribute (int location, int size, int type, boolean normalize, int stride, Buffer buffer) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glVertexAttribPointer(location, size, type, normalize, stride, buffer);
	}

	/** Sets the vertex attribute with the given name. The {@link NewShaderProgram} must be bound for this to work.
	 * 
	 * @param name the attribute name
	 * @param size the number of components, must be &gt;= 1 and &lt;= 4
	 * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT,
	 *           GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
	 * @param normalize whether fixed point data should be normalized. Will not work on the desktop
	 * @param stride the stride in bytes between successive attributes
	 * @param offset byte offset into the vertex buffer object bound to GL20.GL_ARRAY_BUFFER. */
	public void setVertexAttribute (String name, int size, int type, boolean normalize, int stride, int offset) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchAttributeLocation(name);
		if (location == -1) return;
		gl.glVertexAttribPointer(location, size, type, normalize, stride, offset);
	}

	public void setVertexAttribute (int location, int size, int type, boolean normalize, int stride, int offset) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glVertexAttribPointer(location, size, type, normalize, stride, offset);
	}

	/** @deprecated use {@link #bind()} instead, this method will be remove in future version */
	@Deprecated
	public void begin () {
		bind();
	}

	public void bind(){
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glUseProgram(program);
	}

	/** @deprecated no longer necessary, this method will be remove in future version */
	@Deprecated
	public void end () {
	}

	/** Disposes all resources associated with this shader. Must be called when the shader is no longer used. */
	public void dispose () {
		GL20 gl = Gdx.gl20;
		gl.glUseProgram(0);
		for(ShaderPart part : parts){
			gl.glDeleteShader(part.handle);
		}
		gl.glDeleteProgram(program);
		if (shaders.get(Gdx.app) != null) shaders.get(Gdx.app).removeValue(this, true);
	}

	/** Disables the vertex attribute with the given name
	 * 
	 * @param name the vertex attribute name */
	public void disableVertexAttribute (String name) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchAttributeLocation(name);
		if (location == -1) return;
		gl.glDisableVertexAttribArray(location);
	}

	public void disableVertexAttribute (int location) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glDisableVertexAttribArray(location);
	}

	/** Enables the vertex attribute with the given name
	 * 
	 * @param name the vertex attribute name */
	public void enableVertexAttribute (String name) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		int location = fetchAttributeLocation(name);
		if (location == -1) return;
		gl.glEnableVertexAttribArray(location);
	}

	public void enableVertexAttribute (int location) {
		GL20 gl = Gdx.gl20;
		checkManaged();
		gl.glEnableVertexAttribArray(location);
	}

	private void checkManaged () {
		if (invalidated) {
			compileShaders();
			invalidated = false;
		}
	}

	private void addManagedShader (Application app, NewShaderProgram shaderProgram) {
		Array<NewShaderProgram> managedResources = shaders.get(app);
		if (managedResources == null) managedResources = new Array<NewShaderProgram>();
		managedResources.add(shaderProgram);
		shaders.put(app, managedResources);
	}

	/** Invalidates all shaders so the next time they are used new handles are generated
	 * @param app */
	public static void invalidateAllShaderPrograms (Application app) {
		if (Gdx.gl20 == null) return;

		Array<NewShaderProgram> shaderArray = shaders.get(app);
		if (shaderArray == null) return;

		for (int i = 0; i < shaderArray.size; i++) {
			shaderArray.get(i).invalidated = true;
			shaderArray.get(i).checkManaged();
		}
	}

	public static void clearAllShaderPrograms (Application app) {
		shaders.remove(app);
	}

	public static String getManagedStatus () {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		builder.append("Managed shaders/app: { ");
		for (Application app : shaders.keys()) {
			builder.append(shaders.get(app).size);
			builder.append(" ");
		}
		builder.append("}");
		return builder.toString();
	}

	/** @return the number of managed shader programs currently loaded */
	public static int getNumManagedShaderPrograms () {
		return shaders.get(Gdx.app).size;
	}

	/** Sets the given attribute
	 * 
	 * @param name the name of the attribute
	 * @param value1 the first value
	 * @param value2 the second value
	 * @param value3 the third value
	 * @param value4 the fourth value */
	public void setAttributef (String name, float value1, float value2, float value3, float value4) {
		GL20 gl = Gdx.gl20;
		int location = fetchAttributeLocation(name);
		gl.glVertexAttrib4f(location, value1, value2, value3, value4);
	}

	IntBuffer params = BufferUtils.newIntBuffer(1);
	IntBuffer type = BufferUtils.newIntBuffer(1);

	private void fetchUniforms () {
		params.clear();
		Gdx.gl20.glGetProgramiv(program, GL20.GL_ACTIVE_UNIFORMS, params);
		int numUniforms = params.get(0);

		uniformNames = new String[numUniforms];

		for (int i = 0; i < numUniforms; i++) {
			params.clear();
			params.put(0, 1);
			type.clear();
			String name = Gdx.gl20.glGetActiveUniform(program, i, params, type);
			int location = Gdx.gl20.glGetUniformLocation(program, name);
			uniforms.put(name, location);
			uniformTypes.put(name, type.get(0));
			uniformSizes.put(name, params.get(0));
			uniformNames[i] = name;
		}
	}

	private void fetchAttributes () {
		params.clear();
		Gdx.gl20.glGetProgramiv(program, GL20.GL_ACTIVE_ATTRIBUTES, params);
		int numAttributes = params.get(0);

		attributeNames = new String[numAttributes];

		for (int i = 0; i < numAttributes; i++) {
			params.clear();
			params.put(0, 1);
			type.clear();
			String name = Gdx.gl20.glGetActiveAttrib(program, i, params, type);
			int location = Gdx.gl20.glGetAttribLocation(program, name);
			attributes.put(name, location);
			attributeTypes.put(name, type.get(0));
			attributeSizes.put(name, params.get(0));
			attributeNames[i] = name;
		}
	}

	/** @param name the name of the attribute
	 * @return whether the attribute is available in the shader */
	public boolean hasAttribute (String name) {
		return attributes.containsKey(name);
	}

	/** @param name the name of the attribute
	 * @return the type of the attribute, one of {@link GL20#GL_FLOAT}, {@link GL20#GL_FLOAT_VEC2} etc. */
	public int getAttributeType (String name) {
		return attributeTypes.get(name, 0);
	}

	/** @param name the name of the attribute
	 * @return the location of the attribute or -1. */
	public int getAttributeLocation (String name) {
		return attributes.get(name, -1);
	}

	/** @param name the name of the attribute
	 * @return the size of the attribute or 0. */
	public int getAttributeSize (String name) {
		return attributeSizes.get(name, 0);
	}

	/** @param name the name of the uniform
	 * @return whether the uniform is available in the shader */
	public boolean hasUniform (String name) {
		return uniforms.containsKey(name);
	}

	/** @param name the name of the uniform
	 * @return the type of the uniform, one of {@link GL20#GL_FLOAT}, {@link GL20#GL_FLOAT_VEC2} etc. */
	public int getUniformType (String name) {
		return uniformTypes.get(name, 0);
	}

	/** @param name the name of the uniform
	 * @return the location of the uniform or -1. */
	public int getUniformLocation (String name) {
		return uniforms.get(name, -1);
	}

	/** @param name the name of the uniform
	 * @return the size of the uniform or 0. */
	public int getUniformSize (String name) {
		return uniformSizes.get(name, 0);
	}

	/** @return the attributes */
	public String[] getAttributes () {
		return attributeNames;
	}

	/** @return the uniforms */
	public String[] getUniforms () {
		return uniformNames;
	}

	/** get final GLSL code for a pipeline stage.
	 * @param stage
	 * @return the sources of the shaders */
	public Array<String> getShaderSources (ShaderStage stage) {
		Array<String> sources = new Array<String>();
		for(ShaderPart part : parts) {
			if(part.stage == stage) {
				sources.add(part.finalCode);
			}
		}
		return sources;
	}
	
	/** @return the source of the vertex shader 
	 * @deprecated use {@link #getShaderSources(ShaderStage)} instead */
	@Deprecated
	public String getVertexShaderSource () {
		return getShaderSources(ShaderStage.vertex).first();
	}

	/** @return the source of the fragment shader
	 * @deprecated use {@link #getShaderSources(ShaderStage)} instead */
	@Deprecated
	public String getFragmentShaderSource () {
		return getShaderSources(ShaderStage.fragment).first();
	}

	/** @return the handle of the shader program */
	public int getHandle () {
		return program;
	}
}