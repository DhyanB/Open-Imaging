package at.dhyan.open_imaging.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import at.dhyan.open_imaging.GifDecoder;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class GifDecoderRegressionTest {
    private static final String[] CAT_RELAXING_FRAME_HASHES = {
        "beb6e81706feef52ad8879e4d88528d69c59e3e39943c93b709ff201348639fe",
        "8b9e66516e793b06bb6abd3062fd1c10f828849ef90d2f0fad63fd48c65a5211",
        "c895aa714ccb755812ff0b315885a331a3f712e55dbb0fa9bae0484bdfd03f7d",
        "1b5b33c4249c4cb55771164b5c87178b659fbd9c89a4ccef39e4ff6dee6aaf79"
    };

    @Test
    void rendersCatRelaxingFramesAfterTheLocalPaletteTransition() throws IOException {
        final TestImage image = TestImageReader.getAllTestImages().get("cat-relaxing");
        final GifDecoder.GifImage gif = GifDecoder.read(image.data);

        for (int frameIndex = 9; frameIndex <= 12; frameIndex++) {
            final BufferedImage frame = gif.getFrame(frameIndex);
            assertEquals(
                    CAT_RELAXING_FRAME_HASHES[frameIndex - 9],
                    FrameHash.sha256(frame),
                    "cat-relaxing.gif/" + frameIndex);
        }
    }
}
