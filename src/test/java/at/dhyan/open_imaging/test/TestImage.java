package at.dhyan.open_imaging.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestImage {
    public String name;
    public Path path;
    public int width;
    public int height;
    public int frames;

    private byte[] data;
    private ByteArrayInputStream stream;

    public TestImage(final String name, final int width, final int height, final int frames) {
        this.name = name;
        path = Paths.get(GifDecoderTest.IN_FOLDER, name + ".gif");
        this.width = width;
        this.height = height;
        this.frames = frames;
    }

    public byte[] getData() throws IOException {
        if (data == null) {
            data = Files.readAllBytes(path);
        }
        return data;
    }

    public ByteArrayInputStream getStream() throws IOException {
        if (stream == null) {
            stream = new ByteArrayInputStream(getData());
            stream.reset();
        }
        return stream;
    }
}
