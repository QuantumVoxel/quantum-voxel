package dev.ultreon.mixinprovider;

/**
 * A shader program part is a section of GLSL code running at a programmable pipeline stage.
 */
public class ShaderPart {
	/** stage in the pipeline */
	final ShaderStage stage;
	/** original shader source code */
	final String source;
	/** shader handle */
	int handle;
	/** final shader code (prepended code + original source code) */
	String finalCode;

	/** @param stage shader stage, typically one of {@link ShaderStage} preset eg. {@link ShaderStage#vertex},
	 *           {@link ShaderStage#fragment}
	 * @param source shader source code */
	public ShaderPart (ShaderStage stage, String source) {
		this.stage = stage;
		this.source = source;
	}
}