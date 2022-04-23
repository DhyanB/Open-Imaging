package at.dhyan.open_imaging.test;

import at.dhyan.open_imaging.GifDecoder;
import at.dhyan.open_imaging.GifDecoder.GifImage;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GifDecoderOpenImagingTest extends GifDecoderTest {

    @Test
    public void testForCorrectMetadata() throws IOException {
        for (TestImage img : IMAGES.values()) {
            final GifImage gifImage = GifDecoder.read(img.data);
            assertEquals(img.name + ".gif, width", img.width, gifImage.getWidth());
            assertEquals(img.name + ".gif, height", img.height, gifImage.getHeight());
            assertEquals(img.name + ".gif, frames", img.frames, gifImage.getFrameCount());
        }
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
