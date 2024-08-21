package apps.sai.com.imageresizer.select;

import java.io.File;

public class FileHelper {

    private final static String TAG = "FileHelper";

    /**
     * The root directory
     */
    public static final String ROOT_DIRECTORY = "/";


    /**
     * Returns the name of a string, excluding the extension
     *
     * @param name the name (path) of the file
     * @return the name of the file, excluding the extension
     */
    public static String getName(String name) {
        String ext = getExtension(name);
        if (ext == null) {
            return name;
        }
        return name.substring(0, name.length() - ext.length() - 1);
    }

    /**
     * Returns the extension of the file
     *
     * @param name the File to retrieve the extension from
     * @return String the extension of the file
     */
    public static String getExtension(String name) {
        final char dot = '.';
        int pos = name.lastIndexOf(dot);
        if (pos == -1 || pos == 0) { // Hidden files don't have extensions
            return null;
        }

        return name.substring(pos + 1);
    }


    /**
     * Returns true if this OldFileObject can has read & write access
     *
     * @param file the File to check
     * @return boolean true if this OldFileObject can has read & write access
     */
    public static boolean canReadWrite(File file) {
        return file.canRead() && file.canWrite();
    }

}
