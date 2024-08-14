package apps.sai.com.imageresizer.util.algo; /******************************************************************************
 *  Compilation:  javac ShowSeams.java
 *  Execution:    java ShowSeams input.png
 *  Dependencies: SeamCarver.java SCUtility.java
 *
 *  Read image from file specified as command line argument. Show 3 images 
 *  original image as well as horizontal and vertical seams of that image.
 *  Each image hides the previous one - drag them to see all three.
 *
 ******************************************************************************/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

import java.util.Scanner;


public class ShowSeams {

    private static Bitmap showHorizontalSeam(SeamCarver sc) {
        Bitmap picture = SCUtility.toEnergyPicture(sc);
        int[] horizontalSeam = sc.findHorizontalSeam();
        Bitmap overlay = SCUtility.seamOverlay(picture, true, horizontalSeam);

        return overlay;

    }


    private static Bitmap showVerticalSeam(SeamCarver sc) {

        Bitmap picture = SCUtility.toEnergyPicture(sc);
        int[] verticalSeam = sc.findVerticalSeam();
        printArray(verticalSeam);
        Bitmap overlay = SCUtility.seamOverlay(picture, false, verticalSeam);
//        overlay.show();
        return overlay;
    }
    private static int hex2Rgb(String colorStr) {
        return  Color.rgb(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }

    private static void printArray(int a[]){
        StdOut.print("[ ");
        for(int i=0;i<a.length;i++){
            StdOut.print(a[i]+" ");
        }
        StdOut.println("] ");
    }

    public static void showSeam(Context context , Bitmap args, ImageView imageView) {


        if(false){
            StdOut.println("Main ");

            String picHex ="#050404 #020509 #030804 #000301 #080308 #020003\n" +
                    "#070101 #080002 #010009 #040600 #080707 #020601\n" +
                    "#000309 #020703 #070802 #060105 #000906 #020600\n" +
                    "#070800 #000701 #000800 #070307 #030109 #080904\n" +
                    "#070606 #050506 #000206 #060505 #020603 #020706\n" +
                    "#090605 #010205 #090004 #040702 #040803 #040409";
            Scanner scanner = new Scanner(picHex);
            Bitmap picture =  Bitmap.createBitmap(6,6, Bitmap.Config.ARGB_8888);

             while (scanner.hasNext()){
            String rowHex = scanner.nextLine();

            String [] rowData =rowHex.split(" ");


//            StdOut.println("xx = "+rowData[rowData.length-1]);
            for(int j=0;j<6;j++){
            for (int i=0;i<rowData.length;i++) {


                int color = hex2Rgb(rowData[i].trim());
                picture.setPixel(i, j, color);
            }

            }
            }

//            EdgeWeightedGraph


            // Picture picture = new Picture(args[0]);
            StdOut.printf("image is %d columns by %d rows\n", picture.getWidth(), picture.getHeight());
            SeamCarver carver = new SeamCarver(picture);
//             picture.show();
            double energy = carver.energy(1, 2)  ;//for each x and y
            StdOut.println("energy = "+energy);
//            carver.removeVerticalSeam(carver.findVerticalSeam()) ;//


            printArray(carver.findHorizontalSeam ()) ;//


            StdOut.println("energy = "+energy);

//            webviewOrg.setImageBitmap(showVerticalSeam(carver));
//
            return;
        }

        try {
//            final InputStream imageStream = context.getContentResolver().openInputStream(Uri.parse(args));
            Bitmap picture = args;
//                    BitmapFactory.decodeStream(imageStream);
//            Bitmap picture = Bitmap.createBitmap(args[0]);
            StdOut.printf("image is %d columns by %d rows\n", picture.getWidth(), picture.getHeight());
            //picture.show();
            SeamCarver sc = new SeamCarver(picture);


            StdOut.printf("Displaying vertical seam calculated.\n");
            Bitmap returnBitmap = null;
//            returnBitmap = showVerticalSeam(sc);
            StdOut.printf("Displaying horizontal seam calculated.\n");
            returnBitmap = showHorizontalSeam(sc);

            imageView.setImageBitmap(returnBitmap);


        }catch (Exception fe){
            fe.printStackTrace();
        }

    }

}
