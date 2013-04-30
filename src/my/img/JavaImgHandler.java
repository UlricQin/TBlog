package my.img;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.gif4j.GifDecoder;
import com.gif4j.GifEncoder;
import com.gif4j.GifImage;
import com.gif4j.GifTransformer;
import com.jhlabs.image.CropFilter;
import com.jhlabs.image.RotateFilter;

/**
 * 纯Java实现的图像处理
 * 
 * @author Winter Lau
 * @date 2010-4-26 上午09:45:56
 */
public class JavaImgHandler implements ImgHandler {

	public static void main(String[] args) {
		JavaImgHandler handler = new JavaImgHandler();
		File img = new File("F:\\3.jpg");
		// handler.crop(img, new File("F:\\test_crop.jpg"), 0, 0, 500, 500, 100,
		// 100);
		// handler.rotate(img, new File("F:\\test_rotate_90.jpg"), 90);
		// handler.scale(img, new File("F:\\test_scale_560.jpg"), 560);
		// handler.scale(img, new File("F:\\test_scale_530_1800.jpg"), 530,
		// 1800);
//		handler.maxWidth(img, new File("F:\\5.jpg"), 530);
//		handler.toRoundCorner(img, 20);
		try {
			BufferedImage orig_portrait = (BufferedImage) ImageIO.read(img);
			int w = orig_portrait.getWidth();
			int h = orig_portrait.getHeight();
			System.out.println("rawWidth:"+w);
			System.out.println("rawHeight:"+h);
			int[] ret = getShrinkSize(w,h, 530);
			System.out.println(ret[0] + ":" + ret[1]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 未完成
	public void toRoundCorner(File src, int pixels) {
		try {
			BufferedImage orig_portrait = (BufferedImage) ImageIO.read(src);
			Graphics2D g2 = orig_portrait.createGraphics();
			GeneralPath path = new GeneralPath();
			int width = orig_portrait.getWidth();
			int height = orig_portrait.getHeight();
			RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, width,
					height, pixels, pixels);
			path.append(rect, false);
			g2.setClip(path);
			g2.drawImage(orig_portrait, 0, 0, null);
			orig_portrait.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int[] getShrinkSize(int rawWidth, int rawHeight, int maxWidth) {
		if(rawWidth < maxWidth) {
			return new int[] {rawWidth, rawHeight};
		}
		double ratio = (double) maxWidth / rawWidth;
		int w2 = (int) (rawWidth * ratio);
		int h2 = (int) (rawHeight * ratio);
		return new int[] { w2, h2 };
	}

	public int[] maxWidth(File src, File dest, int width) throws ImgException {
		try {
			BufferedImage orig_portrait = (BufferedImage) ImageIO.read(src);
			int w = orig_portrait.getWidth();
			int h = orig_portrait.getHeight();
			if (w <= width) {
				FileUtils.copyFile(src, dest);
				return new int[] { w, h };
			} else {
				double ratio = (double) width / w;
				int w2 = (int) (w * ratio);
				int h2 = (int) (h * ratio);
				scale(src, dest, w2, h2);
				return new int[] { w2, h2 };
			}
		} catch (IOException e) {
			throw new ImgException("Exception occur when shrink image.", e);
		}
	}

	public int[] shrink(File src, File dest, int size) throws ImgException {
		try {
			BufferedImage orig_portrait = (BufferedImage) ImageIO.read(src);
			int w = orig_portrait.getWidth();
			int h = orig_portrait.getHeight();
			if (w <= size && h <= size) {
				FileUtils.copyFile(src, dest);
				return new int[] { w, h };
			} else {
				double ratio = (w > h) ? (double) size / w : (double) size / h;
				int w2 = (int) (w * ratio);
				int h2 = (int) (h * ratio);
				scale(src, dest, w2, h2);
				return new int[] { w2, h2 };
			}
		} catch (IOException e) {
			throw new ImgException("Exception occur when shrink image.", e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see my.img.ImgHandler#crop(java.io.File, java.io.File, int, int, int,
	 *      int, int, int)
	 */
	@Override
	public void crop(File src, File dest, int left, int top, int width,
			int height, int w, int h) throws ImgException {
		if (!dest.getParentFile().exists())
			dest.getParentFile().mkdirs();
		try {
			String ext = FilenameUtils.getExtension(dest.getName())
					.toLowerCase();
			BufferedImage bi = (BufferedImage) ImageIO.read(src);
			height = Math.min(height, bi.getHeight());
			width = Math.min(width, bi.getWidth());
			if (height <= 0)
				height = bi.getHeight();
			if (width <= 0)
				width = bi.getWidth();
			top = Math.min(Math.max(0, top), bi.getHeight() - height);
			left = Math.min(Math.max(0, left), bi.getWidth() - width);

			if (top == 0 && left == 0 && width == bi.getWidth()
					&& height == bi.getHeight()) {
				MyScaleFilter scale = new MyScaleFilter(w, h);
				BufferedImage bi_scale = new BufferedImage(w, h,
						(bi.getType() != 0) ? bi.getType()
								: BufferedImage.TYPE_INT_RGB);
				scale.filter(bi, bi_scale);
				ImageIO.write(bi_scale, ext.equals("png") ? "png" : "jpeg",
						dest);
			} else {
				BufferedImage bi_crop = new BufferedImage(width, height,
						(bi.getType() != 0) ? bi.getType()
								: BufferedImage.TYPE_INT_RGB);
				new CropFilter(left, top, width, height).filter(bi, bi_crop);
				BufferedImage bi_scale = new BufferedImage(w, h,
						(bi.getType() != 0) ? bi.getType()
								: BufferedImage.TYPE_INT_RGB);
				new MyScaleFilter(w, h).filter(bi_crop, bi_scale);
				ImageIO.write(bi_scale, ext.equals("png") ? "png" : "jpeg",
						dest);
			}
		} catch (IOException e) {
			throw new ImgException("Exception occur when crop image.", e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see my.img.ImgHandler#rotate(java.io.File, java.io.File, double)
	 */
	@Override
	public void rotate(File src, File dest, double degrees) throws ImgException {
		if (!dest.getParentFile().exists())
			dest.getParentFile().mkdirs();

		try {
			String ext = FilenameUtils.getExtension(dest.getName())
					.toLowerCase();
			if ("gif".equalsIgnoreCase(ext)) {
				GifImage gifImage = GifDecoder.decode(src);
				GifImage newGif = GifTransformer.rotate(gifImage, degrees,
						false);
				GifEncoder.encode(newGif, dest);
			} else {
				BufferedImage bi = (BufferedImage) ImageIO.read(src);
				RotateFilter Rotate = new RotateFilter(
						(float) Math.toRadians(degrees));
				BufferedImage bi_rotate = new BufferedImage(bi.getHeight(),
						bi.getWidth(), (bi.getType() != 0) ? bi.getType()
								: BufferedImage.TYPE_INT_RGB);
				Rotate.filter(bi, bi_rotate);
				ImageIO.write(bi_rotate, ext.equals("png") ? "png" : "jpeg",
						dest);
			}
		} catch (IOException e) {
			throw new ImgException("Exception occur when scaling image.", e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see my.img.ImgHandler#scale(java.io.File, java.io.File, int, int,
	 *      boolean)
	 */
	@Override
	public void scale(File src, File dest, int w, int h) throws ImgException {
		if (!dest.getParentFile().exists())
			dest.getParentFile().mkdirs();
		try {
			if (w <= 0 && h <= 0) {
				FileUtils.copyFile(src, dest);
				return;
			}
			String ext = FilenameUtils.getExtension(dest.getName())
					.toLowerCase();
			if ("gif".equalsIgnoreCase(ext)) {
				GifImage gifImage = GifDecoder.decode(src);
				GifImage newGif = GifTransformer.resize(gifImage, w, h, false);
				GifEncoder.encode(newGif, dest);
			} else {
				BufferedImage bi = (BufferedImage) ImageIO.read(src);
				MyScaleFilter scale = new MyScaleFilter(w, h);
				BufferedImage bi_scale = new BufferedImage(w, h,
						(bi.getType() != 0) ? bi.getType()
								: BufferedImage.TYPE_INT_RGB);
				scale.filter(bi, bi_scale);
				ImageIO.write(bi_scale, ext.equals("png") ? "png" : "jpeg",
						dest);
			}
		} catch (IOException e) {
			throw new ImgException("Exception occur when scaling image.", e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see my.img.ImgHandler#scale(java.io.File, java.io.File, int, boolean)
	 */
	@Override
	public void scale(File src, File dest, int size) throws ImgException {
		if (!dest.getParentFile().exists())
			dest.getParentFile().mkdirs();

		try {
			String ext = FilenameUtils.getExtension(dest.getName())
					.toLowerCase();
			if ("gif".equalsIgnoreCase(ext)) {
				GifImage gifImage = GifDecoder.decode(src);
				GifImage newGif = GifTransformer.resize(gifImage, size, size,
						false);
				GifEncoder.encode(newGif, dest);
			} else {
				BufferedImage bi = (BufferedImage) ImageIO.read(src);
				int w = bi.getWidth();
				int h = bi.getHeight();
				int max_size = Math.min(w, h);

				// 剪裁
				BufferedImage bi_crop = newImg(bi, max_size, max_size);
				new CropFilter((w - max_size) / 2, (h - max_size) / 2,
						max_size, max_size).filter(bi, bi_crop);

				// 缩小
				MyScaleFilter scale = new MyScaleFilter(size, size);
				BufferedImage bi_scale = newImg(bi_crop, size, size);
				scale.filter(bi_crop, bi_scale);

				ImageIO.write(bi_scale, ext.equals("png") ? "png" : "jpeg",
						dest);
			}
		} catch (IOException e) {
			throw new ImgException("Exception occur when scaling image.", e);
		}
	}

	private BufferedImage newImg(BufferedImage src, int width, int height) {
		ColorModel dstCM = src.getColorModel();
		return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(
				width, height), dstCM.isAlphaPremultiplied(), null);
	}

}
