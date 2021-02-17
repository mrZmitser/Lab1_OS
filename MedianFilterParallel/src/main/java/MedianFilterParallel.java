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
        var reds = new ArrayList<Integer>();
        var greens = new ArrayList<Integer>();
        var blues = new ArrayList<Integer>();
        var alphas = new ArrayList<Integer>();

        for (int i = -radius; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                try {
                    var rgb = new Color(srcImg.getRGB(x + i, y + j), true);
                    reds.add(rgb.getRed());
                    greens.add(rgb.getGreen());
                    blues.add(rgb.getBlue());
                    alphas.add(rgb.getAlpha());
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
        }

        reds.sort(Integer::compare);
        greens.sort(Integer::compare);
        blues.sort(Integer::compare);
        alphas.sort(Integer::compare);
        int mid = reds.size() / 2;
        return getColorRGB(reds.get(mid), greens.get(mid), blues.get(mid), alphas.get(mid));
    }

    private static int getColorRGB(int red, int green, int blue, int alpha) {
        System.out.println(alpha);
        alpha = (alpha << 24) & 0xFF000000; //Shift alpha 24-bits
        red = (red << 16) & 0x00FF0000;     //Shift red 16-bits and mask out other stuff
        green = (green << 8) & 0x0000FF00;  //Shift Green 8-bits and mask out other stuff
        blue = blue & 0x000000FF;           //Mask out anything not blue.
        return alpha | red | green | blue;
    }

    public static BufferedImage filterImageStream(BufferedImage srcImg, int radius, int threadCount)
            throws ExecutionException, InterruptedException {

        int pixelCount = srcImg.getHeight() * srcImg.getWidth();
        ForkJoinPool filterPool = new ForkJoinPool(threadCount);
        int[] filtered =
                filterPool.submit(() -> IntStream.range(0, pixelCount).parallel()
                        .map(k -> getMedian(srcImg, k % srcImg.getWidth(), k / srcImg.getWidth(), radius)))
                        .get().toArray();

        return createImageFromPixels(filtered, srcImg.getWidth(), srcImg.getHeight(), srcImg.getType());
    }
}