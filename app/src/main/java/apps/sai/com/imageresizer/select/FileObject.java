package apps.sai.com.imageresizer.select;


import apps.sai.com.imageresizer.ImageResizeApplication;
import apps.sai.com.imageresizer.listener.FileType;

public class FileObject extends BaseFileObject {

    public String extension;

//    public TagInfo tagInfo;

    private long duration = 0;

    public FileObject() {
        this.fileType = FileType.FILE;
    }

    public String getTimeString() {
        if (duration == 0) {
            duration = FileHelper.getDuration(ImageResizeApplication.getInstance(), this);
        }
        return "";
    }

    @Override
    public String toString() {
        return "FileObject{" +
                "extension='" + extension + '\'' +
                ", size='" + size + '\'' +
                "} " + super.toString();
    }
}
