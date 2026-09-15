package at.dhyan.open_imaging.test;

import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class FrameHash {
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private FrameHash() {
    }

    static String sha256(final BufferedImage image) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            updateInt(digest, image.getWidth());
            updateInt(digest, image.getHeight());
            final int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            for (int pixel : pixels) {
                updateInt(digest, pixel);
            }
            return toHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static void updateInt(final MessageDigest digest, final int value) {
        digest.update((byte) (value >>> 24));
        digest.update((byte) (value >>> 16));
        digest.update((byte) (value >>> 8));
        digest.update((byte) value);
    }

    private static String toHex(final byte[] bytes) {
        final char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            final int value = bytes[i] & 0xFF;
            chars[i * 2] = HEX[value >>> 4];
            chars[i * 2 + 1] = HEX[value & 0x0F];
        }
        return new String(chars);
    }
}
