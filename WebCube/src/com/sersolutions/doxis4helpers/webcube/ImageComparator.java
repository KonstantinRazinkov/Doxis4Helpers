package com.sersolutions.doxis4helpers.webcube;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.URISyntaxException;

import static java.awt.Color.RED;

public class ImageComparator {

    /**
     * The threshold which means the max distance between non-equal pixels.
     * Could be changed according size and requirements to the image.
     */
    public static int threshold = 50;

    /**
     * The number which marks how many rectangles. Beginning from 2.
     */
    private int counter = 2;

    /**
     * The number of the marking specific rectangle.
     */
    private int regionCount = counter;

    private BufferedImage image1;
    private BufferedImage image2;
    private int[][] matrix;


    public ImageComparator( InputStream image1, InputStream image2 ) throws IOException, URISyntaxException {

        this.image1 = ImageIO.read(image1);

        this.image2 = ImageIO.read(image2);

        BufferedImage[] images = new BufferedImage[2];
        images[0] = this.image1;
        images[1] = this.image2;
        rescaleToFit(images);
        this.image1 = images[0];
        this.image2 = images[1];
        matrix = populateTheMatrixOfTheDifferences( this.image1, this.image2 );
    }

    public ImageComparator( byte[] image1, byte[] image2 ) throws IOException, URISyntaxException {

        this.image1 = ImageIO.read(new ByteArrayInputStream(image1));

        this.image2 = ImageIO.read(new ByteArrayInputStream(image2));

        BufferedImage[] images = new BufferedImage[2];
        images[0] = this.image1;
        images[1] = this.image2;
        rescaleToFit(images);
        this.image1 = images[0];
        this.image2 = images[1];
        matrix = populateTheMatrixOfTheDifferences( this.image1, this.image2 );
    }

    public ImageComparator( String image1Name, String image2Name ) throws IOException, URISyntaxException {
        image1 = ImageIO.read(new File(image1Name));
        image2 = ImageIO.read(new File(image2Name));

        BufferedImage[] images = new BufferedImage[2];
        images[0] = this.image1;
        images[1] = this.image2;
        rescaleToFit(images);
        this.image1 = images[0];
        this.image2 = images[1];

        matrix = populateTheMatrixOfTheDifferences( image1, image2 );
    }

    /**
     * Draw rectangles which cover the regions of the difference pixels.
     * @return the result of the drawing.
     */
    public BufferedImage compareImages() throws IOException, URISyntaxException {
        // check images for valid
        checkCorrectImageSize( image1, image2 );

        BufferedImage outImg = deepCopy( image2 );

        Graphics2D graphics = outImg.createGraphics();
        graphics.setColor( RED );

        groupRegions();
        drawRectangles( graphics );

        //save the image:
        //saveImage( "build/result2.png", outImg );

        return outImg;
    }

    public byte[] compareImagesToByteArray() throws  IOException, URISyntaxException {
        BufferedImage result = compareImages();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( result, "png", baos );
        baos.flush();
        byte[] finalResult = baos.toByteArray();
        baos.close();

        return finalResult;
    }

    /**
     * Draw rectangles with the differences pixels.
     * @param graphics the Graphics2D object for drawing rectangles.
     */
    private void drawRectangles( Graphics2D graphics ) {
        if( counter > regionCount ) return;

        Rectangle rectangle = createRectangle( matrix, counter );

        graphics.drawRect( rectangle.getMinY(), rectangle.getMinX(), rectangle.getWidth(), rectangle.getHeight() );
        counter++;
        drawRectangles( graphics );
    }

    /**
     * Group rectangle regions in binary matrix.
     */
    private void groupRegions() {
        for ( int row = 0; row < matrix.length; row++ ) {
            for ( int col = 0; col < matrix[row].length; col++ ) {
                if ( matrix[row][col] == 1 ) {
                    joinToRegion( row, col );
                    regionCount++;
                }
            }
        }
    }

    /**
     * The recursive method which go to all directions and finds difference
     * in binary matrix using {@code threshold} for setting max distance between values which equal "1".
     * and set the {@code groupCount} to matrix.
     * @param row the value of the row.
     * @param col the value of the column.
     */

    private void joinToRegion( int row, int col )
    {
        joinToRegion(row, col, threshold);
    }

    private void joinToRegion( int row, int col, int threshold ) {
        if ( row < 0 || row >= matrix.length || col < 0 || col >= matrix[row].length || matrix[row][col] != 1 ) return;

        matrix[row][col] = regionCount;

        for ( int i = 0; i < threshold; i++ ) {
            // goes to all directions.
            joinToRegion( row - 1 - i, col, threshold -1 );
            joinToRegion( row + 1 + i, col, threshold -1 );
            joinToRegion( row, col - 1 - i, threshold -1 );
            joinToRegion( row, col + 1 + i, threshold -1 );

            joinToRegion( row - 1 - i, col - 1 - i, threshold -1 );
            joinToRegion( row + 1 + i, col - 1 - i, threshold -1 );
            joinToRegion( row - 1 - i, col + 1 + i, threshold -1 );
            joinToRegion( row + 1 + i, col + 1 + i, threshold -1 );
        }
    }

    public static Frame createGUI( BufferedImage image ) {
        JFrame frame = new JFrame( "The result of the comparison" );
        frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        JLabel label = new JLabel();
        label.setIcon( new ImageIcon( image, "Result") );
        frame.getContentPane().add( label, BorderLayout.CENTER);
        frame.setPreferredSize(new Dimension( image.getWidth(), ( int )( image.getHeight() ) ) );
        frame.pack();
        frame.setLocationRelativeTo( null );
        frame.setVisible( true );
        return frame;
    }

    /**
     * Make a copy of the {@code BufferedImage} object.
     * @param image the provided image.
     * @return copy of the provided image.
     */
    static BufferedImage deepCopy( BufferedImage image ) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData( null);
        return new BufferedImage( cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Checks images for equals their widths and heights.
     * @param image1 {@code BufferedImage} object of the first image.
     * @param image2 {@code BufferedImage} object of the second image.
     */
    public static void checkCorrectImageSize( BufferedImage image1, BufferedImage image2 ) {
        if( image1.getHeight() != image2.getHeight() || image1.getWidth() != image2.getWidth() )
            throw new IllegalArgumentException( "Images dimensions mismatch" );
    }

    /**
     * Says if the two pixels equal or not. The rule is the difference between two pixels
     * need to be more then 10%.
     * @param rgb1 the RGB value of the Pixel of the Image1.
     * @param rgb2 the RGB value of the Pixel of the Image2.
     * @return {@code true} if they' are difference, {@code false} otherwise.
     */
    public static boolean isDifferent( int rgb1, int rgb2){
        int red1 = ( rgb1 >> 16 ) & 0xff;
        int green1 = ( rgb1 >> 8 ) & 0xff;
        int blue1 = ( rgb1 ) & 0xff;
        int red2 = ( rgb2 >> 16 ) & 0xff;
        int green2 = ( rgb2 >> 8 ) & 0xff;
        int blue2 = ( rgb2 ) & 0xff;
        double result = Math.sqrt( Math.pow( red2 - red1, 2 ) +
                Math.pow( green2 - green1, 2) +
                Math.pow( blue2 - blue1, 2 ) )
                /
                Math.sqrt( Math.pow( 255, 2 ) * 3 );
        return result > 0.1;
    }

    public static Rectangle createRectangle( int[][] matrix, int counter ) {
        Rectangle rect = new Rectangle();

        for ( int y = 0; y < matrix.length; y++ ) {
            for ( int x = 0; x < matrix[0].length; x++ ) {
                if ( matrix[y][x] == counter ) {
                    if ( x < rect.getMinX() ) rect.setMinX( x );
                    if ( x > rect.getMaxX() ) rect.setMaxX( x );

                    if ( y < rect.getMinY() ) rect.setMinY( y );
                    if ( y > rect.getMaxY() ) rect.setMaxY( y );
                }
            }
        }
        return rect;
    }

    /**
     * Populate binary matrix by "0" and "1". If the pixels are difference set it as "1", otherwise "0".
     * @param image1 {@code BufferedImage} object of the first image.
     * @param image2 {@code BufferedImage} object of the second image.
     * @return populated binary matrix.
     */
    static int[][] populateTheMatrixOfTheDifferences( BufferedImage image1, BufferedImage image2 ) {
        int[][] matrix = new int[image1.getWidth()][image1.getHeight()];
        for ( int y = 0; y < image1.getHeight(); y++ ) {
            for ( int x = 0; x < image1.getWidth(); x++ ) {
                matrix[x][y] = isDifferent( image1.getRGB( x, y ), image2.getRGB( x, y ) ) ? 1 : 0;
            }
        }
        return matrix;
    }


    static void rescaleToFit(BufferedImage[] images) {
        BufferedImage image1 = images[0];
        BufferedImage image2 = images[1];

        int minWith = (image1.getWidth() <= image2.getWidth())? image1.getWidth() : image2.getWidth();
        int minHeight = (image1.getHeight() <= image2.getHeight())? image1.getHeight() : image2.getHeight();

        BufferedImage resized = new BufferedImage(minWith, minHeight, image1.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image1, 0, 0, minWith, minHeight, 0, 0, image1.getWidth(),
                image1.getHeight(), null);
        g.dispose();
        image1 = resized;

        resized = new BufferedImage(minWith, minHeight, image2.getType());
        g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image2, 0, 0, minWith, minHeight, 0, 0, image2.getWidth(),
                image2.getHeight(), null);
        g.dispose();
        image2 = resized;

        images[0] = image1;
        images[1] = image2;
    }

    /**
     * Save image to the provided path.
     * @param path the path to the saving image.
     * @param image the {@code BufferedImage} object of this specific image.
     * @throws IOException
     */
    public static void saveImage(String path, BufferedImage image ) throws IOException {
        // make dir if it's not using from Gradle.
        new File( path ).mkdirs();
        ImageIO.write( image, "png", new File( path ) );
    }

}

class Rectangle {

    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() { return minY; }

    public int getMaxX() { return maxX; }

    public int getMaxY() { return maxY; }

    public int getWidth() { return maxY - minY; }

    public int getHeight() { return maxX - minX; }
}
