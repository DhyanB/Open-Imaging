package at.dhyan.open_imaging.test;

import at.dhyan.open_imaging.GifDecoder;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GifDecoderFrameHashManifestTest {
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
        final InputStream stream = GifDecoderFrameHashManifestTest.class.getResourceAsStream("/frame-hashes.sha256");
        if (stream == null) {
            throw new IOException("Missing frame hash manifest.");
        }
        final Map<String, String> hashes = new LinkedHashMap<String, String>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final int separator = line.indexOf('=');
                if (separator <= 0 || separator == line.length() - 1 || hashes.put(line.substring(0, separator),
                        line.substring(separator + 1)) != null) {
                    throw new IOException("Invalid frame hash manifest entry: " + line);
                }
            }
        }
        return hashes;
    }
}
