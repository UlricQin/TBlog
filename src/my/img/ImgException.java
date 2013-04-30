package my.img;

/**
 * 图像处理异常
 * @author Winter Lau
 * @date 2010-4-26 上午09:22:13
 */
public class ImgException extends RuntimeException {

	public ImgException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImgException(String message) {
		super(message);
	}

}
