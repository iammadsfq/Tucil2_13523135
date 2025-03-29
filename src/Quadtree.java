public class Quadtree {
    int width, height;
    Node root;
    Quadtree nw, ne, sw, se;
    boolean isLeaf;

    public Quadtree(int[][][] imageArray, int x, int y, int width, int height, double threshold, int minBlockSize) {
        this.width = width;
        this.height = height;
        System.out.println(width);
        System.out.println(height);
        int[] avgColor = computeAverageColor(imageArray, x, y, width, height);
        this.root = new Node(x, y, avgColor[0], avgColor[1], avgColor[2]);
        this.isLeaf = true;
        if (shouldSplit(imageArray, x, y, width, height, threshold, minBlockSize)) {
            this.isLeaf = false;
            int westW = width / 2;
            int northH = height / 2;

            int eastW = width - westW;
            int southH = height - northH;
            nw = new Quadtree(imageArray, x, y, westW, northH, threshold, minBlockSize);
            ne = new Quadtree(imageArray, x + westW, y, eastW, northH, threshold, minBlockSize);
            sw = new Quadtree(imageArray, x, y + northH, westW, southH, threshold, minBlockSize);
            se = new Quadtree(imageArray, x + westW, y + northH, eastW, southH, threshold, minBlockSize);
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
        return new int[] {sumR / count, sumG / count, sumB / count};
    }

    private boolean shouldSplit(int[][][] image, int x, int y, int width, int height, double threshold, int minBlockSize) {

        if (width*height <= minBlockSize) return false;

        double variance = computeVariance(image, x, y, width, height);
        return variance > threshold;
    }

    private double computeVariance(int[][][] image, int x, int y, int width, int height) {
        int[] avgColor = computeAverageColor(image, x, y, width, height);
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
