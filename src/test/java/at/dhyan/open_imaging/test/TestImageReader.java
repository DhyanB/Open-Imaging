package at.dhyan.open_imaging.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestImageReader {
    public static Map<String, TestImage> getAllTestImages() throws IOException {
        Map<String, TestImage> images = new HashMap<>();
        images.put("sample_trans", new TestImage("sample_trans", 10, 10, 1));
        images.put("sample", new TestImage("sample", 10, 10, 1));
        images.put("sign", new TestImage("sign", 11, 29, 3));
        images.put("c64", new TestImage("c64", 360, 248, 2));
        images.put("smile", new TestImage("smile", 50, 50, 6));
        images.put("cat", new TestImage("cat", 32, 32, 11));
        images.put("steps", new TestImage("steps", 550, 400, 5));
        images.put("dance", new TestImage("dance", 128, 128, 9));
        images.put("stick_man", new TestImage("stick_man", 464, 391, 41));
        images.put("chicken", new TestImage("chicken", 411, 432, 13));
        images.put("mario", new TestImage("mario", 472, 609, 2));
        images.put("comic", new TestImage("comic", 760, 261, 1));
        images.put("hands", new TestImage("hands", 800, 600, 11));
        images.put("prom", new TestImage("prom", 500, 275, 71));
        images.put("cradle", new TestImage("cradle", 200, 150, 36));
        /*
         * Frames 0-58 look okay, frame 59 is slightly disturbed. A different
         * decoder gets 4 more completely broken frames out of it, but even
         * after looking at the bytes in a hex editor I'm not sure how many
         * frames it actually contains. There is more data, but no more valid
         * extensions or image descriptors.
         */
        images.put("science", new TestImage("science", 307, 265, 60));
        images.put("hand", new TestImage("hand", 400, 400, 49));
        images.put("run", new TestImage("run", 320, 190, 99));
        images.put("geo1", new TestImage("geo1", 500, 500, 72));
        images.put("cats", new TestImage("cats", 512, 269, 63));
        images.put("dancing", new TestImage("dancing", 640, 360, 57));
        images.put("geo2", new TestImage("geo2", 720, 720, 45));
        images.put("fish", new TestImage("fish", 400, 288, 100));
        images.put("train", new TestImage("train", 240, 166, 175));
        images.put("bubble", new TestImage("bubble", 395, 256, 255));
        images.put("space", new TestImage("space", 1157, 663, 28));
        images.put("eat_book", new TestImage("eat_book", 240, 240, 13)); // Dispose = 2
        images.put("dispose_none_1", new TestImage("dispose_none_1", 100, 100, 4));
        images.put("dispose_none_2", new TestImage("dispose_none_2", 100, 100, 5)); // Buggy
        images.put("dispose_prev", new TestImage("dispose_prev", 100, 100, 5)); // Buggy
        images.put("dispose_background_1", new TestImage("dispose_background_1", 100, 100, 4));
        images.put("dispose_background_2", new TestImage("dispose_background_2", 100, 100, 5)); // Buggy
        images.put("just_do_it", new TestImage("just_do_it", 59, 60, 42));

        return images;
    }

    // For testing relevant subsets during development
    public static Map<String, TestImage> getSubsetOfTestImages() throws IOException {
        Map<String, TestImage> images = getAllTestImages();
        Map<String, TestImage> subset = new HashMap<>();
        subset.put("dance", images.get("dance"));
        subset.put("eat_book", images.get("eat_book"));
        subset.put("just_do_it", images.get("just_do_it"));
        return subset;
    }
}
