package apps.sai.com.imageresizer.data;

import androidx.annotation.NonNull;

/**
 * Created by sailesh on 15/01/18.
 */

public class ResolutionInfo{
    private int width,height;
    private int percentageOfOriginal;
    private String formatedString ;
    private boolean percentageSelected, preResolutionSelected;
    private boolean aspect;


    public ResolutionInfo(){
       System.out.print("" +
               "");
    }
    public boolean isAspect() {
        return aspect;
    }

    public void setAspect(boolean aspect) {
        this.aspect = aspect;
    }

    public String getFormatedString() {
        return formatedString;
    }

    public void setFormatedString(String formatedString) {
        this.formatedString = formatedString;
    }

    public int getWidth() {
        return width;
    }

    public boolean isPreResolutionSelected() {
        return preResolutionSelected;
    }

    public void setPercentageSelected(boolean percentageSelected) {
        this.percentageSelected = percentageSelected;
    }

    public void setPreResolutionSelected(boolean preResolutionSelected) {
        this.preResolutionSelected = preResolutionSelected;
    }

    public boolean isPercentageSelected() {
        return percentageSelected;
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

    public int getPercentageOfOriginal() {
        return percentageOfOriginal;
    }

    public void setPercentageOfOriginal(int percentageOfOriginal) {
        this.percentageOfOriginal = percentageOfOriginal;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ResolutionInfo){
            ResolutionInfo resolutionInfo = (ResolutionInfo) obj;
            return  resolutionInfo.getWidth()==width && resolutionInfo.getHeight() ==height;
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return formatedString;
    }

    @Override
    public int hashCode() {
        int result =17;
        result =result*31 + width;
        result =result*31 + height;
        return result;
    }
}
