package apps.sai.com.imageresizer.util.algo;

import android.graphics.Bitmap;
import android.graphics.Color;

;
/******************************************************************************
 *  Compilation:  javac SCUtility.java
 *  Execution:    none
 *  Dependencies: SeamCarver.java
 *
 *  Some utility functions for testing SeamCarver.java.
 *
 ******************************************************************************/

public class SCUtility {


    // create random width-by-height array of tiles
    public static Bitmap randomPicture(int width, int height) {
        Bitmap picture =  Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int r = StdRandom.uniform(255);
                int g = StdRandom.uniform(255);
                int b = StdRandom.uniform(255);
                int a = StdRandom.uniform(255);
//                Color color = new Color();
                picture.setPixel(col,row,Color.rgb(r,g,b));
//                picture.set(col, row, color);
            }
        }
        return picture;
    }


    public static double[][] toEnergyMatrix(SeamCarver sc) {
        double[][] returnDouble = new double[sc.width()][sc.height()];
        for (int col = 0; col < sc.width(); col++)
            for (int row = 0; row < sc.height(); row++)
                returnDouble[col][row] = sc.energy(col, row);
    
        return returnDouble;        
    }

    // displays grayvalues as energy (converts to picture, calls show)
    public static Bitmap showEnergy(SeamCarver sc) {
       return doubleToPicture(toEnergyMatrix(sc));
    }

    public static Bitmap toEnergyPicture(SeamCarver sc) {
        double[][] energyMatrix = toEnergyMatrix(sc);
        return doubleToPicture(energyMatrix);
    }

    // converts a double matrix of values into a normalized picture
    // values are normalized by the maximum grayscale value (ignoring border pixels)
    public static Bitmap doubleToPicture(double[][] grayValues) {

        // each 1D array in the matrix represents a single column, so number
        // of 1D arrays is the width, and length of each array is the height
        int width = grayValues.length;
        int height = grayValues[0].length;

        Bitmap picture = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // maximum grayscale value (ignoring border pixels)
        double maxVal = 0;
        for (int col = 1; col < width-1; col++) {
            for (int row = 1; row < height-1; row++) {
                if (grayValues[col][row] > maxVal)
                    maxVal = grayValues[col][row];
             }
        }
            
        if (maxVal == 0)
            return picture; //return black picture

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                float normalizedGrayValue = (float) grayValues[col][row] / (float) maxVal;
                if (normalizedGrayValue >= 1.0f) normalizedGrayValue = 1.0f;
//                picture.setPixel(col, row,Color.RED);

                picture.setPixel(col, row, getIntFromColor(normalizedGrayValue, normalizedGrayValue, normalizedGrayValue));
            }
        }

        return picture;
    }
    private static int getIntFromColor(float Red, float Green, float Blue){
        int R = Math.round(255 * Red);
        int G = Math.round(255 * Green);
        int B = Math.round(255 * Blue);

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return 0xFF000000 | R | G | B;
    }

    // This method is useful for debugging seams. It overlays red
    // pixels over the calculate seam. Due to the lack of a copy
    // constructor, it also alters the original picture.
    public static Bitmap seamOverlay(Bitmap picture, boolean horizontal, int[] seamIndices) {
        Bitmap overlaid = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
        int width = picture.getWidth();
        int height = picture.getHeight();

        for (int col = 0; col < width; col++)
            for (int row = 0; row < height; row++) {
//                overlaid.set(col, row, picture.get(col, row));

                overlaid.setPixel(col, row, picture.getPixel(col, row));
            }
        

        //if horizontal seam, then set one pixel in every column

        try {
            if (horizontal) {
                for (int col = 0; col < width; col++)
//                overlaid.set(col, seamIndices[col], Color.RED);
                    overlaid.setPixel(col, seamIndices[col], Color.RED);

            } else { // if vertical, put one pixel in every rows
                for (int row = 0; row < height; row++) {
//                overlaid.set(seamIndices[row], row, Color.RED);

                    overlaid.setPixel(seamIndices[row], row, Color.RED);

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return overlaid;
    }

}
