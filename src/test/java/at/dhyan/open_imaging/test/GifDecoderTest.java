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
	static TestImage SINGLE;
	static TestImage[] ALL;
	static int LOOPS = 100;

	@BeforeClass
	public static void setUp() throws IOException {
		ALL = new TestImage[] { new TestImage("sampletrans", 10, 10, 1),
				new TestImage("sample", 10, 10, 1),
				new TestImage("sign", 11, 29, 3),
				new TestImage("c64", 360, 248, 2),
				new TestImage("smile", 50, 50, 6),
				new TestImage("cat", 32, 32, 11),
				new TestImage("steps", 550, 400, 5),
				new TestImage("dance", 128, 128, 9),
				new TestImage("stickman", 464, 391, 41),
				new TestImage("chicken", 411, 432, 13),
				new TestImage("mario", 472, 609, 2),
				new TestImage("comic", 760, 261, 1),
				new TestImage("hands", 800, 600, 11),
				new TestImage("prom", 500, 275, 71),
				new TestImage("cradle", 200, 150, 36),
				new TestImage("hand", 400, 400, 49),
				new TestImage("run", 320, 190, 99),
				new TestImage("geo1", 500, 500, 72),
				new TestImage("cats", 512, 269, 63),
				new TestImage("dancing", 640, 360, 57),
				new TestImage("geo2", 720, 720, 45),
				new TestImage("fish", 400, 288, 100),
				new TestImage("train", 240, 166, 175),
				new TestImage("bubble", 395, 256, 255),
				new TestImage("space", 1157, 663, 28) };
		SINGLE = new TestImage("hand", 400, 400, 49);
	}

	@Test
	public void testMetadata() throws Exception {
		try {
			for (final TestImage testImg : ALL) {
				final GifImage gifImage = GifDecoder.read(testImg.data);
				assertEquals("width", testImg.width, gifImage.getWidth());
				assertEquals("height", testImg.height, gifImage.getHeight());
				assertEquals("frames", testImg.frames, gifImage.getFrameCount());
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
				for (final TestImage testImg : ALL) {
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
			System.out.println("Files: " + ALL.length);
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
			final long start = System.nanoTime();
			for (int n = 0; n < LOOPS; n++) {
				for (final TestImage testImg : ALL) {
					// System.out.println(ALL[f].path);
					final GifImage gifImage = GifDecoder.read(testImg.data);
					final int frameCount = gifImage.getFrameCount();
					for (int i = 0; i < frameCount; i++) {
						gifImage.getFrame(i);
					}
				}
			}
			final long runtime = (System.nanoTime() - start) / 1000000;
			final long avg = Math.round(runtime / LOOPS);
			System.out.println("RESULTS FOR OPEN IMAGING DECODER");
			System.out.println("Files: " + ALL.length);
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
	public void testWriteFrames() throws Exception {
		try {
			final GifImage gif = GifDecoder.read(SINGLE.data);
			final int frameCount = gif.getFrameCount();
			for (int i = 0; i < frameCount; i++) {
				final BufferedImage img = gif.getFrame(i);
				ImageIO.write(img, "png", new File(OUT_FOLDER + "frame_" + i
						+ ".png"));
			}
		} catch (final Exception e) {
			e.printStackTrace();
			assertEquals(true, false);
		}
	}
}
