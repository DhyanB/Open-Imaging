package at.dhyan.open_imaging.test;

import at.dhyan.open_imaging.GifDecoder;
import at.dhyan.open_imaging.GifDecoder.GifImage;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import static org.junit.Assert.*;

public class GifDecoderTest {

    static final String OUT_FOLDER = "src/test/resources/output-frames/";
    static final String IN_FOLDER = "src/test/resources/input-images/";
    static Map<String, TestImage> IMAGES;
    static Map<String, TestImage> IMAGES_SUBSET;
    static int WARMUP_LOOPS = 10; // Default: 10
    static int LOOPS = 10; // Default: 10

    @BeforeClass
    public static void setUp() {
        IMAGES = TestImageReader.getAllTestImages();
        IMAGES_SUBSET = TestImageReader.getSubsetOfTestImages();
    }

    @Test
    public void testMetadata() {
        try {
            for (String fileName : IMAGES.keySet()) {
                TestImage testImg = IMAGES.get(fileName);
                final GifImage gifImage = GifDecoder.read(testImg.getData());
                assertEquals(testImg.name + ".gif, width", testImg.width, gifImage.getWidth());
                assertEquals(testImg.name + ".gif, height", testImg.height, gifImage.getHeight());
                assertEquals(testImg.name + ".gif, frames", testImg.frames, gifImage.getFrameCount());
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPerformanceKevinWeinerDecoder() {
        try {
            // Warm up
            for (int n = 0; n < WARMUP_LOOPS; n++) {
                for (String fileName : IMAGES.keySet()) {
                    TestImage testImg = IMAGES.get(fileName);
                    testImg.getStream().reset();
                    final com.fmsware.GifDecoder decoder = new com.fmsware.GifDecoder();
                    decoder.read(testImg.getStream());
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
                    testImg.getStream().reset();
                    final com.fmsware.GifDecoder decoder = new com.fmsware.GifDecoder();
                    decoder.read(testImg.getStream());
                    final int frameCount = decoder.getFrameCount();
                    for (int i = 0; i < frameCount; i++) {
                        decoder.getFrame(i);
                    }
                }
            }
            final double runtimeMs = (System.nanoTime() - start) / 1000000.;

            // Output results
            final long averageMs = Math.round(runtimeMs / LOOPS);
            System.out.println("RESULTS FOR KEVIN WEINER DECODER");
            System.out.println("Files: " + IMAGES.size());
            System.out.println("Repetitions: " + LOOPS);
            System.out.println("Runtime: " + runtimeMs + " ms");
            System.out.println("Time per repetition: " + averageMs + " ms");
            assertTrue(true);
        } catch (final Exception e) {
            fail();
        }

    }

    @Test
    public void testPerformanceOpenImagingDecoder() {
        try {
            // Warm up
            for (int n = 0; n < WARMUP_LOOPS; n++) {
                for (String fileName : IMAGES.keySet()) {
                    TestImage testImg = IMAGES.get(fileName);
                    final GifImage gifImage = GifDecoder.read(testImg.getData());
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
                    final GifImage gifImage = GifDecoder.read(testImg.getData());
                    final int frameCount = gifImage.getFrameCount();
                    for (int i = 0; i < frameCount; i++) {
                        gifImage.getFrame(i);
                    }
                }
            }
            final double runtimeMs = (System.nanoTime() - start) / 1000000.;

            // Output results
            final long averageMs = Math.round(runtimeMs / LOOPS);
            System.out.println("RESULTS FOR OPEN IMAGING DECODER");
            System.out.println("Files: " + IMAGES.size());
            System.out.println("Repetitions: " + LOOPS);
            System.out.println("Total time: " + runtimeMs + " ms");
            System.out.println("Time per repetition: " + averageMs + " ms");
            assertTrue(true);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testWriteFramesAllImagesKevinWeinerDecoder() {
        for (String fileName : IMAGES.keySet()) {
            TestImage testImg = IMAGES.get(fileName);
            writeGifImageKevinWeinerDecoder(testImg);
        }
    }

    @Test
    public void testWriteFramesAllImagesOpenImagingDecoder() {
        for (String fileName : IMAGES.keySet()) {
            TestImage testImg = IMAGES.get(fileName);
            writeGifImageOpenImagingDecoder(testImg);
        }
    }

    @Test
    public void testWriteFramesSubsetImageKevinWeinerDecoder() {
        for (String fileName : IMAGES.keySet()) {
            TestImage testImg = IMAGES.get(fileName);
            writeGifImageKevinWeinerDecoder(testImg);
        }
    }

    @Test
    public void testWriteFramesSubsetImageOpenImagingDecoder() {
        for (String fileName : IMAGES.keySet()) {
            TestImage testImg = IMAGES.get(fileName);
            writeGifImageOpenImagingDecoder(testImg);
        }
    }

    private void writeGifImageKevinWeinerDecoder(final TestImage testImg) {
        try {
            testImg.getStream().reset();
            final com.fmsware.GifDecoder decoder = new com.fmsware.GifDecoder();
            decoder.read(testImg.getStream());
            final int frameCount = decoder.getFrameCount();
            for (int i = 0; i < frameCount; i++) {
                final BufferedImage img = decoder.getFrame(i);
                ImageIO.write(img, "png", new File(OUT_FOLDER + testImg.name + "_" + i + ".png"));
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void writeGifImageOpenImagingDecoder(final TestImage testImg) {
        try {
            final GifImage gif = GifDecoder.read(testImg.getData());
            final int frameCount = gif.getFrameCount();
            for (int i = 0; i < frameCount; i++) {
                final BufferedImage img = gif.getFrame(i);
                ImageIO.write(img, "png", new File(OUT_FOLDER + testImg.name + "_" + i + ".png"));
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
