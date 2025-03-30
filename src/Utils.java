import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import com.github.dragon66.AnimatedGIFWriter;
import java.io.OutputStream;


public class Utils {

    public static int[][][] pathToArray(String imagePath) {
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

    public static void saveQuadtreeAsImage(Quadtree tree, int width, int height, String outputPath) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        String outputExt = outputPath.substring(outputPath.lastIndexOf('.') + 1).toLowerCase();
        fillImageFromQuadtree(tree, img);
        ImageIO.write(img, outputExt, new File(outputPath));
    }

    public static void fillImageFromQuadtree(Quadtree tree, BufferedImage img) {
        if (tree.root == null) return;

        if (tree.isLeaf) {
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
        return file.length();
    }

    public static List<BufferedImage> generateQuadtreeFrames(Quadtree tree, int width, int height, int maxDepth) {
        List<BufferedImage> frames = new ArrayList<>();
        int step = (maxDepth - 1) / 4;
        if (step < 1) step = 1;

        for (int d = 1; d < maxDepth; d += step) {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            fillQuadtreeFrame(tree, img, d, 1);
            frames.add(img);
        }
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        fillQuadtreeFrame(tree, img, maxDepth, 1);
        frames.add(img);

        return frames;
    }

    private static void fillQuadtreeFrame(Quadtree tree, BufferedImage img, int targetDepth, int currentDepth) {
        if (tree == null || tree.root == null) return;

        if (currentDepth > targetDepth) return;

        if (tree.isLeaf || currentDepth == targetDepth) {
            for (int i = tree.getY(); i < tree.getY() + tree.height; i++) {
                for (int j = tree.getX(); j < tree.getX() + tree.width; j++) {
                    int rgb = (tree.getRed() << 16) | (tree.getGreen() << 8) | tree.getBlue();
                    img.setRGB(j, i, rgb);
                }
            }
        } else {
            fillQuadtreeFrame(tree.nw, img, targetDepth, currentDepth + 1);
            fillQuadtreeFrame(tree.ne, img, targetDepth, currentDepth + 1);
            fillQuadtreeFrame(tree.sw, img, targetDepth, currentDepth + 1);
            fillQuadtreeFrame(tree.se, img, targetDepth, currentDepth + 1);
        }
    }


    public static void createGIF(List<BufferedImage> frames, String outputPath, int delay, boolean loop) throws IOException {
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("No frames to create GIF");
        }

        File outputFile = new File(outputPath);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            AnimatedGIFWriter writer = new AnimatedGIFWriter(false);
            writer.prepareForWrite(outputStream, -1, -1);

            for (BufferedImage frame : frames) {
                writer.writeFrame(outputStream, frame, delay);
            }

            writer.finishWrite(outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}