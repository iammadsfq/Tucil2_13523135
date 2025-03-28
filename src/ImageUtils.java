import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageUtils {

    public static int[][][] imageToArray(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            int width = image.getWidth();
            int height = image.getHeight();
            int[][][] imageArray = new int[height][width][3];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    imageArray[y][x][0] = (rgb >> 16) & 0xFF;
                    imageArray[y][x][1] = (rgb >> 8) & 0xFF;
                    imageArray[y][x][2] = rgb & 0xFF;
                }
            }
            return imageArray;
        } catch (Exception e) {
            return null;
        }
    }
}