package at.dhyan.open_imaging.test;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

public class GifDecoderTest {

	static byte[][] data;
	static ByteArrayInputStream[] ins;
	static final String OUT_FOLDER = "src/test/resources/output-frames/";
	static final String IN_FOLDER = "src/test/resources/input-images/";
	static final String[] ALL = new String[] { "sampletrans", "sample", "sign",
			"smile", "cat", "steps", "dance", "stickman", "chicken", "mario",
			"comic", "hands", "prom", "cradle", "hand", "run", "geo1", "cats",
			"dancing", "geo2", "fish", "bubble", "space" };
	static final String[] SINGLE = { "geo2" };

	// Makes test variations easier
	static final int LOOPS = 20;
	static final String[] FILENAMES = SINGLE; // SINGLE | ALL

	public static byte[] readFile(final String pathname)
			throws FileNotFoundException, IOException {
		final File file = new File(pathname);
		final byte[] data = new byte[(int) file.length()];
		try (FileInputStream fis = new FileInputStream(file)) {
			fis.read(data);
			fis.close();
		}
		return data;
	}

	public static void writeFile(final BufferedImage img, final int index)
			throws IOException {
		ImageIO.write(img, "png", new File(OUT_FOLDER + "frame_" + index
				+ ".png"));
	}

	@Test
	public void testDecodeAndSaveFrames() throws Exception {
		try {
			final byte[] data = readFile(IN_FOLDER + SINGLE[0] + ".gif");
			final at.dhyan.open_imaging.GifDecoder decoder = new at.dhyan.open_imaging.GifDecoder();
			final at.dhyan.open_imaging.GifDecoder.GifImage gif = decoder
					.read(data);
			final int width = gif.getWidth();
			final int height = gif.getHeight();
			final int background = gif.getBackgroundColor();
			final int frameCount = gif.getFrameCount();
			for (int i = 0; i < frameCount; i++) {
				final BufferedImage img = gif.getFrame(i);
				writeFile(img, i);
			}
			assertEquals("width", 720, width);
			assertEquals("height", 720, height);
			assertEquals("frames", 45, frameCount);
			assertEquals("background", -16777216, background);
		} catch (final Exception e) {
			assertEquals(true, false);
		}
	}

	@Test
	public void testDecodeAndSaveFramesFromInputStream() throws Exception {
		try {
			final FileInputStream data = new FileInputStream(IN_FOLDER
					+ SINGLE[0] + ".gif");
			final at.dhyan.open_imaging.GifDecoder decoder = new at.dhyan.open_imaging.GifDecoder();
			final at.dhyan.open_imaging.GifDecoder.GifImage gif = decoder
					.read(data);
			final int width = gif.getWidth();
			final int height = gif.getHeight();
			final int background = gif.getBackgroundColor();
			final int frameCount = gif.getFrameCount();
			for (int i = 0; i < frameCount; i++) {
				final BufferedImage img = gif.getFrame(i);
				writeFile(img, i);
			}
			assertEquals("width", 720, width);
			assertEquals("height", 720, height);
			assertEquals("frames", 45, frameCount);
			assertEquals("background", -16777216, background);
		} catch (final Exception e) {
			assertEquals(true, false);
		}
	}

	@Test
	public void testKevinWeinerDecoder() {
		try {
			data = new byte[FILENAMES.length][];
			ins = new ByteArrayInputStream[FILENAMES.length];
			for (int i = 0; i < FILENAMES.length; i++) {
				data[i] = readFile(IN_FOLDER + FILENAMES[i] + ".gif");
				ins[i] = new ByteArrayInputStream(data[i]);
				ins[i].reset();
			}
			final long start = System.nanoTime();
			for (int n = 0; n < LOOPS; n++) {
				for (int f = 0; f < FILENAMES.length; f++) {
					final ByteArrayInputStream in = ins[f];
					in.reset();
					final com.fmsware.GifDecoder decoder = new com.fmsware.GifDecoder();
					decoder.read(in);
					final int frameCount = decoder.getFrameCount();
					for (int i = 0; i < frameCount; i++) {
						decoder.getFrame(i);
					}
				}
			}
			final long runtime = (System.nanoTime() - start) / 1000000;
			final long avg = Math.round(runtime / LOOPS);
			System.out.println("RESULTS FOR KEVIN WEINER DECODER");
			System.out.println("Files: " + FILENAMES.length);
			System.out.println("Repetitions: " + LOOPS);
			System.out.println("Runtime: " + runtime + " ms");
			System.out.println("Time per repetition: " + avg + " ms");
			assertEquals(true, true);
		} catch (final Exception e) {
			assertEquals(true, false);
		}

	}

	@Test
	public void testOpenImagingDecoder() throws Exception {
		try {
			data = new byte[FILENAMES.length][];
			for (int i = 0; i < FILENAMES.length; i++) {
				data[i] = readFile(IN_FOLDER + FILENAMES[i] + ".gif");
			}
			final long start = System.nanoTime();
			for (int n = 0; n < LOOPS; n++) {
				for (int f = 0; f < FILENAMES.length; f++) {
					final at.dhyan.open_imaging.GifDecoder decoder = new at.dhyan.open_imaging.GifDecoder();
					final at.dhyan.open_imaging.GifDecoder.GifImage gifImage = decoder
							.read(data[f]);
					final int frameCount = gifImage.getFrameCount();
					for (int i = 0; i < frameCount; i++) {
						gifImage.getFrame(i);
					}
				}
			}
			final long runtime = (System.nanoTime() - start) / 1000000;
			final long avg = Math.round(runtime / LOOPS);
			System.out.println("RESULTS FOR OPEN IMAGING DECODER");
			System.out.println("Files: " + FILENAMES.length);
			System.out.println("Repetitions: " + LOOPS);
			System.out.println("Total time: " + runtime + " ms");
			System.out.println("Time per repetition: " + avg + " ms");
			assertEquals(true, true);
		} catch (final Exception e) {
			e.printStackTrace();
			assertEquals(true, false);
		}
	}
}
