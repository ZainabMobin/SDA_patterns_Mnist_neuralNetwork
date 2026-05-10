/// (ideally if a folderpath is give to it, it must work with that as well by checking the file types in it). 
// Checks file extensions in the provided data path, creates relevant adapters for corresponding file types and 
// combines image results to send back to the NeuralNetrokr class.

package data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import data.adapters.CSVAdapter;
import data.adapters.BinaryAdapter;

/**
 *  ADAPTER HANDLER - Folder scanner & multi-adapter manager
 * Scans folder for heterogeneous file types, creates adapters, combines results
 */
public class AdapterHandler implements ReaderInterface {

    // Map to store adapter instances by file type (persistent across calls)
    private Map<String, Object> adapters;

    // Detected files by type (resets per readData call)
    private Map<String, List<String>> filesByType;

    public AdapterHandler() {
        this.adapters = new HashMap<>();
    }


    //Dynamically detects file types and creates adapters if needed
   
    @Override
    public List<Image> readData(String path) {
        // Reset files map for this call (but keep adapters persistent)
        this.filesByType = new HashMap<>();

        System.out.println(" AdapterHandler.readData() called with path: " + path);

        //  Detect if path is file or folder
        File pathFile = new File(path);

        if (pathFile.isDirectory()) {
            //  It's a folder - scan it
            System.out.println(" Detected: FOLDER");
            scanFolder(path);
        } else if (pathFile.isFile()) {
            //  It's a single file - treat it as a file
            System.out.println(" Detected: SINGLE FILE");
            scanSingleFile(path);
        } else {
            throw new IllegalArgumentException(" Path does not exist: " + path);
        }

        //  Validate at least one file found
        if (filesByType.isEmpty()) {
            throw new IllegalArgumentException(" No CSV or Binary files found in: " + path);
        }

        System.out.println(" Detected file types: " + filesByType.keySet() + "\n");

        //  Create adapters for detected file types (if not already created)
        createAdapters();

        //  Read and combine data from all files
        return readAndCombineData();
    }

    
    //  Scan folder and categorize files by type
    private void scanFolder(String folderPath) {
        File folder = new File(folderPath);

        System.out.println(" Scanning folder: " + folderPath);

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException(" Folder is empty: " + folderPath);
        }

        //  Categorize files by extension
        for (File file : files) {
            if (!file.isFile()) continue; //  Skip directories

            String fileName = file.getName().toLowerCase();
            String fileType = detectFileType(fileName); //  Detect type

            if (fileType != null) {
                //  Add to appropriate category
                filesByType.computeIfAbsent(fileType, k -> new ArrayList<>())
                           .add(file.getAbsolutePath());

                System.out.println("   Found " + fileType.toUpperCase() + ": " + fileName);
            }
        }
    }

    //Handle single file (add it to filesByType)
    private void scanSingleFile(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName().toLowerCase();
        String fileType = detectFileType(fileName); //  Detect type

        if (fileType == null) {
            throw new IllegalArgumentException(" Unknown file type: " + fileName);
        }

        //  Add single file to appropriate category
        filesByType.computeIfAbsent(fileType, k -> new ArrayList<>())
                   .add(file.getAbsolutePath());

        System.out.println("   Found " + fileType.toUpperCase() + ": " + fileName);
    }

    
    //  Detect file type by extension
    private String detectFileType(String fileName) {
        if (fileName.endsWith(".csv")) {
            return "csv"; //  CSV file
        } else if (fileName.endsWith(".gzip") || fileName.endsWith(".gz") || fileName.endsWith(".bin") || fileName.endsWith(".binary")) {
            return "binary"; //  Binary file
        }
        return null; //  Unknown type
    }

    
    //  Create adapter instances for detected file types (only if not already created)
    private void createAdapters() {
        System.out.println(" Creating adapters for detected file types...\n");

        //  CSV adapter
        if (filesByType.containsKey("csv") && !adapters.containsKey("csv")) {
            adapters.put("csv", new CSVAdapter()); //  Create CSV adapter
            System.out.println(" CSVAdapter created (uses CSVReader singleton)");
        }

        //  Binary adapter
        if (filesByType.containsKey("binary") && !adapters.containsKey("binary")) {
            adapters.put("binary", new BinaryAdapter()); //  Create Binary adapter
            System.out.println(" BinaryAdapter created (uses BinaryReader singleton)");
        }

        System.out.println();
    }

    //  Read data from all detected files and combine into one list
    private List<Image> readAndCombineData() {
        List<Image> combinedData = new ArrayList<>(); // Final combined list

        System.out.println(" Reading data from all detected files...\n");

        //  Read CSV files
        if (filesByType.containsKey("csv")) {
            CSVAdapter csvAdapter = (CSVAdapter) adapters.get("csv");
            List<String> csvFiles = filesByType.get("csv");

            for (String csvFile : csvFiles) {
                System.out.println("   Reading: " + csvFile);
                List<Image> csvData = csvAdapter.readData(csvFile); //  Read via adapter
                combinedData.addAll(csvData); //  Add to combined list
                System.out.println("     Loaded " + csvData.size() + " images\n");
            }
        }

        //  Read Binary files
        if (filesByType.containsKey("binary")) {
            BinaryAdapter binAdapter = (BinaryAdapter) adapters.get("binary");
            List<String> binFiles = filesByType.get("binary");
            // when label file encountered, binAdapter.readData(labelFile) returns an empty list. 
            // When it encounters the image file, the adapter returns the fully labelled list.
            for (String binFile : binFiles) {
                List<Image> binData = binAdapter.readData(binFile);
                combinedData.addAll(binData);
            }

            for (String binFile : binFiles) {
                System.out.println("    Reading: " + binFile);
                List<Image> binData = binAdapter.readData(binFile); //  Read via adapter
                combinedData.addAll(binData); //  Add to combined list
                System.out.println("     Loaded " + binData.size() + " images\n");
            }
        }

        //  Final summary
        System.out.println(" All data combined: " + combinedData.size() + " total images\n");
        return combinedData; //  Return combined list
    }

    //  Print summary of detected files (for debugging)
    public void printSummary() {
        System.out.println("  AdapterHandler Summary:");
        System.out.println("   File types detected:");

        for (Map.Entry<String, List<String>> entry : filesByType.entrySet()) {
            System.out.println("    - " + entry.getKey().toUpperCase() + ": " + entry.getValue().size() + " files");
        }

        System.out.println("   Adapters created:");
        for (String type : adapters.keySet()) {
            System.out.println("    - " + type + "Adapter (singleton reader)");
        }
    }

    //  Get specific adapter by type, return Adapter instance or null if not found
    public Object getAdapter(String fileType) {
        return adapters.get(fileType); //  Return adapter or null
    }
}