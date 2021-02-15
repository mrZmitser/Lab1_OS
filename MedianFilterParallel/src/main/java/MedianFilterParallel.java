import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class MedianFilterParallel {
    private static class FilterThread extends Thread {
        private final BufferedImage srcImg;
        private final int radius;
        private final int startPos, finishPos;
        private final int[] filteredRGBs;

        public int[] getFilteredRGBs() {
            return filteredRGBs;
        }

        public FilterThread(BufferedImage srcImg, int startPos, int finishPos, int radius) {
            this.srcImg = srcImg;
            this.startPos = startPos;
            this.finishPos = finishPos;
            filteredRGBs = new int[finishPos - startPos];
            this.radius = radius;
        }

        @Override
        public void run() {
            for (int k = startPos; k < finishPos; k++) {
                int i = k / srcImg.getHeight();
                int j = k % srcImg.getHeight();
                filteredRGBs[k - startPos] = getMedian(srcImg, i, j, radius);
            }
        }
    }

    public static BufferedImage filterImage(BufferedImage srcImg, int radius) {
        BufferedImage newImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), TYPE_INT_ARGB);
        for (int i = 0; i < srcImg.getWidth(); ++i) {
            for (int j = 0; j < srcImg.getHeight(); ++j) {
                newImg.setRGB(i, j, getMedian(srcImg, i, j, radius));
            }
        }
        return newImg;
    }

    public static BufferedImage filterImageParallel(BufferedImage srcImg, int radius, int threadCount)
            throws InterruptedException {
        assert (radius > 0);
        assert (threadCount > 0);
        FilterThread[] threads = new FilterThread[threadCount];
        int square = srcImg.getHeight() * srcImg.getWidth();
        int sliceSize = square/threadCount;
        for (int i = 0; i < threadCount; i++) {
            if (i < threadCount - 1) {
                threads[i] = new FilterThread(srcImg, sliceSize * i, sliceSize * (i + 1), radius);
            } else {
                threads[i] = new FilterThread(srcImg, sliceSize * i, square, radius);
            }
        }
        for (var thread: threads) {
            thread.start();
        }
        for (var thread: threads) {
            thread.join();
        }
        var filteredRGBs = new ArrayList<Integer>();
        for (var thread: threads) {
            for(var rgb: thread.getFilteredRGBs())
                filteredRGBs.add(rgb);
        }
        return createImageFromPixels(filteredRGBs, srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_INT_RGB);
    }

    private static BufferedImage createImageFromPixels(ArrayList<Integer> filteredRGBs, int width, int height,
                                                       int imageType) {
        assert (width > 0);
        assert (height > 0);
        var img = new BufferedImage(width, height, imageType);
        int k = 0;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                img.setRGB(i, j, filteredRGBs.get(k++));
            }
        }
        return img;
    }

    private static int getMedian(BufferedImage srcImg, int x, int y, int radius) {
        int squareSide = 2 * radius + 1;
        int[] reds = new int[squareSide * squareSide];
        int[] greens = new int[squareSide * squareSide];
        int[] blues = new int[squareSide * squareSide];

        int k = 0;
        for (int i = -radius; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                try {
                    var rgb = new Color(srcImg.getRGB(x + i, y + j));
                    reds[k] = rgb.getRed();
                    greens[k] = rgb.getGreen();
                    blues[k] = rgb.getBlue();
                    k++;
                } catch (IndexOutOfBoundsException e) {
                    return srcImg.getRGB(x, y);
                }
            }
        }
        Arrays.sort(reds);
        Arrays.sort(greens);
        Arrays.sort(blues);
        return getColorRGB(reds[reds.length / 2], greens[greens.length / 2], blues[blues.length/2]);
    }

    private static int getColorRGB(int red, int green, int blue) {
        red = (red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        green = (green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        blue = blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | red | green | blue;
    }
}