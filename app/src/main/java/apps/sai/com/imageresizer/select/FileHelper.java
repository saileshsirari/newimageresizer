package apps.sai.com.imageresizer.select;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class FileHelper {

    private final static String TAG = "FileHelper";

    /**
     * The root directory
     */
    public static final String ROOT_DIRECTORY = "/";


    /**
     * Method that check if a file is a symbolic link.
     *
     * @param file File to check
     * @return boolean If file is a symbolic link
     * @throws IOException If real file couldn't be checked
     */
    public static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            return false;
        }
        String absPath = file.getAbsolutePath();
        String canonPath = file.getCanonicalPath();

        return !(TextUtils.isEmpty(absPath) || TextUtils.isEmpty(canonPath)) && absPath.compareTo(canonPath) != 0;
    }

    /**
     * Method that resolves a symbolic link to the real file or directory.
     *
     * @param file File to check
     * @return File The real file or directory
     * @throws IOException If real file couldn't be resolved
     */
    public static File resolveSymlink(File file) throws IOException {
        return file.getCanonicalFile();
    }

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

    /**
     * Resolves the /storage/emulated/legacy paths to
     * their true folder path representations. Required
     * for Nexii and other devices with no SD card.
     *
     * @return The true, resolved file path to the input path.
     */
    @SuppressLint("SdCardPath")
    public static String getPath(File file) {

        if (file == null) {
            return null;
        }

        String filePath = file.getAbsolutePath();

        try {
            if (isSymlink(file)) {
                file = resolveSymlink(file);
                filePath = file.getAbsolutePath();
            }
        } catch (IOException ignored) {

        }

        if (!TextUtils.isEmpty(filePath) && filePath.equals("/storage/emulated/0") ||
                filePath.equals("/storage/emulated/0/") ||
                filePath.equals("/storage/emulated/legacy") ||
                filePath.equals("/storage/emulated/legacy/") ||
                filePath.equals("/storage/sdcard0/") ||
                filePath.equals("/storage/sdcard1/") ||
                filePath.equals("/storage/sdcard2/") ||
                filePath.equals("/sdcard") ||
                filePath.equals("/sdcard2") ||
                filePath.equals("/mnt/sdcard2/") ||
                filePath.equals("/sdcard3") ||
                filePath.equals("/mnt/sdcard3/")) {

            filePath = Environment.getExternalStorageDirectory().toString();
        }

        return filePath;
    }

    /**
     * Gets a formatted, human readable file size String
     *
     * @param size long, the size of the file in bytes
     * @return String a formatted, human readable file size
     */
    public static String getHumanReadableSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static long getDuration(Context context, BaseFileObject baseFileObject) {
        int duration = 0;
        if (baseFileObject != null && !TextUtils.isEmpty(baseFileObject.path)) {
            Uri uri = Uri.parse(baseFileObject.path);
            if (uri != null) {
                MediaPlayer mediaPlayer = MediaPlayer.create(context, uri);
                if (mediaPlayer != null) {
                    duration = mediaPlayer.getDuration();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }
            }
        }
        return duration;
    }

    /**
     * Recursively collects all the files for the given directory and
     * all of its sub-directories. Must be called Asynchronously.
     *
     * @param file      the File to retrieve the song Id's from
     * @param recursive whether to recursively check the sub-directories for song Id's
     * @return long[] a list of the songId's for the given fileObject's directory & sub-directories
     */
    public static Observable<List<String>> getPathList(final File file, final boolean recursive, final boolean inSameDir) {
        return Observable.fromCallable(
                () -> walk(file, new ArrayList<>(), recursive, inSameDir))
                .subscribeOn(Schedulers.io());
    }

    /**
     * Recursively 'walks' the files subdirectories, gathering a list of paths.
     *
     * @param root      the root file to walk
     * @param paths     the paths will be added to this List
     * @param recursive whether to recursively walk subdirectories
     * @param inSameDir whether files in the same dir as root should be included
     * @return a List of paths
     */
    private static List<String> walk(File root, final List<String> paths, final boolean recursive, final boolean inSameDir) {

        if (inSameDir) {
            root = root.getParentFile();
        }

        if (!root.isDirectory()) {
            paths.add(root.getAbsolutePath());
            return paths;
        }

        File[] list = root.listFiles(getAudioFilter());
        if (list != null) {
            for (File f : list) {
                if (f.isDirectory()) {
                    if (recursive) {
                        walk(f, paths, true, false);
                    }
                } else {
                    paths.add(f.getAbsolutePath());
                }
            }
        }

        return paths;
    }

    /**
     * Delete a File recursively.
     *
     * @param file the File to delete
     * @return true if the deletion was successful
     */
    public static boolean deleteFile(File file) {
        return deleteRecursive(file);
    }

    /**
     * Recursively delete a File
     *
     * @param fileOrDirectory the file or directory to delete
     * @return true id the deletion was successful
     */
    private static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory == null) {
            return false;
        } else if (fileOrDirectory.isDirectory()) {
            File[] fileList = fileOrDirectory.listFiles();
            if (fileList != null) {
                for (File child : fileList)
                    deleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }

    /**
     * An array of accepted/supported audio extensions.
     */
    public static String[] sExtensions = new String[]{
            "mp3", "3gp", "mp4", "m4a",
            "aac", "ts", "flac", "mid",
            "xmf", "mxmf", "midi", "rtttl",
            "rtx", "ota", "imy", "ogg",
            "mkv", "wav"
    };

    /**
     * An {@link FileFilter} which only accepts directories & supported audio filetypes, based on extension
     */
    public static FileFilter getAudioFilter() {
        return file -> {
            if (!file.isHidden() && file.canRead()) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    String ext = getExtension(file.getName());
                    for (String allowedExtension : sExtensions) {
                        if (!TextUtils.isEmpty(ext)) {
                            if (allowedExtension.equalsIgnoreCase(ext)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        };
    }
}
