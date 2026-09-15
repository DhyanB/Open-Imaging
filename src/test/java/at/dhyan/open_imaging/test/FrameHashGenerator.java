package at.dhyan.open_imaging.test;

import at.dhyan.open_imaging.GifDecoder;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FrameHashGenerator {
    static final List<String> IMAGE_NAMES =
            Collections.unmodifiableList(
                    Arrays.asList(
                            "cat",
                            "c64",
                            "dance",
                            "dispose_background_1",
                            "dispose_none_1",
                            "hands",
                            "sample",
                            "sample_trans",
                            "sign",
                            "smile",
                            "steps",
                            "stick_man"));

    private FrameHashGenerator() {}

    public static void main(final String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one output path.");
        }

        final List<String> imageNames = new ArrayList<String>(IMAGE_NAMES);
        Collections.sort(imageNames);
        final StringBuilder hashes = new StringBuilder();
        for (String imageName : imageNames) {
            final TestImage image = TestImageReader.getAllTestImages().get(imageName);
            final GifDecoder.GifImage gif = GifDecoder.read(image.data);
            for (int frameIndex = 0; frameIndex < gif.getFrameCount(); frameIndex++) {
                final BufferedImage frame = gif.getFrame(frameIndex);
                hashes.append(imageName)
                        .append(".gif/")
                        .append(frameIndex)
                        .append('=')
                        .append(FrameHash.sha256(frame))
                        .append('\n');
            }
        }

        final Path output = Paths.get(args[0]);
        Files.createDirectories(output.getParent());
        Files.write(output, hashes.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Wrote " + countLines(hashes) + " frame hashes to " + output);
    }

    private static int countLines(final CharSequence value) {
        int count = 0;
        for (int index = 0; index < value.length(); index++) {
            if (value.charAt(index) == '\n') {
                count++;
            }
        }
        return count;
    }
}
