package at.dhyan.open_imaging.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestImageReader {
    private static final Map<String, TestImage> images = new HashMap<>();

    static {
        put("sample_trans", 10, 10, 1);
        put("sample", 10, 10, 1);
        put("sign", 11, 29, 3);
        put("c64", 360, 248, 2);
        put("smile", 50, 50, 6);
        put("cat", 32, 32, 11);
        put("steps", 550, 400, 5);
        put("dance", 128, 128, 9);
        put("stick_man", 464, 391, 41);
        put("chicken", 411, 432, 13);
        put("mario", 472, 609, 2);
        put("comic", 760, 261, 1);
        put("hands", 800, 600, 11);
        put("prom", 500, 275, 71);
        put("cradle", 200, 150, 36);
        /*
         * Frames 0-58 look okay, frame 59 is slightly disturbed. A different
         * decoder gets 4 more completely broken frames out of it, but even
         * after looking at the bytes in a hex editor I'm not sure how many
         * frames it actually contains. There is more data, but no more valid
         * extensions or image descriptors.
         */
        put("science", 307, 265, 60);
        put("hand", 400, 400, 49);
        put("run", 320, 190, 99);
        put("geo1", 500, 500, 72);
        put("cats", 512, 269, 63);
        put("dancing", 640, 360, 57);
        put("geo2", 720, 720, 45);
        put("fish", 400, 288, 100);
        put("train", 240, 166, 175);
        put("bubble", 395, 256, 255);
        put("space", 1157, 663, 28);
        put("eat_book", 240, 240, 13); // Dispose = 2
        put("dispose_none_1", 100, 100, 4);
        put("dispose_none_2", 100, 100, 5); // Buggy
        put("dispose_prev", 100, 100, 5); // Buggy
        put("dispose_background_1", 100, 100, 4);
        put("dispose_background_2", 100, 100, 5); // Buggy
        put("just_do_it", 59, 60, 42);
    }

    private static void put(String fileName, int width, int height, int frames) {
        try {
            images.put(fileName, new TestImage(fileName, width, height, frames));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, TestImage> getAllTestImages() {
        return images;
    }

    public static Map<String, TestImage> getSubsetOfTestImages(List<String> fileNames) {
        Map<String, TestImage> subset = new HashMap<>();
        fileNames.forEach(fileName -> subset.put(fileName, images.get(fileName)));
        return subset;
    }
}
