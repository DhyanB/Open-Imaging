package at.dhyan.open_imaging.test;

import java.awt.image.BufferedImage;

public class GifDecoderKevinWeinerTest extends GifDecoderTest {

    @Override
    BufferedImage[] readImageFrames(TestImage img) {
        img.stream.reset();
        final com.fmsware.GifDecoder decoder = new com.fmsware.GifDecoder();
        decoder.read(img.stream);
        final int frameCount = decoder.getFrameCount();
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = decoder.getFrame(i);
        }
        return frames;
    }
}
