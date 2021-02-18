import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public class MedianFilterParallel {
    private static class FilterThread extends Thread {
        private final BufferedImage srcImg;
        private final int radius;
        private final int start, finish;
        private final int[] filtered;

        public int[] getFiltered() {
            return filtered;
        }

        public int getStart() {
            return start;
        }

        public FilterThread(BufferedImage srcImg, int startPos, int finish, int radius) {
            this.srcImg = srcImg;
            this.start = startPos;
            this.finish = finish;
            filtered = new int[finish - startPos];
            this.radius = radius;
        }

        @Override
        public void run() {
            for (int k = start; k < finish; k++) {
                int i = k % srcImg.getWidth();
                int j = k / srcImg.getWidth();
                filtered[k - start] = getMedian(srcImg, i, j, radius);
            }
        }
    }

    public static BufferedImage filterImageParallel(BufferedImage srcImg, int radius, int threadCount)
            throws InterruptedException {
        assert (srcImg != null);
        assert (radius > 0);
        assert (threadCount > 0);

        FilterThread[] threads = new FilterThread[threadCount];
        int pixelCount = srcImg.getHeight() * srcImg.getWidth();
        int sliceSize = pixelCount / threadCount;

        for (int i = 0; i < threadCount; i++) {
            int finishPos = (i == threadCount - 1 ? pixelCount : sliceSize * (i + 1));
            threads[i] = new FilterThread(srcImg, sliceSize * i, finishPos, radius);
        }

        for (var thread : threads) {
            thread.start();
        }

        for (var thread : threads) {
            thread.join();
        }

        var filtered = new int[pixelCount];

        for (FilterThread thread : threads) {
            System.arraycopy(thread.getFiltered(), 0, filtered, thread.getStart(), thread.getFiltered().length);
        }

        return createImageFromPixels(filtered, srcImg.getWidth(), srcImg.getHeight(), srcImg.getType());
    }

    private static BufferedImage createImageFromPixels(int[] pixels, int width, int height, int imgType) {
        var newImg = new BufferedImage(width, height, imgType);
        newImg.setRGB(0, 0, width, height, pixels, 0, width);
        return newImg;
    }

    private static int getMedian(BufferedImage srcImg, int x, int y, int radius) {
        var hues = new ArrayList<Float>();
        var saturations = new ArrayList<Float>();
        var brightnesses = new ArrayList<Float>();

        for (int i = -radius; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                if (x + i >= 0 && x + i < srcImg.getWidth() && y + j >= 0 && y + j < srcImg.getHeight()) {
                    var rgb = new Color(srcImg.getRGB(x + i, y + j));
                    var hsb = Color.RGBtoHSB(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), null);
                    hues.add(hsb[0]);
                    saturations.add(hsb[1]);
                    brightnesses.add(hsb[2]);
                }
            }
        }

        hues.sort(Float::compare);
        saturations.sort(Float::compare);
        brightnesses.sort(Float::compare);

        int mid = hues.size() / 2;
        return Color.HSBtoRGB(hues.get(mid), saturations.get(mid), brightnesses.get(mid));
    }

    public static BufferedImage filterImageStream(BufferedImage srcImg, int radius, int threadCount)
            throws ExecutionException, InterruptedException {
        assert (srcImg != null);
        assert (radius > 0);
        assert (threadCount > 0);

        int pixelCount = srcImg.getHeight() * srcImg.getWidth();
        ForkJoinPool filterPool = new ForkJoinPool(threadCount);

        int[] filtered = filterPool
                .submit(() ->
                    IntStream
                        .range(0, pixelCount)
                        .parallel()
                        .map(k -> getMedian(srcImg, k % srcImg.getWidth(), k / srcImg.getWidth(), radius)))
                .get()
                .toArray();

        return createImageFromPixels(filtered, srcImg.getWidth(), srcImg.getHeight(), srcImg.getType());
    }
}