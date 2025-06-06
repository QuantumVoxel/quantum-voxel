/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.MemoryUtil;

/** Class with static helper methods to increase the speed of array/direct buffer and direct buffer/direct buffer transfers
 * 
 * @author mzechner, xoppa */
public final class BufferUtilsEmu {

	private BufferUtilsEmu() {
	}

	static final Array<ByteBuffer> unsafeBuffers = new Array<ByteBuffer>();
	static int allocatedUnsafe = 0;

	/** Copies numFloats floats from src starting at offset to dst. Dst is assumed to be a direct {@link Buffer}. The method will
	 * crash if that is not the case. The position and limit of the buffer are ignored, the copy is placed at position 0 in the
	 * buffer. After the copying process the position of the buffer is set to 0 and its limit is set to numFloats * 4 if it is a
	 * ByteBuffer and numFloats if it is a FloatBuffer. In case the Buffer is neither a ByteBuffer nor a FloatBuffer the limit is
	 * not set. This is an expert method, use at your own risk.
	 * 
	 * @param src the source array
	 * @param dst the destination buffer, has to be a direct Buffer
	 * @param numFloats the number of floats to copy
	 * @param offset the offset in src to start copying from */
	public static void copy (float[] src, Buffer dst, int numFloats, int offset) {
		if (dst instanceof ByteBuffer)
			dst.limit(numFloats << 2);
		else if (dst instanceof FloatBuffer) dst.limit(numFloats);

		copyJni(src, dst, numFloats, offset);
		dst.position(0);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (byte[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (short[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 1));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 1);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay
	 * the same. <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param numElements the number of elements to copy.
	 * @param dst the destination Buffer, its position is used as an offset. */
	public static void copy (char[] src, int srcOffset, int numElements, Buffer dst) {
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 1);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay
	 * the same. <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param numElements the number of elements to copy.
	 * @param dst the destination Buffer, its position is used as an offset. */
	public static void copy (int[] src, int srcOffset, int numElements, Buffer dst) {
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay
	 * the same. <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param numElements the number of elements to copy.
	 * @param dst the destination Buffer, its position is used as an offset. */
	public static void copy (long[] src, int srcOffset, int numElements, Buffer dst) {
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay
	 * the same. <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param numElements the number of elements to copy.
	 * @param dst the destination Buffer, its position is used as an offset. */
	public static void copy (float[] src, int srcOffset, int numElements, Buffer dst) {
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay
	 * the same. <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param numElements the number of elements to copy.
	 * @param dst the destination Buffer, its position is used as an offset. */
	public static void copy (double[] src, int srcOffset, int numElements, Buffer dst) {
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (char[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 1));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 1);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (int[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 2));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (long[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 3));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (float[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 2));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (double[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 3));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
	}

	/** Copies the contents of src to dst, starting from the current position of src, copying numElements elements (using the data
	 * type of src, no matter the datatype of dst). The dst {@link Buffer#position()} is used as the writing offset. The position
	 * of both Buffers will stay the same. The limit of the src Buffer will stay the same. The limit of the dst Buffer will be set
	 * to dst.position() + numElements, where numElements are translated to the number of elements appropriate for the dst Buffer
	 * data type. <b>The Buffers must be direct Buffers with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source Buffer.
	 * @param dst the destination Buffer.
	 * @param numElements the number of elements to copy. */
	public static void copy (Buffer src, Buffer dst, int numElements) {
		int numBytes = elementsToBytes(src, numElements);
		dst.limit(dst.position() + bytesToElements(dst, numBytes));
		copyJni(src, positionInBytes(src), dst, positionInBytes(dst), numBytes);
	}

	/** Multiply float vector components within the buffer with the specified matrix. The {@link Buffer#position()} is used as the
	 * offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components of the vector (2 for xy, 3 for xyz or 4 for xyzw)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with */
	public static void transform (Buffer data, int dimensions, int strideInBytes, int count, Matrix4 matrix) {
		transform(data, dimensions, strideInBytes, count, matrix, 0);
	}

	/** Multiply float vector components within the buffer with the specified matrix. The {@link Buffer#position()} is used as the
	 * offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components of the vector (2 for xy, 3 for xyz or 4 for xyzw)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with */
	public static void transform (float[] data, int dimensions, int strideInBytes, int count, Matrix4 matrix) {
		transform(data, dimensions, strideInBytes, count, matrix, 0);
	}

	/** Multiply float vector components within the buffer with the specified matrix. The specified offset value is added to the
	 * {@link Buffer#position()} and used as the offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components of the vector (2 for xy, 3 for xyz or 4 for xyzw)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with
	 * @param offset The offset within the buffer (in bytes relative to the current position) to the vector */
	public static void transform (Buffer data, int dimensions, int strideInBytes, int count, Matrix4 matrix, int offset) {
		switch (dimensions) {
		case 4:
			transformV4M4Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
			break;
		case 3:
			transformV3M4Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
			break;
		case 2:
			transformV2M4Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	/** Multiply float vector components within the buffer with the specified matrix. The specified offset value is added to the
	 * {@link Buffer#position()} and used as the offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components of the vector (2 for xy, 3 for xyz or 4 for xyzw)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with
	 * @param offset The offset within the buffer (in bytes relative to the current position) to the vector */
	public static void transform (float[] data, int dimensions, int strideInBytes, int count, Matrix4 matrix, int offset) {
		switch (dimensions) {
		case 4:
			transformV4M4Jni(data, strideInBytes, count, matrix.val, offset);
			break;
		case 3:
			transformV3M4Jni(data, strideInBytes, count, matrix.val, offset);
			break;
		case 2:
			transformV2M4Jni(data, strideInBytes, count, matrix.val, offset);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	/** Multiply float vector components within the buffer with the specified matrix. The {@link Buffer#position()} is used as the
	 * offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components (x, y, z) of the vector (2 for xy or 3 for xyz)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with */
	public static void transform (Buffer data, int dimensions, int strideInBytes, int count, Matrix3 matrix) {
		transform(data, dimensions, strideInBytes, count, matrix, 0);
	}

	/** Multiply float vector components within the buffer with the specified matrix. The {@link Buffer#position()} is used as the
	 * offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components (x, y, z) of the vector (2 for xy or 3 for xyz)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with */
	public static void transform (float[] data, int dimensions, int strideInBytes, int count, Matrix3 matrix) {
		transform(data, dimensions, strideInBytes, count, matrix, 0);
	}

	/** Multiply float vector components within the buffer with the specified matrix. The specified offset value is added to the
	 * {@link Buffer#position()} and used as the offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components (x, y, z) of the vector (2 for xy or 3 for xyz)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with,
	 * @param offset The offset within the buffer (in bytes relative to the current position) to the vector */
	public static void transform (Buffer data, int dimensions, int strideInBytes, int count, Matrix3 matrix, int offset) {
		switch (dimensions) {
		case 3:
			transformV3M3Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
			break;
		case 2:
			transformV2M3Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	/** Multiply float vector components within the buffer with the specified matrix. The specified offset value is added to the
	 * {@link Buffer#position()} and used as the offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components (x, y, z) of the vector (2 for xy or 3 for xyz)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with,
	 * @param offset The offset within the buffer (in bytes relative to the current position) to the vector */
	public static void transform (float[] data, int dimensions, int strideInBytes, int count, Matrix3 matrix, int offset) {
		switch (dimensions) {
		case 3:
			transformV3M3Jni(data, strideInBytes, count, matrix.val, offset);
			break;
		case 2:
			transformV2M3Jni(data, strideInBytes, count, matrix.val, offset);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public static long findFloats (Buffer vertex, int strideInBytes, Buffer vertices, int numVertices) {
		return find(vertex, positionInBytes(vertex), strideInBytes, vertices, positionInBytes(vertices), numVertices);
	}

	public static long findFloats (float[] vertex, int strideInBytes, Buffer vertices, int numVertices) {
		return find(vertex, 0, strideInBytes, vertices, positionInBytes(vertices), numVertices);
	}

	public static long findFloats (Buffer vertex, int strideInBytes, float[] vertices, int numVertices) {
		return find(vertex, positionInBytes(vertex), strideInBytes, vertices, 0, numVertices);
	}

	public static long findFloats (float[] vertex, int strideInBytes, float[] vertices, int numVertices) {
		return find(vertex, 0, strideInBytes, vertices, 0, numVertices);
	}

	public static long findFloats (Buffer vertex, int strideInBytes, Buffer vertices, int numVertices, float epsilon) {
		return find(vertex, positionInBytes(vertex), strideInBytes, vertices, positionInBytes(vertices), numVertices, epsilon);
	}

	public static long findFloats (float[] vertex, int strideInBytes, Buffer vertices, int numVertices, float epsilon) {
		return find(vertex, 0, strideInBytes, vertices, positionInBytes(vertices), numVertices, epsilon);
	}

	public static long findFloats (Buffer vertex, int strideInBytes, float[] vertices, int numVertices, float epsilon) {
		return find(vertex, positionInBytes(vertex), strideInBytes, vertices, 0, numVertices, epsilon);
	}

	public static long findFloats (float[] vertex, int strideInBytes, float[] vertices, int numVertices, float epsilon) {
		return find(vertex, 0, strideInBytes, vertices, 0, numVertices, epsilon);
	}

	private static int positionInBytes (Buffer dst) {
		if (dst instanceof ByteBuffer)
			return dst.position();
		else if (dst instanceof ShortBuffer)
			return dst.position() << 1;
		else if (dst instanceof CharBuffer)
			return dst.position() << 1;
		else if (dst instanceof IntBuffer)
			return dst.position() << 2;
		else if (dst instanceof LongBuffer)
			return dst.position() << 3;
		else if (dst instanceof FloatBuffer)
			return dst.position() << 2;
		else if (dst instanceof DoubleBuffer)
			return dst.position() << 3;
		else
			throw new GdxRuntimeException("Can't copy to a " + dst.getClass().getName() + " instance");
	}

	private static int bytesToElements (Buffer dst, int bytes) {
		if (dst instanceof ByteBuffer)
			return bytes;
		else if (dst instanceof ShortBuffer)
			return bytes >>> 1;
		else if (dst instanceof CharBuffer)
			return bytes >>> 1;
		else if (dst instanceof IntBuffer)
			return bytes >>> 2;
		else if (dst instanceof LongBuffer)
			return bytes >>> 3;
		else if (dst instanceof FloatBuffer)
			return bytes >>> 2;
		else if (dst instanceof DoubleBuffer)
			return bytes >>> 3;
		else
			throw new GdxRuntimeException("Can't copy to a " + dst.getClass().getName() + " instance");
	}

	private static int elementsToBytes (Buffer dst, int elements) {
		if (dst instanceof ByteBuffer)
			return elements;
		else if (dst instanceof ShortBuffer)
			return elements << 1;
		else if (dst instanceof CharBuffer)
			return elements << 1;
		else if (dst instanceof IntBuffer)
			return elements << 2;
		else if (dst instanceof LongBuffer)
			return elements << 3;
		else if (dst instanceof FloatBuffer)
			return elements << 2;
		else if (dst instanceof DoubleBuffer)
			return elements << 3;
		else
			throw new GdxRuntimeException("Can't copy to a " + dst.getClass().getName() + " instance");
	}

	public static FloatBuffer newFloatBuffer (int numFloats) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numFloats * 4);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asFloatBuffer();
	}

	public static DoubleBuffer newDoubleBuffer (int numDoubles) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numDoubles * 8);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asDoubleBuffer();
	}

	public static ByteBuffer newByteBuffer (int numBytes) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numBytes);
		buffer.order(ByteOrder.nativeOrder());
		return buffer;
	}

	public static ShortBuffer newShortBuffer (int numShorts) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numShorts * 2);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asShortBuffer();
	}

	public static CharBuffer newCharBuffer (int numChars) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numChars * 2);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asCharBuffer();
	}

	public static IntBuffer newIntBuffer (int numInts) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numInts * 4);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asIntBuffer();
	}

	public static LongBuffer newLongBuffer (int numLongs) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numLongs * 8);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asLongBuffer();
	}

	public static void disposeUnsafeByteBuffer (ByteBuffer buffer) {
		int size = buffer.capacity();
		synchronized (unsafeBuffers) {
			if (!unsafeBuffers.removeValue(buffer, true))
				throw new IllegalArgumentException("buffer not allocated with newUnsafeByteBuffer or already disposed");
		}
		allocatedUnsafe -= size;
	}

	public static boolean isUnsafeByteBuffer (ByteBuffer buffer) {
		synchronized (unsafeBuffers) {
			return unsafeBuffers.contains(buffer, true);
		}
	}

	/** Allocates a new direct ByteBuffer from native heap memory using the native byte order. Needs to be disposed with
	 * {@link #disposeUnsafeByteBuffer(ByteBuffer)}. */
	public static ByteBuffer newUnsafeByteBuffer (int numBytes) {
		ByteBuffer buffer = newDisposableByteBuffer(numBytes);
		buffer.order(ByteOrder.nativeOrder());
		allocatedUnsafe += numBytes;
		synchronized (unsafeBuffers) {
			unsafeBuffers.add(buffer);
		}
		return buffer;
	}

	/** Returns the address of the Buffer, it assumes it is an unsafe buffer.
	 * @param buffer The Buffer to ask the address for.
	 * @return the address of the Buffer. */
	public static long getUnsafeBufferAddress (Buffer buffer) {
		return getBufferAddress(buffer) + buffer.position();
	}

	/** Registers the given ByteBuffer as an unsafe ByteBuffer. The ByteBuffer must have been allocated in native code, pointing to
	 * a memory region allocated via malloc. Needs to be disposed with {@link #disposeUnsafeByteBuffer(ByteBuffer)}.
	 * @param buffer the {@link ByteBuffer} to register
	 * @return the ByteBuffer passed to the method */
	public static ByteBuffer newUnsafeByteBuffer (ByteBuffer buffer) {
		allocatedUnsafe += buffer.capacity();
		synchronized (unsafeBuffers) {
			unsafeBuffers.add(buffer);
		}
		return buffer;
	}

	/** @return the number of bytes allocated with {@link #newUnsafeByteBuffer(int)} */
	public static int getAllocatedBytesUnsafe () {
		return allocatedUnsafe;
	}

	/**
	 * Allocates an unsafe ByteBuffer (off-heap).
	 */
	public static ByteBuffer newDisposableByteBuffer(int numBytes) {
		ByteBuffer buffer = newUnsafeByteBuffer(numBytes);
		buffer.clear();
		return buffer;
	}

	/**
	 * Frees a ByteBuffer allocated using {@link #newDisposableByteBuffer}.
	 */
	private static void freeMemoryJni(ByteBuffer buffer) {
		//
	}

	/**
	 * Gets the native memory address of the buffer.
	 */
	public static long getBufferAddress(Buffer buffer) {
		return MemoryUtil.getAddress0(buffer);
	}

	/**
	 * Clears the buffer to zero.
	 */
	private static void clearJni(ByteBuffer buffer, int numBytes) {
		BufferUtils.zeroBuffer(buffer);
	}

	/**
	 * Copies float[] into a FloatBuffer.
	 */
	private static void copyJni(float[] src, Buffer dst, int numFloats, int offset) {
		if (!(dst instanceof FloatBuffer)) throw new IllegalArgumentException("Expected FloatBuffer");
        FloatBuffer fb = (FloatBuffer) dst;
        fb.put(src, offset, numFloats);
	}

	private static void copyJni(byte[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes) {
		if (!(dst instanceof ByteBuffer)) throw new IllegalArgumentException("Expected ByteBuffer");
        ByteBuffer bb = (ByteBuffer) dst;
        bb.position(dstOffset);
		bb.put(src, srcOffset, numBytes);
	}

	private static void copyJni(char[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes) {
		if (!(dst instanceof CharBuffer)) throw new IllegalArgumentException("Expected CharBuffer");
        CharBuffer cb = (CharBuffer) dst;
        for (int i = 0; i < numBytes / 2; i++)
			cb.put(dstOffset + i, src[srcOffset + i]);
	}

	private static void copyJni(short[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes) {
		if (!(dst instanceof ShortBuffer)) throw new IllegalArgumentException("Expected ShortBuffer");
        ShortBuffer sb = (ShortBuffer) dst;
        for (int i = 0; i < numBytes / 2; i++)
			sb.put(dstOffset + i, src[srcOffset + i]);
	}

	private static void copyJni(int[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes) {
		if (!(dst instanceof IntBuffer)) throw new IllegalArgumentException("Expected IntBuffer");
        IntBuffer ib = (IntBuffer) dst;
        for (int i = 0; i < numBytes / 4; i++)
			ib.put(dstOffset + i, src[srcOffset + i]);
	}

	private static void copyJni(long[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes) {
		if (!(dst instanceof LongBuffer)) throw new IllegalArgumentException("Expected LongBuffer");
        LongBuffer lb = (LongBuffer) dst;
        for (int i = 0; i < numBytes / 8; i++)
			lb.put(dstOffset + i, src[srcOffset + i]);
	}

	private static void copyJni(float[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes) {
		if (!(dst instanceof FloatBuffer)) throw new IllegalArgumentException("Expected FloatBuffer");
        FloatBuffer fb = (FloatBuffer) dst;
        for (int i = 0; i < numBytes / 4; i++)
			fb.put(dstOffset + i, src[srcOffset + i]);
	}

	private static void copyJni(double[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes) {
		if (!(dst instanceof DoubleBuffer)) throw new IllegalArgumentException("Expected DoubleBuffer");
        DoubleBuffer db = (DoubleBuffer) dst;
        for (int i = 0; i < numBytes / 8; i++)
			db.put(dstOffset + i, src[srcOffset + i]);
	}

	private static void copyJni(Buffer src, int srcOffset, Buffer dst, int dstOffset, int numBytes) {
		if (dst instanceof ByteBuffer) {
			ByteBuffer bb = (ByteBuffer) dst;
			bb.position(dstOffset);
			if (src instanceof ByteBuffer) {
				ByteBuffer bbSrc = (ByteBuffer) src;
				byte[] bbSrcArray = new byte[numBytes];
				bbSrc.get(bbSrcArray);
				bb.put(bbSrcArray, srcOffset, numBytes);
			} else if (src instanceof ShortBuffer) {
				ShortBuffer sbSrc = (ShortBuffer) src;
				short[] sbSrcArray = new short[numBytes / 2];
				sbSrc.get(sbSrcArray);
				bb.asShortBuffer().put(sbSrcArray, srcOffset, numBytes / 2);
			} else if (src instanceof CharBuffer) {
				CharBuffer cbSrc = (CharBuffer) src;
				char[] cbSrcArray = new char[numBytes / 2];
				cbSrc.get(cbSrcArray);
				bb.asCharBuffer().put(cbSrcArray, srcOffset, numBytes / 2);
			} else if (src instanceof IntBuffer) {
				IntBuffer ibSrc = (IntBuffer) src;
				int[] ibSrcArray = new int[numBytes / 4];
				ibSrc.get(ibSrcArray);
				bb.asIntBuffer().put(ibSrcArray, srcOffset, numBytes / 4);
			} else if (src instanceof LongBuffer) {
				LongBuffer lbSrc = (LongBuffer) src;
				long[] lbSrcArray = new long[numBytes / 8];
				lbSrc.get(lbSrcArray);
				bb.asLongBuffer().put(lbSrcArray, srcOffset, numBytes / 8);
			} else if (src instanceof FloatBuffer) {
				FloatBuffer fbSrc = (FloatBuffer) src;
				float[] fbSrcArray = new float[numBytes / 4];
				fbSrc.get(fbSrcArray);
				bb.asFloatBuffer().put(fbSrcArray, srcOffset, numBytes / 4);
			} else if (src instanceof DoubleBuffer) {
				DoubleBuffer dbSrc = (DoubleBuffer) src;
				double[] dbSrcArray = new double[numBytes / 8];
				dbSrc.get(dbSrcArray);
				bb.asDoubleBuffer().put(dbSrcArray, srcOffset, numBytes / 8);
			} else
				throw new IllegalArgumentException("Expected ByteBuffer, ShortBuffer, CharBuffer, IntBuffer, or LongBuffer");
		} else if (dst instanceof ShortBuffer) {
			ShortBuffer sbDst = (ShortBuffer) dst;
			short[] sbDstArray = new short[numBytes / 2];
			if (src instanceof ShortBuffer) {
				ShortBuffer sbSrc = (ShortBuffer) src;
				sbSrc.get(sbDstArray, srcOffset, numBytes / 2);
				sbDst.put(sbDstArray);
            }
		} else if (dst instanceof CharBuffer) {
			CharBuffer cbDst = (CharBuffer) dst;
			char[] cbDstArray = new char[numBytes / 2];
			if (src instanceof CharBuffer) {
				CharBuffer cbSrc = (CharBuffer) src;
				cbSrc.get(cbDstArray, srcOffset, numBytes / 2);
				cbDst.put(cbDstArray);
			}
		} else if (dst instanceof IntBuffer) {
			IntBuffer ibDst = (IntBuffer) dst;
			int[] ibDstArray = new int[numBytes / 4];
			if (src instanceof IntBuffer) {
				IntBuffer ibSrc = (IntBuffer) src;
				ibSrc.get(ibDstArray, srcOffset, numBytes / 4);
				ibDst.put(ibDstArray);
			}
		} else if (dst instanceof LongBuffer) {
			LongBuffer lbDst = (LongBuffer) dst;
			long[] lbDstArray = new long[numBytes / 8];
			if (src instanceof LongBuffer) {
				LongBuffer lbSrc = (LongBuffer) src;
				lbSrc.get(lbDstArray, srcOffset, numBytes / 8);
				lbDst.put(lbDstArray);
			}
		}
	}
	private static void transformV4M4Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes) {
		int stride = strideInBytes / 4;
		int offset = offsetInBytes / 4;
		FloatBuffer fb = data instanceof FloatBuffer ? (FloatBuffer) data : (FloatBuffer) data;
		for (int i = 0; i < count; i++) {
			int pos = offset + i * stride;
			float x = fb.get(pos);
			float y = fb.get(pos + 1);
			float z = fb.get(pos + 2);
			float w = fb.get(pos + 3);
			fb.put(pos,     matrix[0] * x + matrix[4] * y + matrix[8]  * z + matrix[12] * w);
			fb.put(pos + 1, matrix[1] * x + matrix[5] * y + matrix[9]  * z + matrix[13] * w);
			fb.put(pos + 2, matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14] * w);
			fb.put(pos + 3, matrix[3] * x + matrix[7] * y + matrix[11] * z + matrix[15] * w);
		}
	}

	private static void transformV4M4Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes) {
		int stride = strideInBytes / 4;
		int offset = offsetInBytes / 4;
		for (int i = 0; i < count; i++) {
			int pos = offset + i * stride;
			float x = data[pos];
			float y = data[pos + 1];
			float z = data[pos + 2];
			float w = data[pos + 3];
			data[pos]     = matrix[0] * x + matrix[4] * y + matrix[8]  * z + matrix[12] * w;
			data[pos + 1] = matrix[1] * x + matrix[5] * y + matrix[9]  * z + matrix[13] * w;
			data[pos + 2] = matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14] * w;
			data[pos + 3] = matrix[3] * x + matrix[7] * y + matrix[11] * z + matrix[15] * w;
		}
	}

	private static void transformV3M4Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes) {
		int stride = strideInBytes / 4;
		int offset = offsetInBytes / 4;
		FloatBuffer fb = data instanceof FloatBuffer ? (FloatBuffer) data : (FloatBuffer) data;
		for (int i = 0; i < count; i++) {
			int pos = offset + i * stride;
			float x = fb.get(pos);
			float y = fb.get(pos + 1);
			float z = fb.get(pos + 2);
			fb.put(pos,     matrix[0] * x + matrix[4] * y + matrix[8]  * z + matrix[12]);
			fb.put(pos + 1, matrix[1] * x + matrix[5] * y + matrix[9]  * z + matrix[13]);
			fb.put(pos + 2, matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14]);
		}
	}

	private static void transformV3M4Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes) {
		int stride = strideInBytes / 4;
		int offset = offsetInBytes / 4;
		for (int i = 0; i < count; i++) {
			int pos = offset + i * stride;
			float x = data[pos];
			float y = data[pos + 1];
			float z = data[pos + 2];
			data[pos]     = matrix[0] * x + matrix[4] * y + matrix[8]  * z + matrix[12];
			data[pos + 1] = matrix[1] * x + matrix[5] * y + matrix[9]  * z + matrix[13];
			data[pos + 2] = matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14];
		}
	}

	private static void transformV2M4Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes) {
		int stride = strideInBytes / 4;
		int offset = offsetInBytes / 4;
		FloatBuffer fb = data instanceof FloatBuffer ? (FloatBuffer) data : (FloatBuffer) data;
		for (int i = 0; i < count; i++) {
			int pos = offset + i * stride;
			float x = fb.get(pos);
			float y = fb.get(pos + 1);
			fb.put(pos,     matrix[0] * x + matrix[4] * y + matrix[12]);
			fb.put(pos + 1, matrix[1] * x + matrix[5] * y + matrix[13]);
		}
	}

	private static void transformV2M4Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes) {
		int stride = strideInBytes / 4;
		int offset = offsetInBytes / 4;
		for (int i = 0; i < count; i++) {
			int pos = offset + i * stride;
			float x = data[pos];
			float y = data[pos + 1];
			data[pos]     = matrix[0] * x + matrix[4] * y + matrix[12];
			data[pos + 1] = matrix[1] * x + matrix[5] * y + matrix[13];
		}
	}

	private static void transformV3M3Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes) {
		int stride = strideInBytes / 4;
		int offset = offsetInBytes / 4;
		FloatBuffer fb = data instanceof FloatBuffer ? (FloatBuffer) data : (FloatBuffer) data;
		for (int i = 0; i < count; i++) {
			int pos = offset + i * stride;
			float x = fb.get(pos);
			float y = fb.get(pos + 1);
			float z = fb.get(pos + 2);
			fb.put(pos,     matrix[0] * x + matrix[3] * y + matrix[6] * z);
			fb.put(pos + 1, matrix[1] * x + matrix[4] * y + matrix[7] * z);
			fb.put(pos + 2, matrix[2] * x + matrix[5] * y + matrix[8] * z);
		}
	}

	private static void transformV3M3Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes) {
		int stride = strideInBytes / 4;
		int offset = offsetInBytes / 4;
		for (int i = 0; i < count; i++) {
			int pos = offset + i * stride;
			float x = data[pos];
			float y = data[pos + 1];
			float z = data[pos + 2];
			data[pos]     = matrix[0] * x + matrix[3] * y + matrix[6] * z;
			data[pos + 1] = matrix[1] * x + matrix[4] * y + matrix[7] * z;
			data[pos + 2] = matrix[2] * x + matrix[5] * y + matrix[8] * z;
		}
	}

	private static void transformV2M3Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes) {
		int stride = strideInBytes / 4;
		int offset = offsetInBytes / 4;
		FloatBuffer fb = data instanceof FloatBuffer ? (FloatBuffer) data : (FloatBuffer) data;
		for (int i = 0; i < count; i++) {
			int pos = offset + i * stride;
			float x = fb.get(pos);
			float y = fb.get(pos + 1);
			fb.put(pos,     matrix[0] * x + matrix[3] * y);
			fb.put(pos + 1, matrix[1] * x + matrix[4] * y);
		}
	}

	private static void transformV2M3Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes) {
		int stride = strideInBytes / 4;
		int offset = offsetInBytes / 4;
		for (int i = 0; i < count; i++) {
			int pos = offset + i * stride;
			float x = data[pos];
			float y = data[pos + 1];
			data[pos]     = matrix[0] * x + matrix[3] * y;
			data[pos + 1] = matrix[1] * x + matrix[4] * y;
		}
	}

	private static boolean compareVertex(float[] vertexA, int offsetA, float[] vertexB, int offsetB, int stride, float epsilon) {
		for (int i = 0; i < stride; i++) {
			if (Math.abs(vertexA[offsetA + i] - vertexB[offsetB + i]) > epsilon) {
				return false;
			}
		}
		return true;
	}
	private static int findVertex(float[] vertex, int vertexOffset, int stride, float[] vertices, int verticesOffset, int numVertices, float epsilon) {
		for (int i = 0; i < numVertices; i++) {
			int offset = verticesOffset + i * stride;
			if (compareVertex(vertex, vertexOffset, vertices, offset, stride, epsilon)) {
				return i;
			}
		}
		return -1;
	}
	public static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices) {
		return find(vertex, vertexOffsetInBytes, strideInBytes, vertices, verticesOffsetInBytes, numVertices, 0.0001f);
	}

	public static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices, float epsilon) {
		int stride = strideInBytes / 4;
		int vertexOffset = vertexOffsetInBytes / 4;
		int verticesOffset = verticesOffsetInBytes / 4;
		return findVertex(vertex, vertexOffset, stride, vertices, verticesOffset, numVertices, epsilon);
	}

	public static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices) {
		return find(vertex, vertexOffsetInBytes, strideInBytes, vertices, verticesOffsetInBytes, numVertices, 0.0001f);
	}

	public static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices, float epsilon) {
		FloatBuffer vb = ((ByteBuffer) vertex).asFloatBuffer();
		float[] vertexArray = new float[strideInBytes / 4];
		vb.position(vertexOffsetInBytes / 4);
		vb.get(vertexArray);
		return find(vertexArray, 0, strideInBytes, vertices, verticesOffsetInBytes, numVertices, epsilon);
	}

	public static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices) {
		return find(vertex, vertexOffsetInBytes, strideInBytes, vertices, verticesOffsetInBytes, numVertices, 0.0001f);
	}

	public static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices, float epsilon) {
		FloatBuffer vb = ((ByteBuffer) vertices).asFloatBuffer();
		float[] verticesArray = new float[numVertices * (strideInBytes / 4)];
		vb.position(verticesOffsetInBytes / 4);
		vb.get(verticesArray);
		return find(vertex, vertexOffsetInBytes, strideInBytes, verticesArray, 0, numVertices, epsilon);
	}

	public static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices) {
		return find(vertex, vertexOffsetInBytes, strideInBytes, vertices, verticesOffsetInBytes, numVertices, 0.0001f);
	}

	public static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices, float epsilon) {
		FloatBuffer vb = ((ByteBuffer) vertex).asFloatBuffer();
		float[] vertexArray = new float[strideInBytes / 4];
		vb.position(vertexOffsetInBytes / 4);
		vb.get(vertexArray);

		FloatBuffer vbs = ((ByteBuffer) vertices).asFloatBuffer();
		float[] verticesArray = new float[numVertices * (strideInBytes / 4)];
		vbs.position(verticesOffsetInBytes / 4);
		vbs.get(verticesArray);

		return find(vertexArray, 0, strideInBytes, verticesArray, 0, numVertices, epsilon);
	}
}
