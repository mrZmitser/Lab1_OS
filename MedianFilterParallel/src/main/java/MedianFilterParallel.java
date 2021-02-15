import java.awt.image.BufferedImage;
import java.util.Arrays;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class MedianFilterParallel {
    private static class FilterThread extends Thread{
        private final BufferedImage srcImg;
        private final int startPos, finishPos;
        private int[] filteredRgbs;

        FilterThread(BufferedImage srcImg, int startPos, int finishPos){
            this.srcImg = srcImg;
            this.startPos = startPos;
            this.finishPos = finishPos;
        }

        @Override
        public void run() {
        }
    }

    BufferedImage filterImage(BufferedImage srcImg, int size) {
        BufferedImage newImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), TYPE_INT_ARGB);
        for (int i = 0; i < srcImg.getWidth(); ++i) {
            for (int j = 0; j < srcImg.getHeight(); ++j) {
                newImg.setRGB(i, j, getMedian(srcImg, i, j, size));
            }
        }
        return newImg;
    }

    private int getMedian(BufferedImage srcImg, int x, int y, int size) {
        int squareSide = 2 * size + 1;
        int[] square = new int[squareSide * squareSide];
        int k = 0;
        for (int i = -size; i <= size; ++i) {
            for (int j = -size; j <= size; ++j) {
                try {
                    square[k++] = srcImg.getRGB(x + i, y + j);
                } catch (IndexOutOfBoundsException e) {
                    return srcImg.getRGB(x, y);
                }
            }
        }
        Arrays.sort(square);
        return square[square.length / 2];
    }
}
