import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class MedianFilterParallelTest {
    BufferedImage img;
    int[] pixels = {9, 1, 8, 2, 7, 3, 6, 4, 5};
    int[] et = {7, 7, 7, 6, 5, 5, 6, 5, 5};

    @org.junit.jupiter.api.Test
    void filterSmallImageParallel() {
        int[] newPixels = new int[9];
        img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, 3, 3, pixels, 0, 3);
        BufferedImage newImg;
        try {
            newImg = MedianFilterParallel.filterImageParallel(img, 1, 200);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < newImg.getWidth(); i++) {
            for (int j = 0; j < newImg.getHeight(); j++) {
                newPixels[i * newImg.getWidth() + j] = newImg.getRGB(j, i);
            }
        }
        assertArrayEquals(et, newPixels);
    }

    @org.junit.jupiter.api.Test
    void filterSmallImageStream() {
        int[] newPixels = new int[9];
        img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, 3, 3, pixels, 0, 3);
        BufferedImage newImg;
        try {
            newImg = MedianFilterParallel.filterImageStream(img, 1, 200);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < newImg.getWidth(); i++) {
            for (int j = 0; j < newImg.getHeight(); j++) {
                newPixels[i * newImg.getWidth() + j] = newImg.getRGB(j, i);
            }
        }
        assertArrayEquals(et, newPixels);
    }

    void filterLargeImageParallelNoCheck(int width, int height, int threadsCount){
        img = getRandomImage(width, height);
        try {
            MedianFilterParallel.filterImageParallel(img, 5, threadsCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.Test
    void filterLargeImageParallel1() { // 1 поток
        filterLargeImageParallelNoCheck(1000, 1000, 1);
    }

    @org.junit.jupiter.api.Test
    void filterLargeImageParallel2() { // 1 поток
        filterLargeImageParallelNoCheck(1000, 1000, 2);
    }

    @org.junit.jupiter.api.Test
    void filterLargeImageParallel4() { // 5 потоков
        filterLargeImageParallelNoCheck(1000, 1000, 4);
    }

    @org.junit.jupiter.api.Test
    void filterLargeImageParallel8() { // 8 потоков
        filterLargeImageParallelNoCheck(1000, 1000, 8);
    }

    @org.junit.jupiter.api.Test
    void filterLargeImageParallel16() { // 8 потоков
        filterLargeImageParallelNoCheck(1000, 1000, 16);
    }

    void filterLargeImageStreamNoCheck(int width, int height, int threadsCount){
        img = getRandomImage(width, height);
        try {
            MedianFilterParallel.filterImageStream(img, 5, threadsCount);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.Test
    void filterLargeImageStream1() { // 1 поток
        filterLargeImageStreamNoCheck(1000, 1000, 1);
    }

    @org.junit.jupiter.api.Test
    void filterLargeImageStream2() { // 1 поток
        filterLargeImageStreamNoCheck(1000, 1000, 2);
    }

    @org.junit.jupiter.api.Test
    void filterLargeImageStream4() { // 5 потоков
        filterLargeImageStreamNoCheck(1000, 1000, 4);
    }

    @org.junit.jupiter.api.Test
    void filterLargeImageStream8() { // 8 потоков
        filterLargeImageStreamNoCheck(1000, 1000, 8);
    }

    @org.junit.jupiter.api.Test
    void filterLargeImageStream16() { // 8 потоков
        filterLargeImageStreamNoCheck(1000, 1000, 16);
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

    @org.junit.jupiter.api.Test
    void filterImageByPath(){
        int radius = 5;
        int threadCount = 4;

        String path = "src\\test\\test_img.png";
        File imgFile = new File(path);
        BufferedImage img;
        try {
            img = ImageIO.read(imgFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        File newImgFile = new File("src\\test\\filtered_img.png");
        try {
            ImageIO.write(MedianFilterParallel.filterImageStream(img, radius, threadCount),
                    "png", newImgFile);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}