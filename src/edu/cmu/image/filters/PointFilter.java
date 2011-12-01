package edu.cmu.image.filters;

/**
 * An abstract superclass for point filters. The interface is the same as the old RGBImageFilter.
 */
public abstract class PointFilter {
        private int width;
        private int height;
        
        protected boolean canFilterIndexColorModel = false;

    public int[] filter( int[] src ,int w, int h) {
        width = w;
        height = h;

        setDimensions( width, height);
        
                int[] inPixels = new int[width];
                int[] outPixels = new int[width * height];
                
        for ( int y = 0; y < height; y++ ) {
                int index = 0;
                for(int i=(y*width);i<((y*width) + width);++i){
                        inPixels[index] = src[i];
                        index++;
                }
                        
                        for ( int x = 0; x < width; x++ ){
                                inPixels[x] = filterRGB( x, y, inPixels[x] );
                        }                       
                        
                        index = 0;
                        for(int i=(y*width);i<((y*width) + width);++i){
                                outPixels[i] = inPixels[index];
                        index++;
                }                       
        }

        return outPixels;
    }

        public void setDimensions(int w, int h) {
                width = w;
        height = h;
        }

        public abstract int filterRGB(int x, int y, int rgb);
}