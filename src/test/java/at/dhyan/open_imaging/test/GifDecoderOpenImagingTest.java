package at.dhyan.open_imaging.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import at.dhyan.open_imaging.GifDecoder;
import at.dhyan.open_imaging.GifDecoder.GifImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class GifDecoderOpenImagingTest extends GifDecoderTest {

    @Test
    public void readsInputStreamUntilEof() throws IOException {
        final TestImage image = TestImageReader.getAllTestImages().get("sample");
        final ByteArrayInputStream stream =
                new ByteArrayInputStream(image.data) {
                    @Override
                    public synchronized int available() {
                        return 0;
                    }
                };

        final GifImage gifImage = GifDecoder.read(stream);

        assertEquals(image.width, gifImage.getWidth());
        assertEquals(image.height, gifImage.getHeight());
        assertEquals(image.frames, gifImage.getFrameCount());
    }

    @Test
    public void getBackgroundColorHandlesMissingFramesAndInvalidPaletteIndexes()
            throws IOException {
        final GifImage emptyImage = new GifDecoder().new GifImage();
        assertEquals(0, emptyImage.getBackgroundColor());

        final TestImage image = TestImageReader.getAllTestImages().get("sample");
        final GifImage imageWithInvalidBackgroundIndex = GifDecoder.read(image.data);
        imageWithInvalidBackgroundIndex.bgColIndex = Integer.MAX_VALUE;
        assertEquals(0, imageWithInvalidBackgroundIndex.getBackgroundColor());
    }

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
