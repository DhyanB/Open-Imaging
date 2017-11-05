package at.dhyan.open_imaging.test;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import org.junit.Test;

import at.dhyan.open_imaging.GifDecoder;
import at.dhyan.open_imaging.GifDecoder.GifImage;

public class GifDecoderTest {

	static ByteArrayInputStream[] ins;
	static final String OUT_FOLDER = "src/test/resources/output-frames/";
	static final String IN_FOLDER = "src/test/resources/input-images/";
	static TestImage[] IMAGES;
	static TestImage SINGLE_IMAGE;
	static int LOOPS = 10;

	@BeforeClass
	public static void setUp() throws IOException {
		IMAGES = new TestImage[26];
		IMAGES[0] = new TestImage("sampletrans", 10, 10, 1);
		IMAGES[1] = new TestImage("sample", 10, 10, 1);
		IMAGES[2] = new TestImage("sign", 11, 29, 3);
		IMAGES[3] = new TestImage("c64", 360, 248, 2);
		IMAGES[4] = new TestImage("smile", 50, 50, 6);
		IMAGES[5] = new TestImage("cat", 32, 32, 11);
		IMAGES[6] = new TestImage("steps", 550, 400, 5);
		IMAGES[7] = new TestImage("dance", 128, 128, 9);
		IMAGES[8] = new TestImage("stickman", 464, 391, 41);
		IMAGES[9] = new TestImage("chicken", 411, 432, 13);
		IMAGES[10] = new TestImage("mario", 472, 609, 2);
		IMAGES[11] = new TestImage("comic", 760, 261, 1);
		IMAGES[12] = new TestImage("hands", 800, 600, 11);
		IMAGES[13] = new TestImage("prom", 500, 275, 71);
		IMAGES[14] = new TestImage("cradle", 200, 150, 36);
		/*
		 * Frames 0-58 look okay, frame 59 is slightly disturbed. A different
		 * decoder gets 4 more completely broken frames out of it, but even
		 * after looking at the bytes in a hex editor I'm not sure how much
		 * frames it actually contains. There is more data, but no more valid
		 * extensions or image descriptors.
		 */
		IMAGES[15] = new TestImage("science", 307, 265, 60);
		IMAGES[16] = new TestImage("hand", 400, 400, 49);
		IMAGES[17] = new TestImage("run", 320, 190, 99);
		IMAGES[18] = new TestImage("geo1", 500, 500, 72);
		IMAGES[19] = new TestImage("cats", 512, 269, 63);
		IMAGES[20] = new TestImage("dancing", 640, 360, 57);
		IMAGES[21] = new TestImage("geo2", 720, 720, 45);
		IMAGES[22] = new TestImage("fish", 400, 288, 100);
		IMAGES[23] = new TestImage("train", 240, 166, 175);
		IMAGES[24] = new TestImage("bubble", 395, 256, 255);
		IMAGES[25] = new TestImage("space", 1157, 663, 28);
		// IMAGES[26] = new TestImage("eatbook", 240, 240, 13);
		// IMAGES[27] = new TestImage("dispose_none_1", 100, 100, 4);
		// IMAGES[28] = new TestImage("dispose_none_2", 100, 100, 5);
		// IMAGES[29] = new TestImage("dispose_prev", 100, 100, 5);
		// IMAGES[30] = new TestImage("dispose_background_1", 100, 100, 4);
		// IMAGES[31] = new TestImage("dispose_background_2", 100, 100, 5);

		// For single image write test during development
		SINGLE_IMAGE = IMAGES[16];

	}

	@Test
	public void testMetadata() throws Exception {
		try {
			for (final TestImage testImg : IMAGES) {
				final GifImage gifImage = GifDecoder.read(testImg.data);
				assertEquals(testImg.name + ".gif, width", testImg.width, gifImage.getWidth());
				assertEquals(testImg.name + ".gif, height", testImg.height, gifImage.getHeight());
				assertEquals(testImg.name + ".gif, frames", testImg.frames, gifImage.getFrameCount());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			assertEquals(true, false);
		}
	}

	@Test
	public void testPerformanceKevinWeinerDecoder() {
		try {
			final long start = System.nanoTime();
			for (int n = 0; n < LOOPS; n++) {
				for (final TestImage testImg : IMAGES) {
					testImg.stream.reset();
					final com.fmsware.GifDecoder decoder = new com.fmsware.GifDecoder();
					decoder.read(testImg.stream);
					final int frameCount = decoder.getFrameCount();
					for (int i = 0; i < frameCount; i++) {
						decoder.getFrame(i);
					}
				}
			}
			final long runtime = (System.nanoTime() - start) / 1000000;
			final long avg = Math.round(runtime / LOOPS);
			System.out.println("RESULTS FOR KEVIN WEINER DECODER");
			System.out.println("Files: " + IMAGES.length);
			System.out.println("Repetitions: " + LOOPS);
			System.out.println("Runtime: " + runtime + " ms");
			System.out.println("Time per repetition: " + avg + " ms");
			assertEquals(true, true);
		} catch (final Exception e) {
			assertEquals(true, false);
		}

	}

	@Test
	public void testPerformanceOpenImagingDecoder() throws Exception {
		try {
			// Warm up
			for (int n = 0; n < 10; n++) {
				for (final TestImage testImg : IMAGES) {
					final GifImage gifImage = GifDecoder.read(testImg.data);
					final int frameCount = gifImage.getFrameCount();
					for (int i = 0; i < frameCount; i++) {
						gifImage.getFrame(i);
					}
				}
			}

			// Actual performance test
			final long start = System.nanoTime();
			for (int n = 0; n < LOOPS; n++) {
				for (final TestImage testImg : IMAGES) {
					final GifImage gifImage = GifDecoder.read(testImg.data);
					final int frameCount = gifImage.getFrameCount();
					for (int i = 0; i < frameCount; i++) {
						gifImage.getFrame(i);
					}
				}
			}
			final long runtime = (System.nanoTime() - start) / 1000000;

			// Output results
			final long avg = Math.round(runtime / LOOPS);
			System.out.println("RESULTS FOR OPEN IMAGING DECODER");
			System.out.println("Files: " + IMAGES.length);
			System.out.println("Repetitions: " + LOOPS);
			System.out.println("Total time: " + runtime + " ms");
			System.out.println("Time per repetition: " + avg + " ms");
			assertEquals(true, true);
		} catch (final Exception e) {
			e.printStackTrace();
			assertEquals(true, false);
		}
	}

	@Test
	public void testWriteFramesAllImages() throws Exception {
		for (final TestImage testImg : IMAGES) {
			writeGifImage(testImg);
		}
	}

	@Test
	public void testWriteFramesSingleImage() throws Exception {
		writeGifImage(SINGLE_IMAGE);
	}

	private void writeGifImage(final TestImage testImg) {
		try {
			final GifImage gif = GifDecoder.read(testImg.data);
			final int frameCount = gif.getFrameCount();
			for (int i = 0; i < frameCount; i++) {
				final BufferedImage img = gif.getFrame(i);
				ImageIO.write(img, "png", new File(OUT_FOLDER + testImg.name + "_" + i + ".png"));
			}
		} catch (final Exception e) {
			e.printStackTrace();
			assertEquals(true, false);
		}
	}
}
