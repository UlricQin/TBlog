package my.img;

import java.io.File;

/**
 * 图像处理接口
 * @author Winter Lau
 * @date 2010-4-26 上午09:15:10
 */
public interface ImgHandler {
	
	/**
	 * 图像自动根据比例缩小到指定的方框中
	 * @param src
	 * @param dest
	 * @param size
	 * @return
	 * @throws ImgException
	 */
	public int[] shrink(File src, File dest, int size) throws ImgException ;

	/**
	 * 图像缩放
	 * @param src	源文件
	 * @param dest	目标文件
	 * @param w		缩放宽度
	 * @param h		缩放高度
	 * @exception
	 */
	public void scale(File src, File dest, int w, int h) throws ImgException ;
	
	/**
	 * 将图像缩放到某个正方形框内
	 * @param src	源文件
	 * @param dest	目标文件
	 * @param size	正方形大小
	 * @exception
	 */
	public void scale(File src, File dest, int size) throws ImgException ;
	
	/**
	 * 进行图像剪裁
	 * @param src	源文件
	 * @param dest	目标文件
	 * @param left	剪裁部分的左上角x轴
	 * @param top	剪裁部分的左上角y轴
	 * @param width	剪裁部分的宽度
	 * @param height	剪裁部分的高度
	 * @param w	目标大小宽度
	 * @param h	目标大小高度
	 * @throws ImgException
	 */
	public void crop(File src, File dest, int left, int top, int width, int height, int w, int h) throws ImgException ;
	
	/**
	 * 图像旋转
	 * @param src	源文件
	 * @param dest	目标文件
	 * @param degrees	旋转度数
	 * @throws ImgException
	 */
	public void rotate(File src, File dest, double degrees) throws ImgException ;
	
}
