package at.dhyan.open_imaging.test;

import at.dhyan.open_imaging.GifDecoder;
import at.dhyan.open_imaging.GifDecoder.GifImage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GifDecoderOpenImagingTest extends GifDecoderTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("allTestImages")
    public void testForCorrectMetadata(final TestImage img) throws IOException {
        final GifImage gifImage = GifDecoder.read(img.data);
        assertEquals(img.width, gifImage.getWidth(), img.name + ".gif, width");
        assertEquals(img.height, gifImage.getHeight(), img.name + ".gif, height");
        assertEquals(img.frames, gifImage.getFrameCount(), img.name + ".gif, frames");
    }

    private static Stream<TestImage> allTestImages() {
        return TestImageReader.getAllTestImages().values().stream();
    }

    @Override
    BufferedImage[] readImageFrames(TestImage img) {
        try {
            final GifImage gifImage = GifDecoder.read(img.data);
            final int frameCount = gifImage.getFrameCount();
            BufferedImage[] frames = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = gifImage.getFrame(i);
            }
            return frames;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
