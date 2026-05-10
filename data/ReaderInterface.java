//shared interface for all data readers (csv, json, binary, etc) to implement, with a common readData function that takes in a file path and returns a list of images
package data;


import java.util.List;

public interface ReaderInterface{

    //reads data from file
    List< Image > readData(String path);

}