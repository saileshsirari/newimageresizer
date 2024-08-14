package apps.sai.com.imageresizer.settings;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import java.io.File;

import apps.sai.com.imageresizer.ImageResizeApplication;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.FileApi;

/**
 * Created by sailesh on 01/03/18.
 */

public class SettingsManager {
    public static final String KEY_PREF_AUTO_SAVE = "pref_auto_save";
    public static final String KEY_PREF_RATE_US = "pref_rate_us";
    public static final String KEY_PREF_FOLDER_PATH = "pref_folder_path";
    public static final String KEY_PREF_FILE_EXTENSION ="pref_file_extension";
    public static final String KEY_PREF_SHARE_FEEDBACK ="pref_share_feedback";
    public static final String KEY_PREF_SHARE_APP ="pref_share_app";
    public static final String KEY_PREF_GRID_SIZE ="pref_grid_size";
    public static final String KEY_PREF_VERSION ="pref_about_build";
    public static final String KEY_PREF_MUSIC_APP ="pref_music_app";

    public static final String KEY_PREF_GRID_APPEARANCE ="pref_grid_appearance";

    private static final String KEY_UPGRADED = "pref_ad_upgraded";

    private   String PUBLIC_FOLDER_NAME ="ImageResizer" ;
    private static SettingsManager sInstance;
    private boolean legacyUpgraded;


    public static SettingsManager getInstance() {
        if (sInstance == null) {
            sInstance = new SettingsManager();
        }
        return sInstance;
    }

    private static final String KEY_LAUNCH_COUNT = "launch_count";

    public int getLaunchCount() {
        return getInt(KEY_LAUNCH_COUNT, 0);
    }

    public void incrementLaunchCount() {
        setInt(KEY_LAUNCH_COUNT, getLaunchCount() + 1);
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(ImageResizeApplication.getInstance());
    }
    private boolean getBool(@NonNull String key, boolean defaultValue) {
        return getSharedPreferences().getBoolean(key, defaultValue);
    }

    private void setInt(@NonNull String key, int value) {
        final SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private int getInt(@NonNull String key, int defaultValue) {
        return getSharedPreferences().getInt(key, defaultValue);
    }

    private void setBool(@NonNull String key, boolean value) {
        final SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    private void setString(@NonNull String key, String value) {
        final SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String getString(@NonNull String key, String defaultValue) {
        return getSharedPreferences().getString(key, defaultValue);
    }
    public boolean getAutoSaveImages() {
        return getBool(KEY_PREF_AUTO_SAVE, true);
    }
    public void setAutoSaveImages(boolean value) {

        setBool(KEY_PREF_AUTO_SAVE, value);
    }

    public String getFolderPath() {




        String path =  getString(KEY_PREF_FOLDER_PATH, null);

        if(path==null){
            //set defalt path
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),PUBLIC_FOLDER_NAME);
            file.mkdirs();
            return file.getPath();
        }
        return  path;
    }
    public void setFolderPath(String value) {

        setString(KEY_PREF_FOLDER_PATH, value);
        FileApi.myFolderPath = value;
    }
    public boolean isFileExtensionJpg() {




        String path =  getString(KEY_PREF_FILE_EXTENSION, DataApi.SUFFIX_JPG);


        return  path.equals(DataApi.SUFFIX_JPG);
    }
    public String getFileExtensionPref() {

      if(true){
          return DataApi.SUFFIX_JPG;
      }


        String path =  getString(KEY_PREF_FILE_EXTENSION, DataApi.SUFFIX_JPG);


        return  path;
    }
    public void setFileExtensionPref(String value) {

        setString(KEY_PREF_FILE_EXTENSION, value);
    }

    public boolean isJpg(String filename){
        return ((filename.toLowerCase().indexOf(DataApi.SUFFIX_JPEG) !=-1 ) || (filename.toLowerCase().indexOf(DataApi.SUFFIX_JPG) !=-1 ));
    }
    public int getGridSize() {

        String val =  getString(KEY_PREF_GRID_SIZE, "2");
        return Integer.valueOf(val);
    }

    public void setGridSize(String val) {

        setString(KEY_PREF_GRID_SIZE, val);
    }

    public void setKeyPrefGridAppearance(String val) {

        setString(KEY_PREF_GRID_APPEARANCE, val);
    }
    public int getGridAppearnece() {

        String val =  getString(KEY_PREF_GRID_APPEARANCE, "1");

        return Integer.valueOf(val);
    }
    public boolean isLegacyUpgraded() {

        return getBool(KEY_UPGRADED, false);
    }
    public  void setLegacyUpgraded(boolean value){
        setBool(KEY_UPGRADED,value);
    }

   /* public DataFile getCorrectExtension(DataFile dataFile) {

        if(dataFile.getName().indexOf("_thumb")!=-1){
            return  dataFile;
        }


        String ext = getFileExtensionPref();

        if(ext.equals(DataApi.SUFFIX_JPG) || ext.equals(DataApi.SUFFIX_JPEG)){
            if(dataFile.getName().endsWith(DataApi.SUFFIX_JPG ) || dataFile.getName().endsWith(DataApi.SUFFIX_JPEG)){
                return dataFile;
            }

            String name =dataFile.getName();
            name =name.substring(0,name.lastIndexOf("."));
            name = name + DataApi.SUFFIX_JPG;
            dataFile.setName(name);
        }
        if(ext.equals(DataApi.SUFFIX_PNG)){
            if(dataFile.getName().endsWith(DataApi.SUFFIX_PNG )){
                return  dataFile;
            }
            String name =dataFile.getName();
            name =name.substring(0,name.lastIndexOf("."));
            name = name + DataApi.SUFFIX_PNG;
            dataFile.setName(name);

        }
        return dataFile;
    }*/
}
