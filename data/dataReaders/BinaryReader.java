package data.dataReaders;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import data.Image;

public class BinaryReader extends DataReader
{
    private static BinaryReader instance; // singleton instance
    private BinaryReader() {} // private constructor to prevent instantiation

    public static BinaryReader getInstance() // method to get singleton instance
    {
        if ( instance == null ) {
            instance = new BinaryReader();
        }
        return instance;
    }

    // Read image file format
    private List<Image> readImageFile(DataInputStream reader, String path) throws IOException {
        List<Image> data = new ArrayList<>();
        
        // Read image header
        int numSamples = readInt(reader);
        int rows = readInt(reader);
        int cols = readInt(reader);

        if (rows != 28 || cols != 28) {
            throw new IllegalArgumentException("Expected 28x28 images, got " + rows + "x" + cols);
        }

        byte[] pixelBuffer = new byte[super.dataLength];

        // Read all images
        for (int i = 0; i < numSamples; i++) {
            reader.readFully(pixelBuffer);
            
            // Normalize pixels to 
            double[] pixels = new double[super.dataLength];
            for (int j = 0; j < super.dataLength; j++) {
                pixels[j] = (pixelBuffer[j] & 0xFF) / 255.0;
            }

            // Create image with placeholder label (will be set by labels file)
            data.add(new Image(pixels, 0));
        }

        System.out.println(" Binary GZip - Images loaded: " + data.size());
        return data;
    }

    @Override
    public List<Image> readData(String path) {
        try (InputStream fileInputStream = new FileInputStream(path);
            BufferedInputStream bufferedStream = new BufferedInputStream(fileInputStream);
            GZIPInputStream gzipStream = new GZIPInputStream(bufferedStream);
            DataInputStream reader = new DataInputStream(gzipStream)) {

            int magic = readInt(reader);
            if (magic != 0x00000803) {
                throw new IllegalArgumentException(
                    "Expected image file (magic 0x00000803) but got magic " + magic + ". Use readLabels() for label files."
                );
            }
            return readImageFile(reader, path);

        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading binary image file: " + path, e);
        }
    }

    // Reads a label file and returns the labels as an int array.
     
    public int[] readLabels(String labelPath) throws IOException {
        try (InputStream fis = new FileInputStream(labelPath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            GZIPInputStream gzis = new GZIPInputStream(bis);
            DataInputStream dis = new DataInputStream(gzis)) {

            int magic = readInt(dis);
            if (magic != 0x00000801) {
                throw new IOException("Not a valid label file, magic number: " + magic);
            }
            int numLabels = readInt(dis);
            int[] labels = new int[numLabels];
            for (int i = 0; i < numLabels; i++) {
                labels[i] = dis.readUnsignedByte();
            }
            return labels;
        }
    }


    // Helper method to read 4-byte big-endian integer
    private int readInt(DataInputStream dis) throws IOException {
        byte[] bytes = new byte[4];
        dis.readFully(bytes);
        return ((bytes[0] & 0xFF) << 24) | 
               ((bytes[1] & 0xFF) << 16) | 
               ((bytes[2] & 0xFF) << 8) | 
               (bytes[3] & 0xFF);
    }

}
