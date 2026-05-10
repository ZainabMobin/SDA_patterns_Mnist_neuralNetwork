package data.adapters;

import java.util.List;
import data.Image;
import data.ReaderInterface;


public abstract class ReaderAdapter implements ReaderInterface{

    @Override
    public abstract List< Image > readData( String path );
}