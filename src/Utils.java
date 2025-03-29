import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Utils {

    public static int[][][] imageToArray(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            int width = image.getWidth();
            int height = image.getHeight();
            System.out.println(width);
            System.out.println(height);
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

    public static void saveQuadtreeAsImage(Quadtree tree, int width, int height, String outputPath) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        fillImageFromQuadtree(tree, img);
        ImageIO.write(img, "jpg", new File(outputPath));
    }

    private static void fillImageFromQuadtree(Quadtree tree, BufferedImage img) {
        if (tree.root == null) return;

        if (tree.nw == null) { // Leaf node
            for (int i = tree.getY(); i < tree.getY() + tree.height; i++) {
                for (int j = tree.getX(); j < tree.getX() + tree.width; j++) {
                    int rgb = (tree.getRed() << 16) | (tree.getGreen() << 8) | tree.getBlue();
                    img.setRGB(j, i, rgb);
                }
            }
        } else {
            fillImageFromQuadtree(tree.nw, img);
            fillImageFromQuadtree(tree.ne, img);
            fillImageFromQuadtree(tree.sw, img);
            fillImageFromQuadtree(tree.se, img);
        }
    }

    public static long getFileSize(String filePath) {
        File file = new File(filePath);
        return file.length();  // Mengembalikan ukuran file dalam byte
    }
}