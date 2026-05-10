package data.adapters;

import java.util.List;
import data.Image;
import data.dataReaders.DataReader;
import data.dataReaders.CSVReader;

public class CSVAdapter extends ReaderAdapter{

    private static DataReader readerAdaptee; 

    public CSVAdapter(){
        //create CSVDatareader
        readerAdaptee = CSVReader.getInstance();
    }

    @Override
    public List< Image > readData( String path ) {
        return readerAdaptee.readData( path );
    }
}
