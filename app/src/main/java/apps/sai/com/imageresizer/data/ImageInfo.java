package apps.sai.com.imageresizer.data;

import android.net.Uri;

/**
 * Created by sailesh on 14/01/18.
 */

public  class ImageInfo {
    private long fileSize;//bytes
    private Uri absoluteFilePathUri;
    private Uri imageUri;
    private String formattedResolution;
    private DataFile dataFile;
    private boolean deleted;
    private boolean saved =true;
    private Uri tobeDeletedUri;
    private boolean isAd;
    private String error;
    private Uri originalContentUri;

    public boolean isAd() {
        return isAd;
    }

    public void setAd(boolean ad) {
        isAd = ad;
    }

    public void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
    }

    public DataFile getDataFile() {
        return dataFile;
    }

    public String getFormattedResolution() {
        return formattedResolution;
    }

    public void setFormattedResolution(String formattedResolution) {
        this.formattedResolution = formattedResolution;
    }

    public Uri getTobeDeletedUri() {
        return tobeDeletedUri;
    }

    public void setTobeDeletedUri(Uri tobeDeletedUri) {
        this.tobeDeletedUri = tobeDeletedUri;
    }

    public String getFormatedFileSize() {
        return formatedFileSize;
    }

    public void setFormatedFileSize(String formatedFileSize) {
        this.formatedFileSize = formatedFileSize;
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof ImageInfo){
            ImageInfo imageInfo = (ImageInfo) obj;
            if(imageInfo.imageUri!=null && imageUri!=null){
                return imageUri.equals(imageInfo.imageUri);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if(imageUri!=null){
            return  imageUri.hashCode();
        }
        return super.hashCode();
    }

    private String formatedFileSize="";
    private String absoluteThumbFilePath;
    private  int width,height;

    public String getAbsoluteThumbFilePath() {
        return absoluteThumbFilePath;
    }

    public void setAbsoluteThumbFilePath(String absoluteThumbFilePath) {
        this.absoluteThumbFilePath = absoluteThumbFilePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Uri getAbsoluteFilePathUri() {
        return absoluteFilePathUri;
    }

    public void setAbsoluteFilePath(Uri absoluteFilePathUri) {
        this.absoluteFilePathUri = absoluteFilePathUri;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    Uri processedUri;

    public Uri getProcessedUri() {
        return processedUri;
    }

    public void setProcessedUri(Uri processedUri) {
        this.processedUri = processedUri;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public Uri getOriginalContentUri() {
        return originalContentUri;
    }

    public void setOriginalContentUri(Uri originalContentUri) {
        this.originalContentUri = originalContentUri;
    }
}
