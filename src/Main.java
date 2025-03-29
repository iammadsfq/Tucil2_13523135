public class Main {
    public static void main(String[] args) {
        try {
            int[][][] imageArray = ImageUtils.imageToArray("D:\\Syafiq\\4th_Semester\\Stima\\Tucil2\\test\\monalisa.jpeg");
            double threshold = 100;
            int minBlockSize = 100;
            Quadtree tree = new Quadtree(imageArray, 0, 0, imageArray[0].length, imageArray.length, threshold, minBlockSize);
            ImageUtils.saveQuadtreeAsImage(tree, imageArray[0].length, imageArray.length, "D:\\Syafiq\\4th_Semester\\Stima\\Tucil2\\test\\out.jpeg");
            System.out.println("Compression Complete!");

        } catch (Exception e) {
            System.out.println("Gagal!");
            e.printStackTrace();
        }
    }
}
