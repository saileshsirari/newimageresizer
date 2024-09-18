package apps.sai.com.imageresizer.data;

import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * Created by sailesh on 30/12/17.
 */

public  class DataFile implements Comparable<DataFile> {

    enum FILE_TYPE{
        nonsharable,
        sharable;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    private String name;
    private Uri uri;


    @Override
    public boolean equals(Object obj) {

        if(obj instanceof DataFile dataFile){
            return dataFile.getName().equals(getName());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public int compareTo(@NonNull DataFile o) {
        return o.getName().compareTo(getName());
    }
}
