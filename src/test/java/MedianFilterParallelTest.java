import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MedianFilterParallelTest {
    BufferedImage img;
    int imgWidth = 10000;
    int imgHeight = 10000;
    int[] pixels = {9, 1, 8, 2, 7, 3, 6, 4, 5};
    int[] rightResult = {-16777209, -16777209, -16777209, -16777210, -16777211, -16777211, -16777210,
            -16777211, -16777211};

    @Test
    void imgParallel() {
        int[] newPixels = new int[9];
        img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, 3, 3, pixels, 0, 3);
        BufferedImage newImg;
        try {
            newImg = MedianFilterParallel.filterImageParallel(img, 1, 4);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < newImg.getWidth(); i++) {
            for (int j = 0; j < newImg.getHeight(); j++) {
                newPixels[i * newImg.getWidth() + j] = newImg.getRGB(j, i);
            }
        }
        assertArrayEquals(rightResult, newPixels);
    }

    @Test
    void imgStream() {
        int[] newPixels = new int[9];
        img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, 3, 3, pixels, 0, 3);
        BufferedImage newImg;
        try {
            newImg = MedianFilterParallel.filterImageStream(img, 1, 4);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < newImg.getWidth(); i++) {
            for (int j = 0; j < newImg.getHeight(); j++) {
                newPixels[i * newImg.getWidth() + j] = newImg.getRGB(j, i);
            }
        }
        assertArrayEquals(rightResult, newPixels);
    }

    void bigImgParallel(int width, int height, int threadsCount) {
        img = getRandomImage(width, height);
        try {
            MedianFilterParallel.filterImageParallel(img, 5, threadsCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void bigImgParallel1() { // 1 поток
        bigImgParallel(imgWidth, imgHeight, 1);
    }

    @Test
    void bigImgParallel2() { // 2 потока
        bigImgParallel(imgWidth, imgHeight, 2);
    }

    @Test
    void bigImgParallel4() { // 4 потока
        bigImgParallel(imgWidth, imgHeight, 4);
    }

    @Test
    void bigImgParallel8() { // 8 потоков
        bigImgParallel(imgWidth, imgHeight, 8);
    }

    @Test
    void bigImgParallel16() { // 16 потоков
        bigImgParallel(imgWidth, imgHeight, 16);
    }

    void bigImgStream(int width, int height, int threadsCount) {
        img = getRandomImage(width, height);
        try {
            MedianFilterParallel.filterImageStream(img, 5, threadsCount);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    void bigImgStream1() { // 1 поток
        bigImgStream(imgWidth, imgHeight, 1);
    }

    @Test
    void bigImgStream2() { // 2 потока
        bigImgStream(imgWidth, imgHeight, 2);
    }

    @Test
    void bigImgStream4() { // 4 потока
        bigImgStream(imgWidth, imgHeight, 4);
    }

    @Test
    void bigImgStream8() { // 8 потоков
        bigImgStream(imgWidth, imgHeight, 8);
    }

    @Test
    void bigImgStream16() { // 16 потоков
        bigImgStream(imgWidth, imgHeight, 16);
    }

    private BufferedImage getRandomImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Random random = new Random(20);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                img.setRGB(i, j, random.nextInt());
            }
        }
        return img;
    }

    @Test
    void filterImageByPath() {
        int radius = 5;
        int threadCount = 4;

        String path = "src\\main\\resources\\test_img.png";
        File imgFile = new File(path);
        BufferedImage img;
        try {
            img = ImageIO.read(imgFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        File newImgFile = new File("src\\main\\resources\\filtered_img.png");
        try {
            ImageIO.write(MedianFilterParallel.filterImageStream(img, radius, threadCount),
                    "png", newImgFile);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}