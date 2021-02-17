import org.junit.jupiter.api.Assertions;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class MedianFilterParallelTest {

    BufferedImage img;
    int[] pixels = {9, 1, 8, 2, 7, 3, 6, 4, 5};
    int[] et = {7, 7, 7, 6, 5, 5, 6, 5, 5};

    @org.junit.jupiter.api.Test
    void filterImageParallel() {
        int[] newPixels = new int[9];
        img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, 3, 3, pixels, 0, 3);
        BufferedImage newImg = null;
        try {
            newImg = MedianFilterParallel.filterImageParallel(img, 1, 200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                newPixels[i * 3 + j] = newImg.getRGB(j, i);
            }
        }
        assertArrayEquals(et, newPixels);
    }

    @org.junit.jupiter.api.Test
    void filterImageStream() {
        int[] newPixels = new int[9];
        img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, 3, 3, pixels, 0, 3);
        BufferedImage newImg = null;
        try {
            newImg = MedianFilterParallel.filterImageStream(img, 1, 200);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                newPixels[i * 3 + j] = newImg.getRGB(j, i);
            }
        }
        assertArrayEquals(et, newPixels);
    }

    @org.junit.jupiter.api.Test
    void filterImageParallel1() { // 1 поток

        img = getRandomImage(1000, 1000);
        try {
            MedianFilterParallel.filterImageParallel(img, 5, 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.Test
    void filterImageParallel5() { // 5 потоков

        img = getRandomImage(1000, 1000);
        try {
            MedianFilterParallel.filterImageParallel(img, 5, 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.Test
    void filterImageParallel8() { // 8 потоков

        img = getRandomImage(1000, 1000);
        try {
            MedianFilterParallel.filterImageParallel(img, 5, 8);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @org.junit.jupiter.api.Test
    void filterImageStream1() { // 1 поток

        img = getRandomImage(1000, 1000);
        try {
            MedianFilterParallel.filterImageStream(img, 5, 1);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.Test
    void filterImageStream5() { // 5 потоков

        img = getRandomImage(1000, 1000);
        try {
            MedianFilterParallel.filterImageStream(img, 5, 5);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.Test
    void filterImageStream8() { // 8 потоков

        img = getRandomImage(1000, 1000);
        try {
            MedianFilterParallel.filterImageStream(img, 5, 8);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage getRandomImage(int width, int height){
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Random random = new Random(20);
        for (int i = 0; i < width; i++){
            for(int j = 0; j<height;j++){
                img.setRGB(i, j, random.nextInt());
            }
        }
        return img;
    }
}