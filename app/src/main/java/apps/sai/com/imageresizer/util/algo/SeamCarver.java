package apps.sai.com.imageresizer.util.algo;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;


/**
 * Created by sailesh on 23/12/17.
 */

public class SeamCarver {

    final private static  double ENERGY_BOUNDARY = 1000.00;
    private Bitmap bitmap;
    private double[][] energy;
    private int rows, cols;
    private int [][] pictureData;
    // sequence of indices for vertical seam
    private STATE_SEAM lastState = STATE_SEAM.VERTICAL_SEAM;

    // create a seam carver object based on the given picture
    public SeamCarver(Bitmap bitmap) {

        if (bitmap == null) {
            throw new IllegalArgumentException();
        }
        this.bitmap = bitmap;

        int width = bitmap.getWidth();

        int height = bitmap.getHeight();

        rows = height;//rows are vertical
        cols = width;

//        StdOut.println(" rows =  "+rows +" cols = "+cols);
        energy = new double[rows][cols];

        pictureData = new int[rows][cols];

        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {

                pictureData[row][col] = bitmap.getPixel(col, row);
                pictureData[row][col] =unpackPixel(pictureData[row][col]);

            }

        }


        initEnergyForBoundaries(energy);
        initRemainingEnergy(energy);

//
    }

    public static void main(String args[]) {


    }
    private int unpackPixel(int pixel) {
        int red  = (short) ((pixel >> 16) & 0xFF);
       int  green = (short) ((pixel >> 8) & 0xFF);
        int blue= (short) ((pixel >> 0) & 0xFF);

       return packPixel(red,green,blue);
    }
    /**
     * Create an RGB colour pixel.
     */
    private int packPixel(int red, int green, int blue) {
        return (red << 16) | (green << 8) | blue;
    }
    private double[][] transposeEnergyMatrix(double[][] energy, int rows, int cols) {
        double[][] tranposedEnergy = new double[cols][rows];//rows and columns are interchange
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {

                tranposedEnergy[col][row] = energy[row][col];

            }
        }
        return tranposedEnergy;
    }

    private int[][] transposeColorMatrix(int[][] colorData, int rows, int cols) {


        int[][] tranposedColorData = new int[cols][rows];//rows and columns are interchange
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {

                tranposedColorData[col][row] = colorData[row][col];

            }
        }
//        StdOut.println("transposed picture rows ="+tranposedColorData.length
//                +" , "+tranposedColorData[0].length);
        return tranposedColorData;
    }

    private void initRemainingEnergy(double[][] energy) {
        for (int col = 1; col < cols - 1; col++) {
            for (int row = 1; row < rows - 1; row++) {
                if (energy[row][col] == ENERGY_BOUNDARY) {
                    continue;
                }
                energy[row][col] = calculateEnergy(col, row);
            }
        }
    }

    private double calculateEnergy(int row, int col) {

        if (energy[col][row] == 0d || energy[col][row] != ENERGY_BOUNDARY) {


            int rgbXN = pictureData[col + 1][row];
            int rgbXP = pictureData[col - 1][row];
            int rgbYP = pictureData[col][row - 1];
            int rgbYN = pictureData[col][row + 1];

         /*   Color rgbXN = picture.get(col + 1,row);
            Color rgbXP = picture.get(col - 1,row);
            Color rgbYP =picture.get(col ,row-1);
            Color rgbYN = pictureData[col][row + 1];*/


            double deltax = Math.pow(Color.red(rgbXN) - Color.red(rgbXP), 2) +
                    Math.pow(Color.blue(rgbXN) - Color.blue(rgbXP), 2) +
                    Math.pow(Color.green(rgbXN) - Color.green(rgbXP), 2) ;

            double deltay =Math.pow(Color.red(rgbYN) - Color.red(rgbYP), 2) +
                    Math.pow(Color.blue(rgbYN) - Color.blue(rgbYP), 2) +
                    Math.pow(Color.green(rgbYN) - Color.green(rgbYP), 2) ;
//            deltax = deltax;
//            deltay = deltay;

            energy[col][row] = (Math.sqrt(deltax + deltay));
            //StdOut.print(energy[i][j]+" ");
        }
        return energy[col][row];


    }

    private void initEnergyForBoundaries(double[][] energy) {

        for (int i = 0; i < cols; i++) {
            energy[0][i] = ENERGY_BOUNDARY;
        }
        for (int i = 0; i < rows; i++) {
            energy[i][0] = ENERGY_BOUNDARY;
        }
        for (int i = 0; i < cols; i++) {
            energy[rows - 1][i] = ENERGY_BOUNDARY;
        }
        for (int i = 0; i < rows; i++) {
            energy[i][cols - 1] = ENERGY_BOUNDARY;
        }
    }

    public Bitmap picture() {

        if (lastState == STATE_SEAM.HORIZONTAL_SEAM) {
            rotateMatrices();
            lastState = STATE_SEAM.VERTICAL_SEAM;
        }


        Bitmap newPicture = Bitmap.createBitmap(cols,rows,bitmap.getConfig());

        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {

                newPicture.setPixel(col, row, pictureData[row][col]);
            }
        }
        this.bitmap = newPicture;
        return this.bitmap;

    }                      // current picture

    public int width() {


        return cols;
    }                           // width of current picture

    public int height() {

        return rows;
    }                          // height of current picture

    public double energy(int x, int y) {

        if (x < 0 || x > width() - 1 || y < 0 || y > height() - 1) {
            throw new IllegalArgumentException("x , y " + x + " , " + y);
        }


        return energy[y][x];

    }

    private void rotateMatrices() {
        pictureData = transposeColorMatrix(pictureData, rows, cols);
        energy = transposeEnergyMatrix(energy, rows, cols);

        swapRowsAndCols();
    }

    private void swapRowsAndCols() {

        int temp = rows;
        rows = cols;
        cols = temp;
    }

    // energy of pixel at column x and rows y
    public int[] findVerticalSeam() {


        rows = height();
        cols = width();


        if (lastState == STATE_SEAM.HORIZONTAL_SEAM) {
            rotateMatrices();
        }


        int[] minSeam = null;

        DepthFirstOrder depthFirstOrder = new DepthFirstOrder(energy, rows, cols,    false);
        minSeam = depthFirstOrder.getSeam();
//

        lastState = STATE_SEAM.VERTICAL_SEAM;

        return minSeam;
    }         // sequence of indices for horizontal seam

    public int[] findHorizontalSeam() {


        if (lastState != STATE_SEAM.HORIZONTAL_SEAM) {

            rotateMatrices();
        }


        int[] minSeam = null;

        DepthFirstOrder depthFirstOrder = null;

        depthFirstOrder = new DepthFirstOrder(energy, rows, cols, false);

        minSeam = depthFirstOrder.getSeam();

        lastState = STATE_SEAM.HORIZONTAL_SEAM;


//        StdOut.println("Total Energy "+minEnegy +" min energy = "+minEnegy+"\n");
        return minSeam;

    }

    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("null seam");
        }

        if (seam.length == 0) {
            return;
        }

        if (bitmap.getHeight() <= 1) {
            throw new IllegalArgumentException();
        }


        if (lastState != STATE_SEAM.HORIZONTAL_SEAM) {

            if (seam.length != width()) {
                throw new IllegalArgumentException();
            }


            //from file
            for (int i = 0; i < seam.length; i++) {
                if (seam[i] < 0 || seam[i] > height() - 1) {
                    throw new IllegalArgumentException();

                }
                if (i > 0 && (Math.abs(seam[i] - seam[i - 1])) > 1) {
                    throw new IllegalArgumentException("invalid seam pos");

                }
            }

            rotateMatrices();


            lastState = STATE_SEAM.VERTICAL_SEAM;
//            StdOut.println("After rotate row = "+rows + " col = "+cols +" seam "+seam.length);

        } else {

            if (seam.length != height()) {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < seam.length; i++) {
                if (seam[i] < 0 || seam[i] > width() - 1) {
                    throw new IllegalArgumentException();

                }
                if (i > 0 && (Math.abs(seam[i] - seam[i - 1])) > 1) {
                    throw new IllegalArgumentException("invalid seam pos");

                }
            }


        }


//        double newEnergy[][] = null;
//        Color newColorData[][] = null;
        if (cols > 1 && rows > 0) {

//            newEnergy = new double[rows][cols - 1];
//            newColorData = new Color[rows][cols - 1];

//            StdOut.println("len ===="+(newEnergy.length));
        }

//        if (newEnergy != null) {


            int seamPos = -1;
//            StdOut.println("rows == " + rows);

            for (int row = 0; row < rows; row++) {

                seamPos = seam[row];
//                StdOut.println("row ===="+(row) +"col = "+energy[row].length+" seamPos "+seamPos);

//                System.arraycopy(energy[row], 0, newEnergy[row], 0, seamPos);
                System.arraycopy(energy[row],
                        seamPos + 1, energy[row], seamPos, energy[row].length -1- seamPos);
//                System.arraycopy(pictureData[row], 0, newColorData[row], 0, seamPos);
                System.arraycopy(pictureData[row],
                        seamPos + 1, pictureData[row], seamPos, pictureData[row].length - 1-seamPos);




            }


            for (int row = 1; row < rows - 1; row++) {
//                    StdOut.println("seam =  " + seamPos + " rows = " + rows + " ");
                if (seamPos>0 && seamPos < cols - 1) {
                    energy[row][seamPos] = calculateEnergy(seamPos, row);
                }
            }

//            energy = newEnergy;
//            pictureData = newColorData;
            cols = cols - 1;
//        }
//
        lastState = STATE_SEAM.HORIZONTAL_SEAM;

//        StdOut.print("last state is hor");


        //  removeVerticalSeam(seam);


    } // remove vertical seam from current picture

    public void removeVerticalSeam(int[] seam) {

        if (lastState == STATE_SEAM.HORIZONTAL_SEAM) {
//            StdOut.print("last state is hor");

            rotateMatrices();
        }

        if (seam == null) {
            throw new IllegalArgumentException();

        }
        if (width() <= 1) {
            throw new IllegalArgumentException();
        }

        if (seam.length != height()) {
            throw new IllegalArgumentException();
        }

        // if(lastState ==STATE_SEAM.VERTICAL_SEAM) {
        for (int i = 0; i < seam.length; i++) {
            if (seam[i] < 0 || seam[i] > width() - 1) {
                throw new IllegalArgumentException();

            }
            if (i > 0 && (Math.abs(seam[i] - seam[i - 1])) > 1) {
                throw new IllegalArgumentException();

            }
        }
        //  }
       /* double newEnergy[][] = null;
        Color newColorData[][] = null;
        if (cols > 1 && rows > 0) {

            newEnergy = new double[rows][cols - 1];
            newColorData = new Color[rows][cols - 1];

//            StdOut.println("len ===="+(seam.length));
        }*/

//        if (newEnergy != null) {


            int seamPos = -1;
//              StdOut.println("rows == "+rows);
            for (int row = 0; row < rows; row++) {

                seamPos = seam[row];
//                StdOut.println("rows ===="+(rows));
                System.arraycopy(energy[row],
                        seamPos + 1, energy[row], seamPos, energy[row].length -1- seamPos);
//                System.arraycopy(pictureData[row], 0, newColorData[row], 0, seamPos);
                System.arraycopy(pictureData[row],
                        seamPos + 1, pictureData[row], seamPos, pictureData[row].length - 1-seamPos);



                /*System.arraycopy(energy[row], 0, newEnergy[row], 0, seamPos);
                System.arraycopy(energy[row],
                        seamPos + 1, newEnergy[row], seamPos, newEnergy[row].length - seamPos);
                System.arraycopy(pictureData[row], 0, newColorData[row], 0, seamPos);
                System.arraycopy(pictureData[row],
                        seamPos + 1, newColorData[row], seamPos, newColorData[row].length - seamPos);

*/
            }
//            StdOut.println(" newEnergy len "+newEnergy[0].length);

            //if (lastState == STATE_SEAM.VERTICAL_SEAM) {
                for (int row = 1; row < rows - 1; row++) {

//                    StdOut.println("seam =  " + seamPos + " rows = " + rows + " ");
                    if (seamPos>0 && seamPos < cols - 1) {
                        energy[row][seamPos] = calculateEnergy(seamPos, row);
                    }
               // }
            }


//            energy = newEnergy;
//            pictureData = newColorData;
            cols = cols - 1;

//        }

        lastState = STATE_SEAM.VERTICAL_SEAM;
    }  // remove vertical seam from current picture


    private enum STATE_SEAM {
        HORIZONTAL_SEAM,
        VERTICAL_SEAM
    }

    private static class Point implements Comparable<Point> {
        int x, y;
        double energy;
        private List<Point> adjList;

        Point(int x, int y, double energy) {
            this.adjList = new ArrayList<>(3);
            this.x = y;
            this.y = x;
            this.energy = energy;

        }

        private List<Point> getAdjList() {
            return adjList;
        }

        private void setAdjList(List<Point> adjList) {
            this.adjList = adjList;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Point) {
                Point p = (Point) obj;

                return (p.x == x) && (p.y == y) && (p.energy == energy);
            }
            return false;
        }

        @Override
        public int hashCode() {

            int result =17;

            result+= 31*x;
            result+= 31*y;
            result+=energy;


            return result;
        }

        @Override
        public String toString() {
            return " " + y + "," + x;
        }

        @Override
        public int compareTo(Point o) {
            if (this.x == o.x) {
                if (this.y == o.y) {
                    return 0;
                }
                return this.y < o.y ? -1 : 1;
            }
            return this.x < o.x ? -1 : 1;
        }
    }


    //
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

    private static class DepthFirstOrder {
        private final double[][] energy;
        int rows, cols;
        boolean horizontal = true;
        private boolean[][] marked;
        private Stack<Point> revPostorder;
        private Point[][] pathTo;
        private double[][] distTo;
        private Point[][] vertices;
        private int[] seam;


        public DepthFirstOrder(double[][] energy, int rows, int cols, boolean isHorizontal) {

            this.energy = energy;
            this.rows = rows;
            this.cols = cols;
            this.pathTo = new Point[this.rows][this.cols];
            this.distTo = new double[this.rows][this.cols];
            this.vertices = new Point[this.rows][this.cols];
            for (int i = 0; i < rows; i += 1) {
                for (int j = 0; j < cols; j += 1) {
                    distTo[i][j] = Double.POSITIVE_INFINITY;
                }
            }
//
            this.revPostorder = new Stack<>();

            this.marked = new boolean[this.rows][this.cols];
            horizontal = isHorizontal;
//
            if (horizontal == false) {
                for (int x = 0; x < cols; x += 1) {


//
                    pathTo[0][x] = null;


                    vertices[0][x] = new Point(0, x, energy[0][x]);

//


                    this.dfs(energy, 0, x);
                    this.revPost().push(vertices[0][x]);
                    // }


//                }
                }
            } else {
                for (int x = 0; x < rows; x += 1) {
//                    for (int i = rows-1; i >=0; i -= 1) {


//
                    pathTo[x][0] = null;

                    vertices[x][0] = new Point(x, 0, energy[x][0]);

//
                    if (!marked[x][0]) {
                        this.dfs(energy, x, 0);
                    }
                    this.revPost().push(vertices[x][0]);


//                }
                }
            }


//            }
//now we have vertices in toplogical order
            Stack<Point> topologicalOrder = this.reversePost();
//            StdOut.println("topologicalOrder \n "+topologicalOrder);
//            distTo[4][11] = 0;
            if (horizontal == true) {
                for (int i = 0; i < rows; i++) {
                    distTo[i][0] = 1000;
                }
            } else {
                for (int i = 0; i < cols; i++) {
                    distTo[0][i] = 1000;
                }
            }
//                }
            while (topologicalOrder.isEmpty() == false) {
                Point p = topologicalOrder.pop();
                if (distTo[p.y][p.x] == Double.POSITIVE_INFINITY && p.y == 0) {
                    distTo[p.y][p.x] = p.energy;
                }

                List<Point> adjList = p.getAdjList();
                Iterator<Point> pointIterator = adjList.iterator();
                while (pointIterator.hasNext()) {
                    Point pp = pointIterator.next();
                    if (distTo[pp.y][pp.x] > pp.energy + distTo[p.y][p.x]) {
                        distTo[pp.y][pp.x] = pp.energy + distTo[p.y][p.x];
                        pathTo[pp.y][pp.x] = p;
                    }else{
//                        distTo[pp.y][pp.x] = pp.energy ;
//                        pathTo[pp.y][pp.x] = p;
                    }


                }


            }


            double minEnergy = 1000000;
            Point dest = null;


            Point minSink = null;
            if (horizontal == true) {
                for (int row = 0; row < rows; row++) {
                    double curEnergy = 0;
                    dest = vertices[row][cols - 1];

                    curEnergy = distTo[dest.y][dest.x];
                    if (curEnergy < minEnergy) {
                        minEnergy = curEnergy;
                        minSink = dest;
                    }
                }
            } else {
                for (int col = 0; col < cols - 1; col++) {
                    double curEnergy = 0;
                    dest = vertices[rows - 1][col];

                    curEnergy = distTo[dest.y][dest.x];
                    if (curEnergy < minEnergy) {
                        minEnergy = curEnergy;
                        minSink = dest;
                    }
                }
            }
            int path[] = null;

            if (horizontal == true) {

                path = new int[cols];
            } else {
                path = new int[rows];
            }


            Point sink = minSink;

            int k = 0;
//                    StdOut.println("path--->"+dest +" "+distTo[dest.y][dest.x]);
//                    StdOut.println("min energy ="+minEnergy);


            while (sink != null) {
                            /*try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
//                            StdOut.print(" "+sink+" "+distTo[sink.y][sink.x]);
                if (horizontal) {
                    path[k++] = sink.y;
                } else {
                    path[k++] = sink.x;

                }
                sink = pathTo[sink.y][sink.x];

            }
//                    StdOut.println("seam-->");
            int[] seam = new int[path.length];
            for (int i = path.length - 1; i >= 0; i--) {
                //  StdOut.print(path[i]+" ");
                seam[seam.length - 1 - i] = path[i];
            }
//            StdOut.println();
            setSeam(seam);


        }

        public static void main(String[] args) {


            if(true){
                StdOut.println("Main ");

                String picHex ="#050404 #020509 #030804 #000301 #080308 #020003 \n" +
                        "#070101 #080002 #010009 #040600 #080707 #020601 \n" +
                        "#000309 #020703 #070802 #060105 #000906 #020600 \n" +
                        "#070800 #000701 #000800 #070307 #030109 #080904 \n" +
                        "#070606 #050506 #000206 #060505 #020603 #020706 \n" +
                        "#090605 #010205 #090004 #040702 #040803 #040409";
                Scanner sc = new Scanner(picHex);
               // while (sc.hasNext()){
                    String rowHex = sc.nextLine();
                    StdOut.println("xx = "+rowHex);
                //}


               // Picture picture = new Picture(args[0]);
                //StdOut.printf("image is %d columns by %d rows\n", picture.width(), picture.height());

                return;
            }


            In in = new In(args[0]);
            Digraph G = new Digraph(in);
            apps.sai.com.imageresizer.util.algo.DepthFirstOrder dfs = new apps.sai.com.imageresizer.util.algo.DepthFirstOrder(G);
            StdOut.println("   v  pre post");
            StdOut.println("--------------");

            for (int v = 0; v < G.V(); ++v) {
                StdOut.printf("%4d %4d %4d\n", new Object[]{Integer.valueOf(v), Integer.valueOf(dfs.pre(v)), Integer.valueOf(dfs.post(v))});
            }

            StdOut.print("Preorder:  ");
            Iterator<Integer> i$ = dfs.pre().iterator();

            int v;
            while (i$.hasNext()) {
                v =  i$.next();
                StdOut.print(v + " ");
            }

            StdOut.println();
            StdOut.print("Postorder: ");
            i$ = dfs.post().iterator();

            while (i$.hasNext()) {
                v =  i$.next();
                StdOut.print(v + " ");
            }

            StdOut.println();
            StdOut.print("Reverse postorder: ");
            i$ = dfs.reversePost().iterator();

            while (i$.hasNext()) {
                v =  i$.next();
                StdOut.print(v + " ");
            }

            StdOut.println();
        }

        private void dfs(double[][] energy, int row, int col) {
            this.marked[row][col] = true;

            Point v = vertices[row][col];

            List<Point> adjVertices = new ArrayList<>(3);

            if (horizontal == false) {

                addValidVertex(row + 1, col, adjVertices);
                addValidVertex(row + 1, col - 1, adjVertices);
                addValidVertex(row + 1, col + 1, adjVertices);
            } else {
                addValidVertex(row, col + 1, adjVertices);
                addValidVertex(row + 1, col + 1, adjVertices);
                addValidVertex(row - 1, col + 1, adjVertices);
            }
            v.setAdjList(adjVertices);


            Iterator<Point> i$ = adjVertices.iterator();
            //for each adacent vertex not marked  run dfs
            //if it is marked then update path if better one is found based on
            //comumlative energy

//            StdOut.println(this.revPostorder);
            while (i$.hasNext()) {
//            for(int k=0;k<adjVertices.size();k++){
                Point w = i$.next();

//
                if (!this.marked[w.y][w.x]) {

                    this.dfs(energy, w.y, w.x);
                    this.revPostorder.push(w);
                }
            }
        }

        public int[] getSeam() {
            return seam;
        }

        public void setSeam(int seam[]) {
            this.seam = seam;
        }

        private boolean addValidVertex(int row, int col, List<Point> adjVertices) {

            if (row < 0 || col < 0 || row > this.rows - 1 || col > this.cols - 1) {
//                StdOut.println(" invalid point "+cols +","+rows);
                return false;
            }
            if (vertices[row][col] == null) {
                Point p = new Point(row, col, energy[row][col]);
                vertices[row][col] = p;
//            StdOut.println(" addidp point "+p);

                adjVertices.add(p);
            } else {
                adjVertices.add(vertices[row][col]);
            }

            return true;

        }

        public Stack<Point> revPost() {
            return this.revPostorder;
        }

        public Stack<Point> reversePost() {
            Stack<Point> reverse = new Stack<>();
//            StdOut.println("Inside "+this.revPostorder);
            Iterator<Point> i$ = this.revPostorder.iterator();

            while (i$.hasNext()) {
                Point p =  i$.next();
//                int v = ((Integer)i$.next()).intValue();
//                int v =p.y;
                reverse.add(p);
            }

            return reverse;
        }


    }


}
