import java.util.Scanner;
import java.io.File;


public class Main {
    static class BinSearchResult {
        double threshold;
        Quadtree tree;

        BinSearchResult(double threshold, Quadtree tree) {
            this.threshold = threshold;
            this.tree = tree;
        }
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);


        try {
            // 1. [INPUT] Alamat absolut gambar yang akan dikompresi
            String inputPath;
            do {
                System.out.print("Masukkan path gambar input: ");
                inputPath = scanner.nextLine().trim();
                if (!new File(inputPath).exists()) {
                    System.out.println("Error: File tidak ditemukan. Coba lagi.");
                }
            } while (!new File(inputPath).exists());



            // 2. [INPUT] Metode perhitungan error (gunakan penomoran sebagai input)
            int errorMethod;
            do {
                System.out.println("Pilih metode perhitungan error:");
                System.out.println("1. Variance\n2. Mean Absolute Deviation (MAD)\n3. Max Pixel Difference\n4. Entropy\n5. Structural Similarity Index (SSIM)");
                System.out.print("Masukkan nomor metode: ");
                while (!scanner.hasNextInt()) {
                    System.out.println("Error: Input harus berupa angka.");
                    scanner.next();
                }
                errorMethod = scanner.nextInt();
                if (errorMethod < 1 || errorMethod > 5) {
                    System.out.println("Error: Pilihan metode tidak valid. Coba lagi.\n");
                }
            } while (errorMethod < 1 || errorMethod > 5);



            // 3. [INPUT] Ambang batas (threshold)
            double threshold;
            do {
                System.out.print("Masukkan ambang batas (threshold): ");
                while (!scanner.hasNextDouble()) {
                    System.out.println("Error: Input harus berupa angka.");
                    scanner.next();
                }
                threshold = scanner.nextDouble();
                if (threshold < 0) {
                    System.out.println("Error: Threshold harus lebih besar atau sama dengan 0. Coba lagi.\n");
                }
                else if (errorMethod == 5 && (threshold <= 0 || threshold >= 1)) {
                    System.out.println("Error: Threshold untuk SSIM harus dalam rentang (0,1). Coba lagi.\n");
                }
            } while ((errorMethod == 5 && (threshold <= 0 || threshold >= 1)) || threshold < 0);



            // 4. [INPUT] Ukuran blok minimum
            int minBlockSize;
            do {
                System.out.print("Masukkan ukuran blok minimum: ");
                while (!scanner.hasNextInt()) {
                    System.out.println("Error: Input harus berupa angka.");
                    scanner.next();
                }
                minBlockSize = scanner.nextInt();
                if (minBlockSize < 1) {
                    System.out.println("Error: Ukuran blok minimum harus lebih besar dari 0. Coba lagi.\n");
                }
            } while (minBlockSize < 1);



            // 5. [INPUT] Target persentase kompresi
            double targetCompression;
            do {
                System.out.print("Masukkan target persentase kompresi (0 untuk menonaktifkan mode ini): ");
                while (!scanner.hasNextDouble()) {
                    System.out.println("Error: Input harus berupa angka.");
                    scanner.next();
                }
                targetCompression = scanner.nextDouble();
                if (targetCompression < 0 || targetCompression > 1) {
                    System.out.println("Error: Persentase kompresi harus di antara 0 dan 1. Coba lagi.\n");
                }
            } while (targetCompression < 0 || targetCompression > 1);



            // 6. [INPUT] Alamat absolut gambar hasil kompresi
            String outputPath;
            do {
                System.out.print("Masukkan path gambar output: ");
                scanner.nextLine(); // Consume newline
                outputPath = scanner.nextLine().trim();

                String inputExt = inputPath.substring(inputPath.lastIndexOf('.') + 1).toLowerCase();
                String outputExt = outputPath.substring(outputPath.lastIndexOf('.') + 1).toLowerCase();

                if (!inputExt.equals(outputExt)) {
                    System.out.println("Error: Ekstensi file input dan output harus sama. Coba lagi.");
                }
            } while (!inputPath.substring(inputPath.lastIndexOf('.') + 1).equalsIgnoreCase(outputPath.substring(outputPath.lastIndexOf('.') + 1)));


            File inputFile = new File(inputPath);
            int[][][] imageArray = Utils.pathToArray(inputPath);


            //START
            long startTime = System.nanoTime();

            // COMPRESSION
            Quadtree tree;
            if (targetCompression > 0) {
                BinSearchResult binSearchResult = findOptimalThreshold(imageArray, inputFile, outputPath, targetCompression, errorMethod);
                threshold = binSearchResult.threshold;
                tree = binSearchResult.tree;
            }
            else {
                tree = new Quadtree(imageArray, 0, 0, imageArray[0].length, imageArray.length, threshold, minBlockSize, errorMethod);
            }

            Utils.saveQuadtreeAsImage(tree, imageArray[0].length, imageArray.length, outputPath);
            long endTime = System.nanoTime();

            System.out.println("Berhasil Kompresi!");
            System.out.println("Sebelum: " + String.format("%.2f KB", Utils.getFileSize(inputPath) / 1024.0));
            System.out.println("Sesudah: " + String.format("%.2f KB", Utils.getFileSize(outputPath) / 1024.0));
            System.out.println("Persentase Kompresi: " + String.format("%.2f", (1 - Utils.getFileSize(outputPath) / (double) Utils.getFileSize(inputPath)) * 100) + "%");
            System.out.println("Kedalaman: " + tree.getDepth());
            System.out.println("Waktu eksekusi: "+ (endTime - startTime)/1_000_000 + " ms");
        } catch (Exception e) {
            System.out.println("Gagal!");
            e.printStackTrace();
        }

        scanner.close();
    }

    private static BinSearchResult findOptimalThreshold(int[][][] imageArray, File inputFile, String outputPath, double targetCompression, int errorMethod) {
        int minBlockSize = 1;
        double lowerThreshold;
        double upperThreshold;
        if (errorMethod == 1) {
            lowerThreshold = Math.pow(10,targetCompression*10-7);
            upperThreshold = Math.pow(10,targetCompression*10-1);
        }
        else if (errorMethod == 2) {
            lowerThreshold = Math.pow(Math.pow(10,targetCompression*10-7), 2.0/3);
            upperThreshold = Math.pow(Math.pow(10,targetCompression*10-1), 2.0/3);
        }
        else if (errorMethod == 3) {
            lowerThreshold = 0;
            upperThreshold = 255;
        }
        else if (errorMethod == 4) {
            lowerThreshold = 0;
            upperThreshold = 8;
        }
        else {
            lowerThreshold = 0;
            upperThreshold = 1;
        }
        double threshold = (lowerThreshold + upperThreshold) / 2.0;
        double temp;
        Quadtree tree = null;
        int i;
        for (i = 0; i < 100; i++) {
            tree = new Quadtree(imageArray, 0, 0, imageArray[0].length, imageArray.length, threshold, minBlockSize, errorMethod);
            try {
                Utils.saveQuadtreeAsImage(tree, imageArray[0].length, imageArray.length, outputPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            double compressionRatio = 1 - Utils.getFileSize(outputPath) / (double) inputFile.length();
            if (Math.abs(compressionRatio - targetCompression) < 0.005) {
                break;
            }

            if (errorMethod == 5) {
                if (compressionRatio > targetCompression) {
                    lowerThreshold = threshold;
                } else {
                    upperThreshold = threshold;
                }
            }
            else {
                if (compressionRatio < targetCompression) {
                    lowerThreshold = threshold;
                } else {
                    upperThreshold = threshold;
                }
            }

            temp = (lowerThreshold + upperThreshold) / 2.0;
            if (Math.abs(temp - threshold) < 0.000001) {
                break;
            }
            threshold = temp;
            System.out.println(i);
            System.out.println(threshold);
        }
        System.out.println(i);
        System.out.println(threshold);
        return new BinSearchResult(threshold, tree);
    }



}
