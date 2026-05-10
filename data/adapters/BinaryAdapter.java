// package data.adapters;

// import java.io.BufferedInputStream;
// import java.io.DataInputStream;
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.util.List;
// import java.util.zip.GZIPInputStream;

// import data.Image;
// import data.dataReaders.BinaryReader;

// public class BinaryAdapter extends ReaderAdapter{

//     private static BinaryReader readerAdaptee; 

//     public BinaryAdapter(){
//         readerAdaptee = BinaryReader.getInstance();
//     }

//     @Override
//     public List<Image> readData(String folderPath) {
//         File folder = new File(folderPath);
//         if (!folder.isDirectory()) {
//             throw new IllegalArgumentException("Expected a folder, but got: " + folderPath);
//         }

//         // We'll look for any .gz files in the folder
//         File[] gzFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".gz"));
//         if (gzFiles == null || gzFiles.length == 0) {
//             throw new IllegalArgumentException("No .gz files found in " + folderPath);
//         }

//         String imageFile = null;
//         String labelFile = null;

//         // Identify files by magic number alone – NO filename pattern assumptions
//         for (File f : gzFiles) {
//             int magic = readMagicNumber(f);
//             if (magic == 0x00000803) {          // 2051 image file
//                 if (imageFile != null) {
//                     throw new IllegalArgumentException("Multiple image files found.");
//                 }
//                 imageFile = f.getAbsolutePath();
//             } else if (magic == 0x00000801) {   // 2049 label file
//                 if (labelFile != null) {
//                     throw new IllegalArgumentException("Multiple label files found.");
//                 }
//                 labelFile = f.getAbsolutePath();
//             }
//         }

//         if (imageFile == null) {
//             throw new IllegalArgumentException("No image file found (magic 0x00000803) in " + folderPath);
//         }
//         if (labelFile == null) {
//             throw new IllegalArgumentException("No label file found (magic 0x00000801) in " + folderPath);
//         }

//         // 1) Load all images (placeholder label = 0)
//         List<Image> images = readerAdaptee.readData(imageFile);  // uses the already existing readImageFile logic

//         // 2) Load the raw labels
//         int[] labels;
//         try {
//             labels = readerAdaptee.readLabels(labelFile);
//         } catch (IOException e) {
//             throw new IllegalArgumentException("Failed to read labels from " + labelFile, e);
//         }

//         // 3) Sanity check
//         if (images.size() != labels.length) {
//             throw new IllegalArgumentException(
//                 "Mismatch: " + images.size() + " images vs " + labels.length + " labels."
//             );
//         }

//         // 4) Assign the real label to every image
//         for (int i = 0; i < images.size(); i++) {
//             images.get(i).setLabel(labels[i]);
//         }

//         System.out.println("Binary Adapter: loaded " + images.size() +
//                            " images and assigned labels from folder " + folderPath);
//         return images;
//     }

//     // Helper to peek at the first 4 bytes (magic number) of a gzipped file
//     private int readMagicNumber(File gzFile) {
//         try (InputStream fis = new FileInputStream(gzFile);
//              BufferedInputStream bis = new BufferedInputStream(fis);
//              GZIPInputStream gzis = new GZIPInputStream(bis);
//              DataInputStream dis = new DataInputStream(gzis)) {
//             byte[] bytes = new byte[4];
//             dis.readFully(bytes);
//             return ((bytes[0] & 0xFF) << 24) |
//                    ((bytes[1] & 0xFF) << 16) |
//                    ((bytes[2] & 0xFF) << 8)  |
//                    (bytes[3] & 0xFF);
//         } catch (IOException e) {
//             throw new IllegalArgumentException("Cannot read magic number from " + gzFile, e);
//         }
//     }
// }

// BinaryAdapter.java
package data.adapters;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import data.Image;
import data.dataReaders.BinaryReader;

public class BinaryAdapter extends ReaderAdapter {

    private static BinaryReader readerAdaptee;

    public BinaryAdapter() {
        readerAdaptee = BinaryReader.getInstance();
    }

    @Override
    public List<Image> readData(String filePath) {
        File file = new File(filePath);
        if (!file.isFile()) {
            throw new IllegalArgumentException("Not a file: " + filePath);
        }

        // 1. Determine file type by magic number
        int magic = readMagicNumber(file);

        if (magic == 0x00000801) {                        // Label file
            System.out.println("BinaryAdapter: detected label file – will be paired with its image file, skipping standalone load.");
            return new ArrayList<>();                    // empty list – no junk data
        }

        if (magic != 0x00000803) {
            throw new IllegalArgumentException("Unknown binary file (magic " + magic + "): " + filePath);
        }

        // 2. It's an image file – load all images with placeholder labels
        List<Image> images = readerAdaptee.readData(filePath);  // now only works on image files

        // 3. Find matching label file in the same folder (magic‑number‑based)
        File folder = file.getParentFile();
        if (folder == null || !folder.isDirectory()) {
            System.out.println("No parent folder, labels not assigned.");
            return images;
        }

        File[] gzFiles = folder.listFiles(
            (dir, name) -> name.toLowerCase().endsWith(".gz"));
        if (gzFiles == null) {
            return images;
        }

        String labelFilePath = null;
        for (File f : gzFiles) {
            if (f.equals(file)) continue;
            if (readMagicNumber(f) == 0x00000801) {
                labelFilePath = f.getAbsolutePath();
                break;
            }
        }

        if (labelFilePath == null) {
            System.out.println("No label file found in folder – images keep placeholder label 0.");
            return images;
        }

        // 4. Read labels and assign
        int[] labels;
        try {
            labels = readerAdaptee.readLabels(labelFilePath);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read labels from " + labelFilePath, e);
        }

        if (images.size() != labels.length) {
            throw new IllegalArgumentException(
                "Mismatch: " + images.size() + " images vs " + labels.length + " labels."
            );
        }

        for (int i = 0; i < images.size(); i++) {
            images.get(i).setLabel(labels[i]);
        }

        System.out.println("BinaryAdapter: loaded " + images.size() +
                           " images and assigned labels from " + labelFilePath);
        return images;
    }

    // Helper to peek at the magic number without consuming the stream
    private int readMagicNumber(File gzFile) {
        try (InputStream fis = new FileInputStream(gzFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             GZIPInputStream gzis = new GZIPInputStream(bis);
             DataInputStream dis = new DataInputStream(gzis)) {
            byte[] bytes = new byte[4];
            dis.readFully(bytes);
            return ((bytes[0] & 0xFF) << 24) |
                   ((bytes[1] & 0xFF) << 16) |
                   ((bytes[2] & 0xFF) << 8)  |
                   (bytes[3] & 0xFF);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read magic number from " + gzFile, e);
        }
    }
}