package at.dhyan.open_imaging.test;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import org.junit.Test;

import at.dhyan.open_imaging.GifDecoder;
import at.dhyan.open_imaging.GifDecoder.GifImage;

public class GifDecoderTest {

	static ByteArrayInputStream[] ins;
	static final String OUT_FOLDER = "src/test/resources/output-frames/";
	static final String IN_FOLDER = "src/test/resources/input-images/";
	static Map<String, TestImage> IMAGES;
	static Map<String, TestImage> IMAGES_SUBSET;
	static int WARMUP_LOOPS = 10;
	static int LOOPS = 10;

	@BeforeClass
	public static void setUp() throws IOException {
		IMAGES = TestImageReader.getAllTestImages();
		IMAGES_SUBSET = TestImageReader.getSubsetOfTestImages();
	}

	@Test
	public void testMetadata() throws Exception {
		try {
			for (String fileName : IMAGES.keySet()) {
				TestImage testImg = IMAGES.get(fileName);
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
			// Warm up
			for (int n = 0; n < WARMUP_LOOPS; n++) {
				for (String fileName : IMAGES.keySet()) {
					TestImage testImg = IMAGES.get(fileName);
					testImg.stream.reset();
					final com.fmsware.GifDecoder decoder = new com.fmsware.GifDecoder();
					decoder.read(testImg.stream);
					final int frameCount = decoder.getFrameCount();
					for (int i = 0; i < frameCount; i++) {
						decoder.getFrame(i);
					}
				}
			}

			// Actual performance test
			final long start = System.nanoTime();
			for (int n = 0; n < LOOPS; n++) {
				for (String fileName : IMAGES.keySet()) {
					TestImage testImg = IMAGES.get(fileName);
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

			// Output results
			final long avg = Math.round(runtime / LOOPS);
			System.out.println("RESULTS FOR KEVIN WEINER DECODER");
			System.out.println("Files: " + IMAGES.size());
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
			for (int n = 0; n < WARMUP_LOOPS; n++) {
				for (String fileName : IMAGES.keySet()) {
					TestImage testImg = IMAGES.get(fileName);
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
				for (String fileName : IMAGES.keySet()) {
					TestImage testImg = IMAGES.get(fileName);
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
			System.out.println("Files: " + IMAGES.size());
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
	public void testWriteFramesAllImagesKevinWeinerDecoder() throws Exception {
		for (String fileName : IMAGES.keySet()) {
			TestImage testImg = IMAGES.get(fileName);
			writeGifImageKevinWeinerDecoder(testImg);
		}
	}

	@Test
	public void testWriteFramesAllImagesOpenImagingDecoder() throws Exception {
		for (String fileName : IMAGES.keySet()) {
			TestImage testImg = IMAGES.get(fileName);
			writeGifImageOpenImagingDecoder(testImg);
		}
	}

	@Test
	public void testWriteFramesSubsetImageKevinWeinerDecoder() throws Exception {
		for (String fileName : IMAGES.keySet()) {
			TestImage testImg = IMAGES.get(fileName);
			writeGifImageKevinWeinerDecoder(testImg);
		}
	}

	@Test
	public void testWriteFramesSubsetImageOpenImagingDecoder() throws Exception {
		for (String fileName : IMAGES.keySet()) {
			TestImage testImg = IMAGES.get(fileName);
			writeGifImageOpenImagingDecoder(testImg);
		}
	}

	private void writeGifImageKevinWeinerDecoder(final TestImage testImg) {
		try {
			testImg.stream.reset();
			final com.fmsware.GifDecoder decoder = new com.fmsware.GifDecoder();
			decoder.read(testImg.stream);
			final int frameCount = decoder.getFrameCount();
			for (int i = 0; i < frameCount; i++) {
				final BufferedImage img = decoder.getFrame(i);
				ImageIO.write(img, "png", new File(OUT_FOLDER + testImg.name + "_" + i + ".png"));
			}
		} catch (final Exception e) {
			e.printStackTrace();
			assertEquals(true, false);
		}
	}

	private void writeGifImageOpenImagingDecoder(final TestImage testImg) {
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
