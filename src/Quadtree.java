public class Quadtree {
    int width, height;
    Node root;
    Quadtree nw, ne, sw, se;
    private int minBlockSize;
    private double threshold;

    public Quadtree(int[][][] imageArray, int x, int y, int width, int height, int minBlockSize, double threshold) {
        this.width = width;
        this.height = height;
        int[] avgColor = computeAverageColor(imageArray, x, y, width, height);
        this.root = new Node(x, y, avgColor[0], avgColor[1], avgColor[2]);

        double variance = computeVariance(imageArray, x, y, width, height, avgColor);

        if (variance > threshold && width > minBlockSize && height > minBlockSize &&
                width / 2 >= minBlockSize && height / 2 >= minBlockSize)   {
            int halfW = width / 2, halfH = height / 2;
            nw = new Quadtree(imageArray, x, y, halfW, halfH, minBlockSize, threshold);
            ne = new Quadtree(imageArray, x + halfW, y, halfW, halfH, minBlockSize, threshold);
            sw = new Quadtree(imageArray, x, y + halfH, halfW, halfH, minBlockSize, threshold);
            se = new Quadtree(imageArray, x + halfW, y + halfH, halfW, halfH, minBlockSize, threshold);
        }
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
        return new int[]{sumR / count, sumG / count, sumB / count};
    }

    private double computeVariance(int[][][] image, int x, int y, int width, int height, int[] avgColor) {
        double sumSquaredDiff = 0;
        int count = width * height;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                int rDiff = image[i][j][0] - avgColor[0];
                int gDiff = image[i][j][1] - avgColor[1];
                int bDiff = image[i][j][2] - avgColor[2];
                sumSquaredDiff += rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
            }
        }
        return sumSquaredDiff / (3.0 * count); // Normalize over 3 channels
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

}
