package at.dhyan.open_imaging.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.dhyan.open_imaging.GifDecoder;
import at.dhyan.open_imaging.GifDecoder.DecodeLimits;
import at.dhyan.open_imaging.GifDecoder.GifImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class GifDecoderOpenImagingTest extends GifDecoderTest {

    private static final byte[] GIF_HEADER = {'G', 'I', 'F', '8', '9', 'a'};

    @Test
    public void readsInputStreamUntilEof() throws IOException {
        final TestImage image = TestImageReader.getAllTestImages().get("sample");
        final ByteArrayInputStream stream =
                new ByteArrayInputStream(image.data) {
                    @Override
                    public synchronized int available() {
                        return 0;
                    }
                };

        final GifImage gifImage = GifDecoder.read(stream);

        assertEquals(image.width, gifImage.getWidth());
        assertEquals(image.height, gifImage.getHeight());
        assertEquals(image.frames, gifImage.getFrameCount());
    }

    @Test
    public void rejectsNullPublicInputs() {
        assertThrows(NullPointerException.class, () -> GifDecoder.read((byte[]) null));
        assertThrows(NullPointerException.class, () -> GifDecoder.read((java.io.InputStream) null));
    }

    @Test
    public void rejectsTruncatedHeadersAndLogicalScreenDescriptors() {
        final IOException emptyInput =
                assertThrows(IOException.class, () -> GifDecoder.read(new byte[0]));
        assertEquals("GIF header is truncated.", emptyInput.getMessage());

        for (int length = GIF_HEADER.length; length < 13; length++) {
            final byte[] data = new byte[length];
            System.arraycopy(GIF_HEADER, 0, data, 0, GIF_HEADER.length);
            final IOException exception =
                    assertThrows(IOException.class, () -> GifDecoder.read(data));
            assertEquals("GIF logical screen descriptor is truncated.", exception.getMessage());
        }

        final byte[] streamData = new byte[GIF_HEADER.length];
        System.arraycopy(GIF_HEADER, 0, streamData, 0, GIF_HEADER.length);
        final IOException streamInput =
                assertThrows(
                        IOException.class,
                        () -> GifDecoder.read(new ByteArrayInputStream(streamData)));
        assertEquals("GIF logical screen descriptor is truncated.", streamInput.getMessage());
    }

    @Test
    public void rejectsExcessiveLogicalScreenAndFrameDimensions() {
        final IOException logicalScreen =
                assertThrows(
                        IOException.class,
                        () -> GifDecoder.read(gifWithLogicalScreen(65535, 65535)));
        assertEquals(
                "GIF logical screen exceeds the maximum pixel count of 10000000.",
                logicalScreen.getMessage());

        final IOException frame =
                assertThrows(IOException.class, () -> GifDecoder.read(gifWithFrame(65535, 65535)));
        assertEquals("GIF frame exceeds the maximum pixel count of 10000000.", frame.getMessage());
    }

    @Test
    public void appliesConfiguredDecodeLimits() throws IOException {
        final IOException pixels =
                assertThrows(
                        IOException.class,
                        () ->
                                GifDecoder.read(
                                        gifWithLogicalScreen(2, 2), new DecodeLimits(3, 1, 13)));
        assertEquals(
                "GIF logical screen exceeds the maximum pixel count of 3.", pixels.getMessage());

        final TestImage sample = TestImageReader.getAllTestImages().get("sample");
        final DecodeLimits dataLimit = new DecodeLimits(10_000_000, 1_000, sample.data.length - 1);
        final IOException data =
                assertThrows(IOException.class, () -> GifDecoder.read(sample.data, dataLimit));
        assertEquals(
                "GIF data exceeds the maximum encoded data size of "
                        + dataLimit.getMaxEncodedDataBytes()
                        + " bytes.",
                data.getMessage());
        final IOException streamData =
                assertThrows(
                        IOException.class,
                        () -> GifDecoder.read(new ByteArrayInputStream(sample.data), dataLimit));
        assertEquals(data.getMessage(), streamData.getMessage());

        final TestImage dance = TestImageReader.getAllTestImages().get("dance");
        final IOException frames =
                assertThrows(
                        IOException.class,
                        () ->
                                GifDecoder.read(
                                        dance.data,
                                        new DecodeLimits(10_000_000, 1, 64 * 1024 * 1024)));
        assertEquals("GIF exceeds the maximum frame count of 1.", frames.getMessage());
    }

    @Test
    public void decodeLimitsValidateAndExposeTheirValues() {
        final DecodeLimits limits = new DecodeLimits(1, 2, 3);
        assertEquals(1, limits.getMaxPixels());
        assertEquals(2, limits.getMaxFrames());
        assertEquals(3, limits.getMaxEncodedDataBytes());
        assertThrows(IllegalArgumentException.class, () -> new DecodeLimits(0, 1, 1));
    }

    @Test
    public void getBackgroundColorUsesTheFirstFramePalette() throws IOException {
        final GifImage emptyImage = new GifDecoder().new GifImage();
        assertEquals(0, emptyImage.getBackgroundColor());

        final TestImage image = TestImageReader.getAllTestImages().get("sample");
        final GifImage imageWithGlobalColorTable = GifDecoder.read(image.data);
        assertEquals(0xFFFFFFFF, imageWithGlobalColorTable.getBackgroundColor());

        final GifImage imageWithInvalidBackgroundIndex = GifDecoder.read(image.data);
        imageWithInvalidBackgroundIndex.bgColIndex = Integer.MAX_VALUE;
        assertEquals(0, imageWithInvalidBackgroundIndex.getBackgroundColor());

        final GifImage imageWithLocalColorTable = GifDecoder.read(gifWithLocalColorTable());
        assertEquals(0xFFFF0000, imageWithLocalColorTable.getBackgroundColor());
    }

    @Test
    public void exposesFrameDelays() throws IOException {
        final TestImage image = TestImageReader.getAllTestImages().get("sample");
        assertEquals(0, GifDecoder.read(image.data).getDelay(0));
    }

    @Test
    public void readsImageDataWithoutAnInitialClearCode() throws IOException {
        final BufferedImage frame = GifDecoder.read(gifWithoutInitialClearCode()).getFrame(0);
        assertEquals(0xFFFF0000, frame.getRGB(0, 0));
    }

    @Test
    public void restoresThePreviousFrameWhenRequested() throws IOException {
        final TestImage image = TestImageReader.getAllTestImages().get("dispose_prev");
        final BufferedImage thirdFrame = GifDecoder.read(image.data).getFrame(2);

        assertEquals(0xFF000000, thirdFrame.getRGB(50, 50));
    }

    @Test
    public void readsPlainTextExtensionsAndRejectsMalformedBlocks() throws IOException {
        final IOException invalidHeader =
                assertThrows(
                        IOException.class,
                        () -> GifDecoder.read(new byte[] {'N', 'O', 'T', 'G', 'I', 'F'}));
        assertEquals("Invalid GIF header.", invalidHeader.getMessage());

        final IOException unexpectedEnd =
                assertThrows(IOException.class, () -> GifDecoder.read(gifWithSuffix((byte) 0x21)));
        assertEquals("Unexpected end of file.", unexpectedEnd.getMessage());

        final GifImage plainTextExtension =
                GifDecoder.read(gifWithSuffix((byte) 0x21, (byte) 0x01, 0, (byte) 0x3B));
        assertEquals(0, plainTextExtension.getFrameCount());

        final IOException unknownExtension =
                assertThrows(
                        IOException.class,
                        () -> GifDecoder.read(gifWithSuffix((byte) 0x21, (byte) 0x02)));
        assertEquals("Unknown extension at 13", unknownExtension.getMessage());

        final IOException unknownBlock =
                assertThrows(
                        IOException.class,
                        () -> GifDecoder.read(gifWithSuffix(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertEquals("Unknown block at: 13", unknownBlock.getMessage());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allTestImages")
    public void testForCorrectMetadata(final TestImage img) throws IOException {
        final GifImage gifImage = GifDecoder.read(img.data);
        assertEquals(img.width, gifImage.getWidth(), img.name + ".gif, width");
        assertEquals(img.height, gifImage.getHeight(), img.name + ".gif, height");
        assertEquals(img.frames, gifImage.getFrameCount(), img.name + ".gif, frames");
    }

    private static Stream<TestImage> allTestImages() {
        return TestImageReader.getAllTestImages().values().stream();
    }

    private static byte[] gifWithLogicalScreen(final int width, final int height) {
        final byte[] data = new byte[13];
        System.arraycopy(GIF_HEADER, 0, data, 0, GIF_HEADER.length);
        writeLittleEndian(data, 6, width);
        writeLittleEndian(data, 8, height);
        return data;
    }

    private static byte[] gifWithFrame(final int width, final int height) {
        final byte[] data = new byte[23];
        System.arraycopy(GIF_HEADER, 0, data, 0, GIF_HEADER.length);
        writeLittleEndian(data, 6, 1);
        writeLittleEndian(data, 8, 1);
        data[13] = 0x2C;
        writeLittleEndian(data, 18, width);
        writeLittleEndian(data, 20, height);
        return data;
    }

    /** Builds a 1x1 GIF with red at background index zero in its local color table. */
    private static byte[] gifWithLocalColorTable() {
        return gifWithSuffix(
                (byte) 0x2C,
                0,
                0,
                0,
                0,
                1,
                0,
                1,
                0,
                (byte) 0x80,
                (byte) 0xFF,
                0,
                0,
                0,
                0,
                0,
                2,
                0,
                (byte) 0x3B);
    }

    /** Builds a 1x1 GIF with a red pixel followed by end-of-information, without a clear code. */
    private static byte[] gifWithoutInitialClearCode() {
        return gifWithSuffix(
                (byte) 0x2C,
                0,
                0,
                0,
                0,
                1,
                0,
                1,
                0,
                (byte) 0x80,
                (byte) 0xFF,
                0,
                0,
                0,
                0,
                0,
                2,
                1,
                0x28,
                0,
                (byte) 0x3B);
    }

    /** Adds GIF blocks to a minimal 1x1 logical screen. */
    private static byte[] gifWithSuffix(final int... suffix) {
        final byte[] data = new byte[13 + suffix.length];
        System.arraycopy(GIF_HEADER, 0, data, 0, GIF_HEADER.length);
        writeLittleEndian(data, 6, 1);
        writeLittleEndian(data, 8, 1);
        for (int index = 0; index < suffix.length; index++) {
            data[13 + index] = (byte) suffix[index];
        }
        return data;
    }

    private static void writeLittleEndian(final byte[] data, final int index, final int value) {
        data[index] = (byte) value;
        data[index + 1] = (byte) (value >>> 8);
    }

    @Override
    BufferedImage[] readImageFrames(TestImage img) {
        try {
            final GifImage gifImage = GifDecoder.read(img.data);
            final int frameCount = gifImage.getFrameCount();
            BufferedImage[] frames = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = gifImage.getFrame(i);
            }
            return frames;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
