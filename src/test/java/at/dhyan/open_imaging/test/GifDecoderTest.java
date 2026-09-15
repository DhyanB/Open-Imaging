package at.dhyan.open_imaging.test;

import static at.dhyan.open_imaging.test.TestImage.OUT_FOLDER;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

abstract class GifDecoderTest {

    static Map<String, TestImage> IMAGES;
    static Map<String, TestImage> IMAGES_SUBSET;

    abstract BufferedImage[] readImageFrames(TestImage img);

    @BeforeAll
    public static void setUp() {
        IMAGES = TestImageReader.getAllTestImages();
        IMAGES_SUBSET =
                TestImageReader.getSubsetOfTestImages(
                        Arrays.asList("dance", "eat_book", "just_do_it"));
    }

    @Test
    public void benchmark() {
        int warmups = Integer.parseInt(System.getProperty("warmups", "1"));
        int runs = Integer.parseInt(System.getProperty("runs", "1"));

        // Warmup runs
        measurePerformanceInMs(IMAGES.values(), warmups);

        // Regular runs
        final double runtimeMs = measurePerformanceInMs(IMAGES.values(), runs);

        // Print results
        System.out.println("Benchmark results from " + this.getClass().getSimpleName() + ":");
        System.out.println("    Image files: " + IMAGES.size());
        System.out.println("    Warmups: " + warmups);
        System.out.println("    Runs: " + runs);
        System.out.println("    Average runtime (ms): " + Math.round(runtimeMs / runs));
        assertTrue(true);
    }

    double measurePerformanceInMs(Collection<TestImage> images, int loops) {
        final long start = System.nanoTime();
        for (int n = 0; n < loops; n++) {
            images.forEach(this::readImageFrames);
        }
        return (System.nanoTime() - start) / 1000000.;
    }

    @Test
    public void writeFramesOfAllImagesToDisk() throws IOException {
        writeFramesOfImagesToDisk(IMAGES.values());
    }

    @Test
    public void writeFramesOfSubsetOfImagesToDisk() throws IOException {
        writeFramesOfImagesToDisk(IMAGES_SUBSET.values());
    }

    void writeFramesOfImagesToDisk(Collection<TestImage> images) throws IOException {
        for (TestImage img : images) {
            writeFramesOfImageToDisk(img);
        }
        System.out.println("Wrote frames of " + images.size() + " image files to " + OUT_FOLDER);
    }

    void writeFramesOfImageToDisk(TestImage img) throws IOException {
        img.writeFramesToDisk(readImageFrames(img));
    }
}
