Open Imaging
============

Currently this project only contains a GIF decoder. At a later point, it may contain other tools and libraries that deal with the creation and processing of images.

# GIF Decoder

A decoder capable of processing a GIF data stream to render the graphics contained in it. This implementation follows the official <A HREF="http://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF specification</A>.

### Example usage:

```
	static void example(final byte[] data) throws Exception {
		final GifDecoder decoder = new GifDecoder();
		final GifImage gif = decoder.read(data);
		final int width = gif.getWidth();
		final int height = gif.getHeight();
		final int background = gif.getBackgroundColor();
		final int frameCount = gif.getFrameCount();
		for (int i = 0; i < frameCount; i++) {
			final BufferedImage img = gif.getFrame(i);
			final int delay = gif.getDelay(i);
			ImageIO.write(img, "png", new File(OUT + "frame_" + i + ".png"));
		}
	}
```