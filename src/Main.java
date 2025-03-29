public class Main {
    public static void main(String[] args) {
        try {
            String inputPath = "D:\\Syafiq\\4th_Semester\\Stima\\Tucil2\\test\\lion.jpg";
            String outputPath = "D:\\Syafiq\\4th_Semester\\Stima\\Tucil2\\test\\out.jpeg";
            int[][][] imageArray = Utils.imageToArray(inputPath);
            double threshold = 100000000;
            int minBlockSize = 100000000;
            Quadtree tree = new Quadtree(imageArray, 0, 0, imageArray[0].length, imageArray.length, threshold, minBlockSize);
            Utils.saveQuadtreeAsImage(tree, imageArray[0].length, imageArray.length, outputPath);
            System.out.println("Berhasil Kompresi!");
            System.out.println("Sebelum: " + String.format("%.2f KB", Utils.getFileSize(inputPath)/1024.0));
            System.out.println("Sesudah: " + String.format("%.2f KB", Utils.getFileSize(outputPath)/1024.0));
            System.out.println("Persentase Kompresi: " + String.format("%.2f", (1 - Utils.getFileSize(outputPath)/(double) Utils.getFileSize(inputPath))*100) + "%");
            System.out.println("Kedalaman: " + tree.getDepth());
        } catch (Exception e) {
            System.out.println("Gagal!");
            e.printStackTrace();
        }
    }
}
