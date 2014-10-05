package at.dhyan.open_imaging;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Copyright 2014 Dhyan Blum
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
/**
 * <p>
 * A decoder capable of processing a GIF data stream to render the graphics
 * contained in it. This implementation follows the official <A
 * HREF="http://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF specification</A>.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <p>
 * 
 * <pre>
 * final GifEncoder decoder = new GifEncoder();
 * final GifImage gifImage = decoder.read(int[] data);
 * final int width = gifImage.getWidth();
 * final int height = gifImage.getHeight();
 * final int frameCount = gifImage.getFrameCount();
 * for (int i = 0; i < frameCount; i++) {
 * 	final BufferedImage image = gifImage.getFrame(i);
 * 	final int delay = gif.getDelay(i);
 * }
 * </pre>
 * 
 * </p>
 * 
 * @author Dhyan Blum
 * @version 1.01 September 2014
 * 
 */
public final class GifDecoder {
	final class BitReader {
		private int bitPos; // Next bit to read
		private final byte[] in; // Data array
		private final int bitsTotal; // Total number of bits in the array

		// To avoid costly bounds checks, 'in' needs 3 more 0-bytes at the end
		BitReader(final byte[] in) {
			this.in = in;
			bitPos = 0;
			bitsTotal = in.length << 3; // array length * 8 = number of bits
		}

		private final int bitsAvailable() {
			return bitsTotal - bitPos;
		}

		private final int read(final int bits) {
			// Byte indices: (bitPos / 8), (bitPos / 8) + 1, (bitPos / 8) + 2
			int i = bitPos >>> 3; // Byte = bit / 8
			// Bits we'll shift to the right, AND 7 is the same as MODULO 8
			final int rBits = bitPos & 7;
			// Byte 0 to 2, AND to get their unsigned values
			final int b0 = in[i++] & 0xFF, b1 = in[i++] & 0xFF, b2 = in[i] & 0xFF;
			// Glue the bytes together, don't do more shifting than necessary
			final int buf = ((b2 << 8 | b1) << 8 | b0) >>> rBits;
			bitPos += bits;
			return buf & MASK[bits]; // Kill the unwanted higher bits
		}
	}

	final class CodeTable {
		private final int[][] tbl; // Maps codes to lists of color indices
		private final int initTableSize; // Number of colors +2 for CLEAR + EOI
		private int currCodeSize; // Current code size, maximum is 12 bits
		private int nextCode; // Next available code for a new entry
		private final int initCodeSize; // Initial code size
		private int nextCodeLimit; // Increase codeSize when nextCode == limit
		private final int initCodeLimit; // First code limit

		CodeTable(final GifFrame fr, final int[] activeColTbl) {
			final int numColors = activeColTbl.length;
			initCodeSize = fr.firstCodeSize;
			initCodeLimit = MASK[initCodeSize]; // 2^initCodeSize - 1
			initTableSize = numColors + 2;
			nextCode = initTableSize;
			tbl = new int[4096][];
			for (int c = 0; c < numColors; c++) {
				tbl[c] = new int[] { activeColTbl[c] }; // Translated color
			}
			tbl[numColors] = new int[] { numColors }; // CLEAR
			tbl[numColors + 1] = new int[] { numColors + 1 }; // EOI
			// Locate transparent color in code table and set to 0
			if (fr.transpColFlag && fr.transpColIndex < numColors) {
				tbl[fr.transpColIndex][0] = 0;
			}
		}

		private final int add(final int[] indices) {
			if (nextCode < 4096) {
				if (nextCode == nextCodeLimit && currCodeSize < 12) {
					currCodeSize++; // Max code size is 12
					nextCodeLimit = MASK[currCodeSize]; // 2^currCodeSize - 1
				}
				tbl[nextCode++] = indices;
			}
			return currCodeSize;
		}

		private final int clear() {
			currCodeSize = initCodeSize;
			nextCodeLimit = initCodeLimit;
			nextCode = initTableSize; // Don't recreate table, reset pointer
			return currCodeSize;
		}
	}

	final class GifFrame {
		// Graphic control extension (optional)
		// Disposal: 0=NO_ACTION, 1=NO_DISPOSAL, 2=RESTORE_BG, 3=RESTORE_PREV
		private int disposalMethod; // 0-3 as above, 4-7 undefined
		private boolean transpColFlag; // 1 Bit
		private int delay; // Unsigned, LSByte first, n * 1/100 * s
		private int transpColIndex; // 1 Byte
		// Image descriptor
		private int left; // Position on the canvas from the left
		private int top; // Position on the canvas from the top
		private int width; // May be smaller than the base image
		private int height; // May be smaller than the base image
		private boolean hasLocColTbl; // Has local color table? 1 Bit
		private boolean interlaceFlag; // Is an interlace image? 1 Bit
		private boolean sortFlag; // Are colors sorted? 1 Bit
		private int sizeOfLocColTbl; // Size of the local color table, 3 Bits
		private int[] localColTbl; // Local color table (optional)
		// Image data
		private int minCodeSize; // LZW minimum code size
		private int firstCodeSize;
		private int clearCode;
		private int endOfInfoCode;
		private byte[] data;
	}

	public final class GifImage {
		public String header; // Bytes 0-5, GIF87a or GIF89a
		private int width; // Unsigned 16 Bit, least significant byte first
		private int height; // Unsigned 16 Bit, least significant byte first
		public boolean hasGlobColTbl; // 1 Bit
		private int colorResolution; // 3 Bits
		private boolean sortFlag; // Whether colors are sorted, 1 Bit
		public int sizeOfGlobColTbl; // 2^(val(3 Bits) + 1), see spec
		public int bgColIndex; // Background color index, 1 Byte
		private int pxAspectRatio; // Pixel aspect ratio, 1 Byte
		public int[] globalColTbl; // Global color table
		private final List<GifFrame> frames = new ArrayList<GifFrame>();
		public String appId = ""; // 8 Bytes at in[i+3], usually "NETSCAPE"
		public String appAuthCode = ""; // 3 Bytes at in[i+11], usually "2.0"
		public int repetitions = 0; // 0: infinite loop, N: number of loops
		private BufferedImage img = null; // Currently drawn frame
		private BufferedImage prevImg = null; // Last drawn frame
		private int prevIndex; // Index of the last drawn frame
		private int prevDisposal; // Disposal of the previous frame

		private final int[] deinterlace(final int[] pixels, final GifFrame fr) {
			final int w = fr.width, h = fr.height, group2, group3, group4;
			final int[] dest = new int[pixels.length];
			// Interlaced images are divided in 4 groups of pixel lines
			group2 = (int) Math.ceil(h / 8.0); // Start index of group 2
			group3 = group2 + (int) Math.ceil((h - 4) / 8.0); // Start index
			group4 = group3 + (int) Math.ceil((h - 2) / 4.0); // Start index
			// Group 1 contains every 8th line starting from 0
			for (int y = 0; y < group2; y++) {
				final int destPos = w * y * 8;
				System.arraycopy(pixels, w * y, dest, destPos, w);
			} // Group 2 contains every 8th line starting from 4
			for (int y = group2; y < group3; y++) {
				final int destY = (y - group2) * 8 + 4, destPos = w * destY;
				System.arraycopy(pixels, w * y, dest, destPos, w);
			} // Group 3 contains every 4th line starting from 2
			for (int y = group3; y < group4; y++) {
				final int destY = (y - group3) * 4 + 2, destPos = w * destY;
				System.arraycopy(pixels, w * y, dest, destPos, w);
			} // Group 4 contains every 2nd line starting from 1 (biggest group)
			for (int y = group4; y < h; y++) {
				final int destY = (y - group4) * 2 + 1, destPos = w * destY;
				System.arraycopy(pixels, w * y, dest, destPos, w);
			}
			return dest; // All pixel lines have now been rearranged
		}

		private final void drawFrame(final GifFrame fr) {
			int bgCol = 0; // Current background color value
			final int[] activeColTbl; // Active color table
			if (fr.hasLocColTbl) {
				activeColTbl = fr.localColTbl;
			} else {
				activeColTbl = globalColTbl;
				if (!fr.transpColFlag) { // Only use background color if there
					bgCol = globalColTbl[bgColIndex]; // is no transparency
				}
			}
			// Handle disposal, prepare current BufferedImage for drawing
			if (prevDisposal <= 1) { // Next frame draws on current one
				setPixels(getPixels(img), prevImg); // Copy current to previous
			} else if (prevDisposal == 2) { // Next frame draws on background
				prevImg = img; // Let previous point to current, dispose current
				img = new BufferedImage(width, height, img.getType());
				final int[] px = getPixels(img);
				Arrays.fill(px, bgCol); // Set background color of new current
			} else if (prevDisposal == 3) { // Next frame draws on previous
				setPixels(getPixels(prevImg), img); // Restore previous image
			}
			// Get pixels from data stream
			int[] pixels = decode(fr, activeColTbl);
			if (fr.interlaceFlag) {
				pixels = deinterlace(pixels, fr); // Rearrange pixel lines
			}
			// Draw pixels on top of current image
			final int w = fr.width, h = fr.height;
			final BufferedImage layer = new BufferedImage(w, h, img.getType());
			setPixels(pixels, layer);
			final Graphics2D g = img.createGraphics();
			g.drawImage(layer, fr.left, fr.top, null);
			g.dispose();
		}

		/**
		 * Returns the background color for the first frame in this GIF image.
		 * If the frame has a local color table, the returned color will be from
		 * this table. If not, the color will be from the global color table. If
		 * there is neither a local nor a global color table, 0 will be
		 * returned.
		 * 
		 * @param index
		 *            Index of the current frame, 0 to N-1
		 * @return 32 bit ARGB color in the form 0xAARRGGBB
		 */
		public final int getBackgroundColor() {
			final GifFrame frame = frames.get(0);
			if (frame.hasLocColTbl) {
				return frame.localColTbl[bgColIndex];
			} else if (hasGlobColTbl) {
				return globalColTbl[bgColIndex];
			}
			return 0;
		}

		/**
		 * If not 0, the delay specifies the number of hundredths (1/100) of a
		 * second to wait before displaying the frame <i>after</i> the current
		 * frame.
		 * 
		 * @param index
		 *            Index of the current frame, 0 to N-1
		 * @return Delay as number of hundredths (1/100) of a second
		 */
		public final int getDelay(final int index) {
			return frames.get(index).delay;
		}

		/**
		 * @param index
		 *            Index of the frame to return as image, starting from 0.
		 *            For indices greater than 0, it may be necessary to draw
		 *            the current frame on top of its previous frames. This
		 *            behavior depends on the disposal method encoded in the
		 *            image. This method's runtime therefore increases with
		 *            higher indices. However, the runtime increase will be
		 *            linear for calls with ascending indices. So don't request
		 *            frame i after requesting frame i+n, as all frames prior to
		 *            i might have to be redrawn. Use call sequences with
		 *            indices from 0 to N-1 instead.
		 * @return A BufferedImage for the specified frame.
		 */
		public final BufferedImage getFrame(final int index) {
			if (img == null || index < prevIndex) { // (Re)Init
				img = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);
				prevImg = new BufferedImage(width, height, img.getType());
				prevIndex = -1;
				prevDisposal = 2;
			}
			// Draw current frame on top of previous frames
			for (int i = prevIndex + 1; i <= index; i++) {
				final GifFrame fr = frames.get(i);
				drawFrame(fr);
				prevIndex = i;
				prevDisposal = fr.disposalMethod;
			}
			return img;
		}

		/**
		 * @return The number of frames contained in this GIF image
		 */
		public final int getFrameCount() {
			return frames.size();
		}

		/**
		 * @return The height of the GIF image
		 */
		public final int getHeight() {
			return height;
		}

		private final int[] getPixels(final BufferedImage img) {
			return ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		}

		/**
		 * @return The width of the GIF image
		 */
		public final int getWidth() {
			return width;
		}

		private final void setPixels(final int[] px, final BufferedImage dest) {
			System.arraycopy(px, 0, getPixels(dest), 0, px.length);
		}
	}

	// If used as a bitmask, the index tells how much lower bits remain.
	// May also be used to compute f(i) = (2^i) - 1.
	static final int[] MASK = new int[] { 0x00000000, 0x00000001, 0x00000003,
			0x00000007, 0x0000000F, 0x0000001F, 0x0000003F, 0x0000007F,
			0x000000FF, 0x000001FF, 0x000003FF, 0x000007FF, 0x00000FFF,
			0x00001FFF, 0x00003FFF, 0x00007FFF, 0x0000FFFF, 0x0001FFFF,
			0x0003FFFF, 0x0007FFFF, 0x000FFFFF, 0x001FFFFF, 0x003FFFFF,
			0x007FFFFF, 0x00FFFFFF, 0x01FFFFFF, 0x03FFFFFF, 0x07FFFFFF,
			0x0FFFFFFF, 0x1FFFFFFF, 0x3FFFFFFF, 0x7FFFFFFF, 0xFFFFFFFF };

	private final int[] decode(final GifFrame fr, final int[] activeColTbl) {
		final CodeTable codes = new CodeTable(fr, activeColTbl);
		final BitReader in = new BitReader(fr.data); // Incoming codes
		final int clearCode = fr.clearCode, endOfInfoCode = fr.endOfInfoCode;
		final int[] out = new int[fr.width * fr.height]; // Outgoing indices
		final int[][] tbl = codes.tbl; // Code table
		int outPos = 0; // Next insert position in the output array
		int currCodeSize = codes.clear(); // Init code table
		in.read(currCodeSize); // Skip leading clear code
		int code = in.read(currCodeSize); // Read first code
		outPos = writeIndices(out, outPos, tbl[code]); // Output 1st code
		while (currCodeSize <= in.bitsAvailable()) {
			final int prevCode = code;
			code = in.read(currCodeSize); // Get next code in code stream
			if (code == clearCode) { // After a CLEAR table, there is
				currCodeSize = codes.clear(); // no previous code, so we need
				code = in.read(currCodeSize); // to read a new previous code
				outPos = writeIndices(out, outPos, tbl[code]); // Write code
				continue; // Now back to the loop, we have a valid previous code
			} else if (code == endOfInfoCode) {
				break;
			}
			final int[] prevVals = tbl[prevCode];
			final int[] prevValsAndK = new int[prevVals.length + 1];
			System.arraycopy(prevVals, 0, prevValsAndK, 0, prevVals.length);
			if (code < codes.nextCode) { // Code table contains code
				outPos = writeIndices(out, outPos, tbl[code]);
				prevValsAndK[prevVals.length] = tbl[code][0]; // K
			} else {
				prevValsAndK[prevVals.length] = prevVals[0]; // K
				outPos = writeIndices(out, outPos, prevValsAndK);
			}
			currCodeSize = codes.add(prevValsAndK); // Add previous indices + K
		}
		return out;
	}

	/**
	 * @param in
	 *            Raw image data as a byte[] array
	 * @return A GifImage object exposing the properties of the GIF image.
	 * @throws ParseException
	 *             If the image violates the GIF specification or is truncated.
	 */
	public final GifImage read(final byte[] in) throws ParseException {
		final GifImage img = new GifImage();
		GifFrame frame = null; // Currently open frame
		int pos = readHeader(in, img); // Read header, get next byte position
		pos = readLogicalScreenDescriptor(img, in, pos);
		if (img.hasGlobColTbl) {
			img.globalColTbl = new int[img.sizeOfGlobColTbl];
			pos = readColTbl(in, img.globalColTbl, pos);
		}
		while (pos < in.length) {
			final int block = in[pos] & 0xFF;
			switch (block) {
			case 0x21: // Extension introducer
				if (pos + 1 >= in.length) {
					throw new ParseException("Unexpected end.", pos);
				}
				switch (in[pos + 1] & 0xFF) {
				case 0xFE: // Comment extension
					pos = readTextExtension(in, pos);
					break;
				case 0xFF: // Application extension
					pos = readAppExt(img, in, pos);
					break;
				case 0x01: // Plain text extension
					frame = null; // End of current frame
					pos = readTextExtension(in, pos);
					break;
				case 0xF9: // Graphic control extension
					if (frame == null) {
						frame = new GifFrame();
						img.frames.add(frame);
					}
					pos = readGraphicControlExt(frame, in, pos);
					break;
				default:
					throw new ParseException("Unkonwn extension.", pos);
				}
				break;
			case 0x2C: // Image descriptor
				if (frame == null) {
					frame = new GifFrame();
					img.frames.add(frame);
				}
				pos = readImgDescr(frame, in, pos);
				if (frame.hasLocColTbl) {
					frame.localColTbl = new int[frame.sizeOfLocColTbl];
					pos = readColTbl(in, frame.localColTbl, pos);
				}
				pos = readImgData(frame, in, pos);
				frame = null; // End of current frame
				break;
			case 0x3B: // GIF Trailer
				return img; // Found trailer, finished reading.
			default:
				throw new ParseException("Unknown block.", pos);
			}
		}
		return img;
	}

	/**
	 * @param is
	 *            Image data as input stream. This method will read from the
	 *            input stream's current position. It will not reset the
	 *            position before reading and won't reset or close the stream
	 *            afterwards. Call these methods before and after calling this
	 *            method as needed.
	 * @return A GifImage object exposing the properties of the GIF image.
	 * @throws ParseException
	 *             If the image violates the GIF specification or is truncated.
	 * @throws IOException
	 */
	public final GifImage read(final InputStream is) throws ParseException,
			IOException {
		final int numBytes = is.available();
		final byte[] data = new byte[numBytes];
		is.read(data, 0, numBytes);
		return read(data);
	}

	/**
	 * @param ext
	 *            Empty application extension object
	 * @param in
	 *            Raw data
	 * @param i
	 *            Index of the first byte of the application extension
	 * @return Index of the first byte after this extension
	 */
	private final int readAppExt(final GifImage img, final byte[] in, int i) {
		img.appId = new String(in, i + 3, 8); // should be "NETSCAPE"
		img.appAuthCode = new String(in, i + 11, 3); // should be "2.0"
		i += 14; // Go to sub-block size, it's value should be 3
		final int subBlockSize = in[i] & 0xFF;
		// The only app extension widely used is NETSCAPE, it's got 3 data bytes
		if (subBlockSize == 3) {
			// in[i+1] should have value 01, in[i+5] should be block terminator
			img.repetitions = in[i + 2] & 0xFF | in[i + 3] & 0xFF << 8; // Short
			return i + 5;
		} // Skip unknown application extensions
		while ((in[i] & 0xFF) != 0) { // While sub-block size != 0
			i += (in[i] & 0xFF) + 1; // Skip to next sub-block
		}
		return i + 1;
	}

	/**
	 * @param in
	 *            Raw data
	 * @param colors
	 *            Pre-initialized target array to store ARGB colors
	 * @param i
	 *            Index of the color table's first byte
	 * @return Index of the first byte after the color table
	 */
	private final int readColTbl(final byte[] in, final int[] colors, int i) {
		final int numColors = colors.length;
		for (int c = 0; c < numColors; c++) {
			final int a = 0xFF; // Alpha 255 (opaque)
			final int r = in[i++] & 0xFF; // 1st byte is red
			final int g = in[i++] & 0xFF; // 2nd byte is green
			final int b = in[i++] & 0xFF; // 3rd byte is blue
			colors[c] = ((a << 8 | r) << 8 | g) << 8 | b;
		}
		return i;
	}

	/**
	 * @param ext
	 *            Graphic control extension object
	 * @param in
	 *            Raw data
	 * @param i
	 *            Index of the extension introducer
	 * @return Index of the first byte after this block
	 */
	private final int readGraphicControlExt(final GifFrame fr, final byte[] in,
			final int i) {
		fr.disposalMethod = (in[i + 3] & 0b00011100) >>> 2; // Bits 4-2
		fr.transpColFlag = (in[i + 3] & 1) == 1; // Bit 0
		fr.delay = in[i + 4] & 0xFF | (in[i + 5] & 0xFF) << 8; // 16 bit LSB
		fr.transpColIndex = in[i + 6] & 0xFF; // Byte 6
		return i + 8; // Skipped byte 7 (blockTerminator), as it's always 0x00
	}

	/**
	 * @param in
	 *            Raw data
	 * @param img
	 *            The GifImage object that is currently read
	 * @return Index of the first byte after this block
	 * @throws ParseException
	 *             If the GIF header/trailer is missing, incomplete or unknown
	 */
	private final int readHeader(final byte[] in, final GifImage img)
			throws ParseException {
		if (in.length < 6) { // Check first 6 bytes
			throw new ParseException("Image is truncated.", 0);
		}
		// Some GIFs don't have a trailer byte. That sucks.
		// if (in[in.length - 1] != 0x3B) { // Check last byte
		// throw new ParseException("Missing GIF trailer.", in.length - 1);
		// }
		img.header = new String(in, 0, 6);
		if (!img.header.equals("GIF87a") && !img.header.equals("GIF89a")) {
			throw new ParseException("Invalid GIF header.", 0);
		}
		return 6;
	}

	/**
	 * @param fr
	 *            The GIF frame to whom this image descriptor belongs
	 * @param in
	 *            Raw data
	 * @param i
	 *            Index of the first byte of this block, i.e. the minCodeSize
	 * @return
	 */
	private final int readImgData(final GifFrame fr, final byte[] in, int i) {
		final int fileSize = in.length;
		fr.minCodeSize = in[i++] & 0xFF; // Read code size, go to block size
		int j = i, imgDataSize = 0, imgDataPos = 0;
		int subBlockSize = in[j++] & 0xFF; // Read first sub-block size
		while (j < fileSize && subBlockSize != 0) { // While block has data
			imgDataSize += subBlockSize; // Add block size to sum of block sizes
			j += subBlockSize; // Go to next block size
			subBlockSize = in[j++] & 0xFF; // Read next block size
		}
		fr.data = new byte[imgDataSize + 2]; // Will hold the LZW encoded data
		final byte[] data = fr.data;
		subBlockSize = in[i++] & 0xFF; // Read first sub-block size
		while (i < fileSize && subBlockSize != 0) { // While sub-block has data
			System.arraycopy(in, i, data, imgDataPos, subBlockSize); // Copy
			imgDataPos += subBlockSize; // Move output data position
			i += subBlockSize; // Go to next sub-block size
			subBlockSize = in[i++] & 0xFF; // Read next sub-block size
		}
		fr.firstCodeSize = fr.minCodeSize + 1; // Add 1 bit for CLEAR and EOI
		fr.clearCode = 1 << fr.minCodeSize; // CLEAR = 2^minCodeSize
		fr.endOfInfoCode = fr.clearCode + 1; // EOI
		return i;
	}

	/**
	 * @param fr
	 *            The GIF frame to whom this image descriptor belongs
	 * @param in
	 *            Raw data
	 * @param i
	 *            Index of the image separator, i.e. the first block byte
	 * @return Index of the first byte after this block
	 */
	private final int readImgDescr(final GifFrame fr, final byte[] in, int i) {
		fr.left = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 1-2
		fr.top = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 3-4
		fr.width = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 5-6
		fr.height = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 7-8
		final byte b = in[++i]; // Byte 9 is a packed byte
		fr.hasLocColTbl = (b & 0b10000000) >>> 7 == 1; // Bit 7
		fr.interlaceFlag = (b & 0b01000000) >>> 6 == 1; // Bit 6
		fr.sortFlag = (b & 0b00100000) >>> 5 == 1; // Bit 5
		final int colTblSizePower = (b & 7) + 1; // Bits 2-0
		fr.sizeOfLocColTbl = 1 << colTblSizePower; // 2^(N+1), As per the spec
		return ++i;
	}

	/**
	 * @param img
	 * @param i
	 *            Start index of this block.
	 * @return Index of the first byte after this block.
	 */
	private final int readLogicalScreenDescriptor(final GifImage img,
			final byte[] in, final int i) {
		img.width = in[i] & 0xFF | (in[i + 1] & 0xFF) << 8; // 16 bit, LSB 1st
		img.height = in[i + 2] & 0xFF | (in[i + 3] & 0xFF) << 8; // 16 bit
		final byte b = in[i + 4]; // Byte 4 is a packed byte
		img.hasGlobColTbl = (b & 0b10000000) >>> 7 == 1; // Bit 7
		final int colResPower = ((b & 0b01110000) >>> 4) + 1; // Bits 6-4
		img.colorResolution = 1 << colResPower; // 2^(N+1), As per the spec
		img.sortFlag = (b & 0b00001000) >>> 3 == 1; // Bit 3
		final int globColTblSizePower = (b & 7) + 1; // Bits 0-2
		img.sizeOfGlobColTbl = 1 << globColTblSizePower; // 2^(N+1), see spec
		img.bgColIndex = in[i + 5] & 0xFF; // 1 Byte
		img.pxAspectRatio = in[i + 6] & 0xFF; // 1 Byte
		return i + 7;
	}

	/**
	 * @param in
	 *            Raw data
	 * @param pos
	 *            Index of the extension introducer
	 * @return Index of the first byte after this block
	 */
	private final int readTextExtension(final byte[] in, final int pos) {
		int i = pos + 2; // Skip extension introducer and label
		int subBlockSize = in[i++] & 0xFF;
		while (subBlockSize != 0 && i < in.length) {
			i += subBlockSize;
			subBlockSize = in[i++] & 0xFF;
		}
		return i;
	}

	private final int writeIndices(final int[] out, final int start,
			final int[] indices) {
		System.arraycopy(indices, 0, out, start, indices.length);
		return start + indices.length;
	}
}