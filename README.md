Open Imaging
============

Welcome to Open Imaging. Currently, this project only contains a GIF decoder. At a later point, other tools and
libraries that deal with the creation and processing of images may be added.

## GIF Decoder

A decoder capable of processing a GIF data stream to render the graphics contained in it. This implementation follows
the official <A HREF="http://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF specification</A>.

### Example Usage
```java
void example(final byte[] data) throws Exception {
	final GifImage gif = GifDecoder.read(data);
	final int width = gif.getWidth();
	final int height = gif.getHeight();
	final int background = gif.getBackgroundColor();
	final int frameCount = gif.getFrameCount();
	for (int i = 0; i < frameCount; i++) {
		final BufferedImage img = gif.getFrame(i);
		final int delay = gif.getDelay(i);
		ImageIO.write(img, "png", new File(OUT_FOLDER + "frame_" + i + ".png"));
	}
}
```

You can also read from an input stream, though it will be converted to a byte array internally:

```java
	final FileInputStream data = new FileInputStream(IN_FOLDER + "some.gif");
	final GifImage gif = GifDecoder.read(data);
```

### Compatibility

* Support for GIF87a, GIF89a, animation, transparency and interlacing.
* Independent of third party libraries. Just download `GifDecoder.java` and the `LICENSE` file.
* Some GIF images cause an 
  <a href="http://stackoverflow.com/questions/22259714/arrayindexoutofboundsexception-4096-while-reading-gif-file">ArrayIndexOutOfBoundsException: 4096</a>
  when using Java's official `ImageIO.read` method or the decoder used in Apache Imaging.
  <a href="http://www.fmsware.com/stuff/gif.html">Kevin Weiner's decoder</a> will either throw the same exception or
  render the frames of these images incorrectly. This decoder does not suffer from this bug.
* Requires Java 8.
* Should support Java 11 and Java 17 (untested).

### Performance

This decoder has been frequently benchmarked against Kevin Weiner's decoder, which is well crafted and
delivers high performance. Recent results indicate that both decoders perform on the same level:

	Benchmark results from GifDecoderOpenImagingTest.benchmark()
        Image files: 33
        Warmups: 10
        Runs: 100
        Average runtime (ms): 1174

	Benchmark results from GifDecoderKevinWeinerTest.benchmark()
        Image files: 33
        Warmups: 10
        Runs: 100
        Average runtime (ms): 1095

However, performance heavily depends on the set of images used for testing and the main motivation
behind this decoder was correctness rather than speed.

Feel free to run your own tests (see next section), any feedback is highly appreciated.

### Running the tests and benchmarks

You'll need a JDK, `gradle` and `make`. Run `make` to list available commands. You'll see something like this:

    Usage: make [target]

    Targets:
    help               Show this help message.
    b                  Build.
    t                  Run all tests with default parameters.
    cb                 Clean and build.
    cbt                Clean, build and test.
    bench              Benchmark using 1 warmup and 1 run.
    bench w=i r=j      Benchmark using i warmups and j runs.
    bench-kw           Benchmark Kevin Weiner's GifDecoder using 1 warmup and 1 run.
    bench-kw w=i r=j   Benchmark Kevin Weiner's GifDecoder using i warmups and j runs.

One of the tests run by `make t` loops through all test images and decodes and writes their individual frames
to `src/test/resources/output-frames/`. This is a test I frequently run after changing the code to ensure correctness.

### Test data

The test data (see `/src/test/resources/input-images/`) consists of more than 30 different GIF images featuring:

- File sizes ranging from 69 bytes (`sample.gif`) up to 5 MB (`space.gif`)
- Around 1.400 individual frames (~30 MB)
- Different image dimensions
- Animated GIFs
- Static GIfs
- GIFs with transparent backgrounds
- GIFs that have optimized frames with smaller dimensions than the base canvas
- GIFs with a high frame-count (255 frames in `bubble.gif`)
- GIFs that use interlacing (e.g. `hand.gif`)
- GIFs that cause the mentioned `ArrayOutOfBoundsException` in various other decoders
- `fish.gif`, which has no trailer byte
- `c64.gif`, which has a truncated `end of information` code at the end of the second frame
- `train.gif`, which has a truncated image data sub-block at the end of the last frame
- `science.gif`, which has about 5% worth of corrupted, unrecoverable trailing data

### Quirks

<b>Background color:</b> If a GIF frame requires the next frame to be drawn on the background, a decoder would have to clear the canvas and restore the background color that is specified in the GIF's logical screen descriptor. Many GIFs that look like they should actually have a transparent background would then have an opaque background. Therefore this decoder only sets the canvas to the background color, if the next frame has no transparent color defined. Otherwise, a transparent background will be used to draw upon. Testing indicates that this approach works fine. However, you can still ask the decoder for the background color of the first frame and use it to set the background of buffered images on your own.

### Additional resources

- https://www.w3.org/Graphics/GIF/spec-gif89a.txt
- http://www.theimage.com/animation/pages/disposal.html
- https://docstore.mik.ua/orelly/web2/wdesign/ch23_05.htm#wdnut2-CHP-23-FIG-1
