package at.dhyan.open_imaging.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestImage {
    static final String IN_FOLDER = "src/test/resources/input-images/";
    static final String OUT_FOLDER = "src/test/resources/output-frames/";

    public final String name;
    public final Path path;
    public final int width;
    public final int height;
    public final int frames;
    public final byte[] data;
    public final ByteArrayInputStream stream;

    public TestImage(final String name, final int width, final int height, final int frames) throws IOException {
        this.name = name;
        path = Paths.get(IN_FOLDER, name + ".gif");
        this.width = width;
        this.height = height;
        this.frames = frames;
        data = Files.readAllBytes(path);
        stream = new ByteArrayInputStream(data);
        stream.reset();
    }

    public void writeFramesToDisk(BufferedImage[] frames) throws IOException {
        for (int i = 0; i < frames.length; i++) {
            ImageIO.write(frames[i], "png", new File(OUT_FOLDER + name + "_" + i + ".png"));
        }
    }
}
