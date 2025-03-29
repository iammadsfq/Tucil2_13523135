import java.util.Scanner;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // 1. [INPUT] Alamat absolut gambar yang akan dikompresi
            System.out.print("Masukkan path gambar input: ");
            String inputPath = scanner.nextLine();

            // 2. [INPUT] Metode perhitungan error (gunakan penomoran sebagai input)
            System.out.println("Pilih metode perhitungan error:");
            System.out.println("1. Variance\n2. Mean Absolute Deviation (MAD)\n3. Max Pixel Difference\n4. Entropy\n5. Structural Similarity Index (SSIM)");
            System.out.print("Masukkan nomor metode: ");
            int errorMethod = scanner.nextInt();

            // 3. [INPUT] Ambang batas (threshold)
            System.out.print("Masukkan ambang batas (threshold): ");
            double threshold = scanner.nextDouble();

            // 4. [INPUT] Ukuran blok minimum
            System.out.print("Masukkan ukuran blok minimum: ");
            int minBlockSize = scanner.nextInt();

            // 5. [INPUT] Target persentase kompresi
            System.out.print("Masukkan target persentase kompresi (0 untuk menonaktifkan mode ini): ");
            double targetCompression = scanner.nextDouble();

            // 6. [INPUT] Alamat absolut gambar hasil kompresi
            System.out.print("Masukkan path gambar output: ");
            scanner.nextLine();
            String outputPath = scanner.nextLine();
            File inputFile = new File(inputPath);
            int[][][] imageArray = Utils.pathToArray(inputPath);

            if (targetCompression > 0) {
                threshold = findOptimalThreshold(imageArray, inputFile, outputPath, targetCompression);
            }
            // COMPRESSION
            Quadtree tree = new Quadtree(imageArray, 0, 0, imageArray[0].length, imageArray.length, threshold, minBlockSize);
            Utils.saveQuadtreeAsImage(tree, imageArray[0].length, imageArray.length, outputPath);

            System.out.println("Berhasil Kompresi!");
            System.out.println("Sebelum: " + String.format("%.2f KB", Utils.getFileSize(inputPath) / 1024.0));
            System.out.println("Sesudah: " + String.format("%.2f KB", Utils.getFileSize(outputPath) / 1024.0));
            System.out.println("Persentase Kompresi: " + String.format("%.2f", (1 - Utils.getFileSize(outputPath) / (double) Utils.getFileSize(inputPath)) * 100) + "%");
            System.out.println("Kedalaman: " + tree.getDepth());
        } catch (Exception e) {
            System.out.println("Gagal!");
            e.printStackTrace();
        }

        scanner.close();
    }

    private static double findOptimalThreshold(int[][][] imageArray, File inputFile, String outputPath, double targetCompression) {
        int minBlockSize = 1;
        double lowerThreshold = 0.1;
        double upperThreshold = Math.pow(10.0,targetCompression*10);
        double threshold = (lowerThreshold + upperThreshold) / 2.0;
        int i = 0;
        while (i < 100) {
            i++;
            System.out.println(i);
            Quadtree tree = new Quadtree(imageArray, 0, 0, imageArray[0].length, imageArray.length, threshold, minBlockSize);
            try {
                Utils.saveQuadtreeAsImage(tree, imageArray[0].length, imageArray.length, outputPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println(threshold);
            double compressionRatio = 1 - Utils.getFileSize(outputPath) / (double) inputFile.length();

            if (Math.abs(compressionRatio - targetCompression) < 0.005) {
                System.out.println(compressionRatio);
                System.out.println(targetCompression);
                break;
            }

            if (compressionRatio < targetCompression) {
                lowerThreshold = threshold;
            } else {
                upperThreshold = threshold;
            }
            threshold = (lowerThreshold + upperThreshold) / 2.0;
        }

        return threshold;
    }

}
