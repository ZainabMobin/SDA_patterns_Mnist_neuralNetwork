## Applying Singleton and Adapter Pattern 

**Original Project Link:** 
https://github.com/PAKIWASI/Neural-Network-from-Scratch-MNIST-JAVA

**Used convention:**

- classes: CamelCase (NeuralNetwork, ReaderAdapter)
- folders: camelCase with first letter lowercase (dataReaders, neuralNetwork, layers)
- methods: camelCase with first letter lowercase (readData(), train())

**Binary dataset**

 Obtained from: https://github.com/fgnt/mnist

## AdapterFactory pattern on ReaderAdapter

Management of individual adapters is handled by the AdapterFactory(AdapterHandler.java), which creates and provides access to the appropriate adapter based on the file type. This allows for a clean separation of concerns and promotes code reusability.

**AdapterHandler.java:** 

- Singleton reuse - multiple CSV files use same CSVReader.getInstance()
- Lazy adapter creation - only creates adapters for file types found
- Seamless combining - NeuralNetwork gets one clean list, doesn't know about files
- Stateful handler - stores adapters and file lists as instance attributes
- Folder scanning - detects heterogeneous files (CSV + Binary in same folder)

## Singleton pattern on MatrixOperations class

**Changes made:**
Made public static methods of MatrixOperations class public only, and loaded MatrixOperation object in Layer class as a protected attribute for child access.

## Applied UML Diagrams

**Singleton(DataReader children)**

![Singleton](img/reader_singleton.drawio.png)


**Adapter(ReaderAdapter children) and AdapterFactory(ReaderAdapter)**

![Adapter](img/adapter_singleton.drawio.png)


**Singleton (Mathematical Operations)**

![Singleton](img/singleton.drawio.png)
