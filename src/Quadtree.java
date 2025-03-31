import java.util.HashMap;
import java.util.Map;

public class Quadtree {
    int width, height;
    Node root;
    int depth; // root = 1
    Quadtree nw, ne, sw, se;
    boolean isLeaf;

    public Quadtree(int[][][] imageArray, int x, int y, int width, int height, double threshold, int minBlockSize, int errorMethod, int depth) {
        if (width*height == 0) {
            isLeaf = false;
            return;
        }
        this.width = width;
        this.height = height;
        int[] avgColor = computeAverageColor(imageArray, x, y, width, height);
        this.root = new Node(x, y, avgColor[0], avgColor[1], avgColor[2]);
        this.depth = depth;
        this.isLeaf = true;
        if (shouldSplit(imageArray, avgColor, x, y, width, height, threshold, minBlockSize, errorMethod)) {
            this.isLeaf = false;
            int westW = width / 2;
            int northH = height / 2;

            int eastW = width - westW;
            int southH = height - northH;
            nw = new Quadtree(imageArray, x, y, westW, northH, threshold, minBlockSize, errorMethod, depth+1);
            ne = new Quadtree(imageArray, x + westW, y, eastW, northH, threshold, minBlockSize, errorMethod, depth+1);
            sw = new Quadtree(imageArray, x, y + northH, westW, southH, threshold, minBlockSize, errorMethod, depth+1);
            se = new Quadtree(imageArray, x + westW, y + northH, eastW, southH, threshold, minBlockSize, errorMethod, depth+1);
        }
    }

    public int maxDepth() {
        if (isLeaf) {
            return 1;
        }
        int nwDepth = (nw != null) ? nw.maxDepth() : 0;
        int neDepth = (ne != null) ? ne.maxDepth() : 0;
        int swDepth = (sw != null) ? sw.maxDepth() : 0;
        int seDepth = (se != null) ? se.maxDepth() : 0;

        return 1 + Math.max(Math.max(nwDepth, neDepth), Math.max(swDepth, seDepth));
    }

    public int leafCount() {
        if (isLeaf) {
            return 1;
        }
        int nwCount = (nw != null) ? nw.leafCount() : 0;
        int neCount = (ne != null) ? ne.leafCount() : 0;
        int swCount = (sw != null) ? sw.leafCount() : 0;
        int seCount = (se != null) ? se.leafCount() : 0;

        return 1 + nwCount + neCount + swCount + seCount;
    }

    private int[] computeAverageColor(int[][][] image, int x, int y, int width, int height) {
        int sumR = 0, sumG = 0, sumB = 0, count = width * height;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                sumR += image[i][j][0];
                sumG += image[i][j][1];
                sumB += image[i][j][2];
            }
        }
        return new int[] {sumR / count, sumG / count, sumB / count};
    }

    private boolean shouldSplit(int[][][] image, int[] avgColor, int x, int y, int width, int height, double threshold, int minBlockSize, int errorMethod) {

        if (width*height/4 < minBlockSize) return false;

        double error = computeError(image, avgColor, x, y, width, height, errorMethod);
        if (errorMethod == 5) {
            return error < threshold;
        }
        return error > threshold;
    }

    private double computeError(int[][][] image, int[] avgColor, int x, int y, int width, int height, int errorMethod) {
        double error = 0;
        if (errorMethod == 1) {
            error = computeVariance(image, avgColor, x, y, width, height);
        }
        else if (errorMethod == 2) {
            error = computeMAD(image, avgColor, x, y, width, height);
        }
        else if (errorMethod == 3) {
            error = computeMPD(image, x, y, width, height);
        }
        else if (errorMethod == 4) {
            error = computeEntropy(image, x, y, width, height);
        }
        else if (errorMethod == 5) {
            error = computeSSIM(image, avgColor, x, y, width, height);
        }

        return error;
    }


    // VARIANCE
    private double computeVariance(int[][][] image, int[] avgColor, int x, int y, int width, int height) {
        double redVariance = 0;
        double greenVariance = 0;
        double blueVariance = 0;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                redVariance += Math.pow(image[i][j][0] - avgColor[0], 2);
                greenVariance += Math.pow(image[i][j][1] - avgColor[1], 2);
                blueVariance += Math.pow(image[i][j][2] - avgColor[2], 2);
            }
        }
        redVariance /= width*height;
        greenVariance /= width*height;
        blueVariance /= width*height;
        return (redVariance + greenVariance + blueVariance) / 3;
    }

    // MEAN ABSOLUTE DEVIATION (MAD)
    private double computeMAD(int[][][] image, int[] avgColor, int x, int y, int width, int height) {
        double redMAD = 0;
        double greenMAD = 0;
        double blueMAD = 0;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                redMAD += Math.abs(image[i][j][0] - avgColor[0]);
                greenMAD += Math.abs(image[i][j][1] - avgColor[1]);
                blueMAD += Math.abs(image[i][j][2] - avgColor[2]);
            }
        }
        redMAD /= width*height;
        greenMAD /= width*height;
        blueMAD /= width*height;
        return (redMAD + greenMAD + blueMAD) / 3;
    }

    // MAX PIXEL DIFFERENCE (MPD)
    private double computeMPD(int[][][] image, int x, int y, int width, int height) {
        double redMax = image[y][x][0];
        double redMin = image[y][x][0];
        double greenMax = image[y][x][1];
        double greenMin = image[y][x][1];
        double blueMax = image[y][x][2];
        double blueMin = image[y][x][2];

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                if (image[i][j][0] > redMax) redMax = image[i][j][0];
                if (image[i][j][0] < redMin) redMin = image[i][j][0];

                if (image[i][j][1] > greenMax) greenMax = image[i][j][1];
                if (image[i][j][1] < greenMin) greenMin = image[i][j][1];

                if (image[i][j][2] > blueMax) blueMax = image[i][j][2];
                if (image[i][j][2] < blueMin) blueMin = image[i][j][2];
            }
        }
        return ((redMax - redMin) + (greenMax - greenMin) + (blueMax - blueMin)) / 3;
    }


    // ENTROPY
    private double computeEntropy(int[][][] image, int x, int y, int width, int height) {
        return (computeChannelEntropy(image, x, y, width, height, 0) +
                computeChannelEntropy(image, x, y, width, height, 1) +
                computeChannelEntropy(image, x, y, width, height, 2)) / 3.0;
    }

    private double computeChannelEntropy(int[][][] image, int x, int y, int width, int height, int channel) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        int totalPixels = width * height;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                int pixelValue = image[i][j][channel];
                frequencyMap.put(pixelValue, frequencyMap.getOrDefault(pixelValue, 0) + 1);
            }
        }

        double entropy = 0.0;
        for (int count : frequencyMap.values()) {
            double probability = (double) count / totalPixels;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        return entropy;
    }

    private double computeSSIM(int[][][] image, int[] avgColor, int x, int y, int width, int height) {
        double ssimR = computeChannelSSIM(image, avgColor[0], x, y, width, height, 0);
        double ssimG = computeChannelSSIM(image, avgColor[1], x, y, width, height, 1);
        double ssimB = computeChannelSSIM(image, avgColor[2], x, y, width, height, 2);

        return 0.2989*ssimR + 0.5870*ssimG + 0.1140*ssimB;
    }

    private double computeChannelSSIM(int[][][] image, int mean, int x, int y, int width, int height, int channel) {
        final double C2 = 58.5225;
        double varianceX = 0;
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                double diffX = image[i][j][channel] - mean;
                varianceX += diffX * diffX;
            }
        }
        varianceX /= (width * height);

        //penyederhanaan karena varianceY = 0, cov = 0, meanX=meanY=mean
        return C2 / (varianceX + C2);
    }


        private static class Node {
        int r, g, b, x, y;

        public Node(int x, int y, int r, int g, int b) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    public int getX() {
        return this.root.x;
    }
    public int getY() {
        return this.root.y;
    }
    public int getRed() {
        return this.root.r;
    }
    public int getGreen() {
        return this.root.g;
    }
    public int getBlue() {
        return this.root.b;
    }
}
