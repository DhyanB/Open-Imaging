Open Imaging
============

Currently this project only contains a GIF decoder. At a later point, it may contain other tools and libraries that deal with the creation and processing of images.

## GIF Decoder

A decoder capable of processing a GIF data stream to render the graphics contained in it. This implementation follows the official <A HREF="http://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF specification</A>.

### Example Usage
```java
void example(final byte[] data) throws Exception {
	final GifDecoder decoder = new GifDecoder();
	final GifImage gif = decoder.read(data);
	final int width = gif.getWidth();
	final int height = gif.getHeight();
	final int background = gif.getBackgroundColor();
	final int frameCount = gif.getFrameCount();
	for (int i = 0; i < frameCount; i++) {
		final BufferedImage img = gif.getFrame(i);
		final int delay = gif.getDelay(i);
		ImageIO.write(img, "png", new File(OUTPATH + "frame_" + i + ".png"));
	}
}
```

### Compatibility

Some GIF images cause an <a href="http://stackoverflow.com/questions/22259714/arrayindexoutofboundsexception-4096-while-reading-gif-file">ArrayIndexOutOfBoundsException: 4096</a> when using Java's official `ImageIO.read` method. The decoder used in Apache Imaging and the one from <a href="http://www.fmsware.com/stuff/gif.html">Kevin Weiner</a> also show that behavior.

This decoder does not suffer from this bug.

### Performance

During development, I frequently compared the performance of this decoder with the one from Kevin Weiner, which is very well crafted and shows an impressive performance. I worked hard to deliver <i>comparable speed</i> and current testing indicates that my decoder is around 9% faster than Kevin Weiner's. However, this heavily depends on the set of images used for testing (see next paragraph) and the main reason for creating this GIF decoder was to avoid the bug aforementioned. So I do not persist on being faster, but I think this decoder delivers reasonable performance. Feel free to run your own tests! Any feedback is highly appreciated.

### Images used during testing

My testing set includes 22 different GIF images of various file sizes and image dimensions. The biggest one is about 5 MB, the smallest one is only 69 bytes, all together sum up to 22 MB. All but three are animated GIFs. Some have transparent backgrounds, some have optimized frames with smaller dimensions than the base canvas. One of the bigger files consists of 255 frames. Some images use interlacing. Three images cause an ArrayOutOfBoundsException in various other decoders.

### Issues

<b>Background color:</b> If a GIF frame requires the next frame to be drawn on the background, a decoder would have to clear the canvas and then set it to the background color that is specified in the GIF's logical screen descriptor. Many GIFs that look like they should have a transparent background would then have an opaque background. Therefore this decoder only sets the canvas to the background color, if the next frame has no transparent color defined. Testing indicates that this approach works fine. However, you can still ask the decoder for the background color of the first frame and use it to set the background of BufferedImage objects obtained from the GIF.