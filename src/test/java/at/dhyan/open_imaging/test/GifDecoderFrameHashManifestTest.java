package at.dhyan.open_imaging.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.dhyan.open_imaging.GifDecoder;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GifDecoderFrameHashManifestTest {
    @Test
    void cachesFramesAcrossRandomAccess() throws IOException {
        final Map<String, String> expectedHashes = readExpectedHashes();
        final TestImage image = TestImageReader.getAllTestImages().get("dance");
        final GifDecoder.GifImage gif = GifDecoder.read(image.data);

        final BufferedImage laterFrame = gif.getFrame(5);
        final BufferedImage earlierFrame = gif.getFrame(1);

        assertEquals(expectedHashes.get("dance.gif/5"), FrameHash.sha256(laterFrame));
        assertEquals(expectedHashes.get("dance.gif/1"), FrameHash.sha256(earlierFrame));
        assertSame(laterFrame, gif.getFrame(5));
        assertSame(earlierFrame, gif.getFrame(1));
    }

    @Test
    void matchesApprovedFrameHashes() throws IOException {
        final Map<String, String> expectedHashes = readExpectedHashes();
        for (String imageName : FrameHashGenerator.IMAGE_NAMES) {
            final TestImage image = TestImageReader.getAllTestImages().get(imageName);
            final GifDecoder.GifImage gif = GifDecoder.read(image.data);
            for (int frameIndex = 0; frameIndex < gif.getFrameCount(); frameIndex++) {
                final String key = imageName + ".gif/" + frameIndex;
                final String expectedHash = expectedHashes.remove(key);
                assertNotNull(expectedHash, "Missing hash for " + key);
                final BufferedImage frame = gif.getFrame(frameIndex);
                assertEquals(expectedHash, FrameHash.sha256(frame), key);
            }
        }
        assertTrue(expectedHashes.isEmpty(), "Unexpected hashes: " + expectedHashes.keySet());
    }

    private static Map<String, String> readExpectedHashes() throws IOException {
        final InputStream stream =
                GifDecoderFrameHashManifestTest.class.getResourceAsStream("/frame-hashes.sha256");
        if (stream == null) {
            throw new IOException("Missing frame hash manifest.");
        }
        final Map<String, String> hashes = new LinkedHashMap<String, String>();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final int separator = line.indexOf('=');
                if (separator <= 0
                        || separator == line.length() - 1
                        || hashes.put(line.substring(0, separator), line.substring(separator + 1))
                                != null) {
                    throw new IOException("Invalid frame hash manifest entry: " + line);
                }
            }
        }
        return hashes;
    }
}
