package my.img;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.jhlabs.image.AbstractBufferedImageOp;

/**
 * Scales an image using the area-averaging algorithm, which can't be done with AffineTransformOp.
 * @author Winter Lau
 * @date 2010-4-26 上午11:56:06
 */
public class MyScaleFilter extends AbstractBufferedImageOp {

	private int width;
	private int height;

    /**
     * Construct a ScaleFilter.
     */
	public MyScaleFilter() {
		this(32, 32);
	}

    /**
     * Construct a ScaleFilter.
     * @param width the width to scale to
     * @param height the height to scale to
     */
	public MyScaleFilter( int width, int height ) {
		this.width = width;
		this.height = height;
	}

    public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		if ( dst == null ) {
			ColorModel dstCM = src.getColorModel();
			dst = new BufferedImage(dstCM, 
				dstCM.createCompatibleWritableRaster( width, height ), 
				dstCM.isAlphaPremultiplied(), null);
		}

		Image scaleImage = src.getScaledInstance( width, height, Image.SCALE_SMOOTH );
		Graphics2D g = dst.createGraphics();
		g.drawImage( scaleImage, 0, 0, width, height, null );
		g.dispose();

        return dst;
    }

	public String toString() {
		return "Distort/Scale";
	}

}