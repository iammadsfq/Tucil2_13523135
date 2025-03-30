import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
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
                File file = new File(inputPath);

                if (!file.exists() || !file.isFile()) {
                    System.out.println("Error: File tidak ditemukan. Coba lagi.\n");
                    continue;
                }
                String lowerPath = inputPath.toLowerCase();
                if (!lowerPath.endsWith(".jpg") && !lowerPath.endsWith(".jpeg") &&
                        !lowerPath.endsWith(".png")) {
                    System.out.println("Error: Format file tidak didukung. Harus berupa .jpg, .jpeg, atau .png. Coba lagi.\n");
                    continue;
                }

                try {
                    BufferedImage testImage = ImageIO.read(file);
                    if (testImage == null) {
                        System.out.println("Error: Tidak bisa membaca gambar. Coba lagi.\n");
                        continue;
                    }
                } catch (IOException e) {
                    System.out.println("Error: Tidak bisa membaca file. Coba lagi.\n");
                    continue;
                }

                break;

            } while (true);


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
                outputPath = scanner.nextLine().trim();

                File outputFile = new File(outputPath);
                File outputDir = outputFile.getParentFile();

                if (outputPath.isEmpty()) {
                    System.out.println("Error: Path tidak boleh kosong. Coba lagi.\n");
                    continue;
                }

                if (!outputPath.contains(".")) {
                    System.out.println("Error: Path harus memiliki ekstensi file. Coba lagi.\n");
                    continue;
                }

                String inputExt = inputPath.substring(inputPath.lastIndexOf('.') + 1).toLowerCase();
                String outputExt = outputPath.substring(outputPath.lastIndexOf('.') + 1).toLowerCase();
                if (!inputExt.equals(outputExt)) {
                    System.out.println("Error: Ekstensi file input dan output harus sama. Coba lagi.\n");
                    continue;
                }

                if (outputDir != null && !outputDir.exists()) {
                    System.out.println("Error: Folder tujuan tidak ditemukan. Coba lagi.\n");
                    continue;
                }

                break;

            } while (true);



            File inputFile = new File(inputPath);
            int[][][] imageArray = Utils.pathToArray(inputPath);

            // 7. [INPUT] Alamat absolut GIF
            String gifOutputPath;
            boolean generateGif;
            do {
                System.out.print("Masukkan path GIF output (kosongkan jika tidak ingin menyimpan GIF): ");
                gifOutputPath = scanner.nextLine().trim();

                if (gifOutputPath.isEmpty()) {
                    generateGif = false;
                    break;
                }


                File gifFile = new File(gifOutputPath);
                String gifExt = gifOutputPath.substring(gifOutputPath.lastIndexOf('.') + 1).toLowerCase();
                if (!gifExt.equals("gif")) {
                    System.out.println("Error: Ekstensi file harus .gif. Coba lagi.\n");
                    continue;
                }

                File gifDir = gifFile.getParentFile();
                if (gifDir != null && (!gifDir.exists() || !gifDir.isDirectory())) {
                    System.out.println("Error: Folder tujuan tidak ditemukan. Coba lagi.\n");
                    continue;
                }

                generateGif = true;
                break;
            } while (true);


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
                tree = new Quadtree(imageArray, 0, 0, imageArray[0].length, imageArray.length, threshold, minBlockSize, errorMethod, 1);
            }

            Utils.saveQuadtreeAsImage(tree, imageArray[0].length, imageArray.length, outputPath);
            if (generateGif) {
                int maxDepth = tree.maxDepth();
                List<BufferedImage> frames = Utils.generateQuadtreeFrames(tree, imageArray[0].length, imageArray.length, maxDepth);
                Utils.createGIF(frames, gifOutputPath, 500, true);
            }
            long endTime = System.nanoTime();

            System.out.println("Berhasil Kompresi!");
//            8. [OUTPUT] waktu eksekusi
            System.out.println("1. Waktu eksekusi: "+ (endTime - startTime)/1_000_000 + " ms");

//            9. [OUTPUT] ukuran gambar sebelum
            System.out.println("2. Sebelum: " + String.format("%.2f KB", Utils.getFileSize(inputPath) / 1024.0));

//            10. [OUTPUT] ukuran gambar setelah
            System.out.println("3. Sesudah: " + String.format("%.2f KB", Utils.getFileSize(outputPath) / 1024.0));

//            11. [OUTPUT] persentase kompresi
            System.out.println("4. Persentase Kompresi: " + String.format("%.2f", (1 - Utils.getFileSize(outputPath) / (double) Utils.getFileSize(inputPath)) * 100) + "%");

//            12. [OUTPUT] kedalaman pohon
            System.out.println("5. Kedalaman Pohon: " + tree.maxDepth());

//            13. [OUTPUT] banyak simpul pada pohon
            System.out.println("6. Jumlah Simpul: " + tree.leafCount());
//            14. [OUTPUT] gambar hasil kompresi pada alamat yang sudah ditentukan
            System.out.println("7. Gambar Tersimpan: " + outputPath);

//            15. [OUTPUT] GIF proses kompresi pada alamat yang sudah ditentukan (bonus)
            if (generateGif) {
                System.out.println("8. GIF Tersimpan: " + gifOutputPath);
            }
            else {
                System.out.println("8. GIF tidak disimpan!");
            }


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
            lowerThreshold = 0;
            upperThreshold = 128*128;
        }
        else if (errorMethod == 2 || errorMethod == 3) {
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
        double inputSize = inputFile.length();
        Quadtree tree = null;
        int i;
        for (i = 0; i < 100; i++) {
            if (tree != null) {
                tree = null;
                System.gc();
            }
            tree = new Quadtree(imageArray, 0, 0, imageArray[0].length, imageArray.length, threshold, minBlockSize, errorMethod, 1);
            try {
                Utils.saveQuadtreeAsImage(tree, imageArray[0].length, imageArray.length, outputPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            double compressionRatio = 1 - Utils.getFileSize(outputPath) / inputSize;
            if (Math.abs(compressionRatio - targetCompression) < 0.003) {
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
        }
        return new BinSearchResult(threshold, tree);
    }



}
