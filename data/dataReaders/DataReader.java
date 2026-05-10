package data.dataReaders;


import java.util.List;
import data.Image;
import data.ReaderInterface;

public abstract class DataReader implements ReaderInterface{
    protected final int dataLength = 784; // 28x28 pixels for MNIST

    @Override
    public abstract List< Image > readData( String path ); //abstract function to be implemented by all data readers (csv, json, binary, etc)

}