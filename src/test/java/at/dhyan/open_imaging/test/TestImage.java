package at.dhyan.open_imaging.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestImage {
	public Path path;
	public int width;
	public int height;
	public int frames;
	public byte[] data;
	public ByteArrayInputStream stream;

	public TestImage(final String name, final int width, final int height,
			final int frames) throws IOException {
		path = Paths.get(GifDecoderTest.IN_FOLDER, name + ".gif");
		this.width = width;
		this.height = height;
		this.frames = frames;
		data = Files.readAllBytes(path);
		stream = new ByteArrayInputStream(data);
		stream.reset();
	}
}
