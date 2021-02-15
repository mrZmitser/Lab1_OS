import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ConsoleParallel {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Arguments must have the next signature: \"path filterRadius threadsCount\"");
            System.exit(-1);
        }
        int radius = parsePositiveInt(args[1]);
        int threadCount = parsePositiveInt(args[2]);

        File imgFile = new File(args[0]);
        BufferedImage img = null;
        try {
            img = ImageIO.read(imgFile);
        } catch (IOException e) {
            System.err.println("File not found");
            System.exit(-1);
        }
        File newImgFile = new File("img.png");
        try {
            ImageIO.write(MedianFilterParallel.filterImageParallel(img, radius, threadCount),
                    "png", newImgFile);
        } catch (IOException e) {
            System.err.println("Unable to create file");
            System.exit(-1);
        } catch (InterruptedException e) {
            System.err.println("The program has been interrupted");
            System.exit(-1);
        }
    }
    private static int parsePositiveInt(String arg){
        int n = 0;
        try {
            n = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            System.err.println("The argument must be integer");
            System.exit(-1);
        }
        if (n <= 0) {
            System.err.println("Radius must be positive");
            System.exit(-1);
        }
        return n;
    }
}
