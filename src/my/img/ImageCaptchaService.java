package my.img;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import my.cache.CacheManager;
import my.mvc.RequestContext;
import my.util.RequestUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

//import com.jhlabs.image.*;

/**
 * 图形验证码
//调用方法
if(!ImageCaptchaService.validate(ctx.request()))
   //提示错误信息
 * @author liudong
 */
public class ImageCaptchaService {

	private final static String CACHE_REGION = "session";
	private final static String COOKIE_NAME = "_reg_key_"; 
	private static int WIDTH = 120;
	private static int HEIGHT = 40;
	private static int LENGTH = 5;
	private final static Random random = new Random();

	/**
	 * 生成验证码
	 * @param req
	 * @param res
	 * @throws IOException 
	 */
	public static void get(RequestContext ctx) throws IOException{
		if(ctx.isRobot()){
			ctx.forbidden();
			return;
		}
		ctx.closeCache();
        ctx.response().setContentType("image/png");
        _Render(_GenerateRegKey(ctx), ctx.response().getOutputStream(), WIDTH, HEIGHT);
	}
	
	/**
	 * 检查验证码是否正确
	 * @param req
	 * @return
	 */
	public static boolean validate(HttpServletRequest req) {
		Cookie cke = RequestUtils.getCookie(req, COOKIE_NAME);
		if(cke == null) {
			String ssnId = req.getSession().getId();
			String code1 = CacheManager.get(String.class, CACHE_REGION, ssnId);
			String code2 = req.getParameter("verifyCode");
			return StringUtils.equalsIgnoreCase(code1, code2);
		}
		if(cke!=null && StringUtils.isNotBlank(cke.getValue())){
			String key = cke.getValue();
			String code1 = CacheManager.get(String.class, CACHE_REGION, key);
			String code2 = req.getParameter("verifyCode");
			return StringUtils.equalsIgnoreCase(code1, code2);
		}
		return false;		
	}
	
	private static String _GenerateRegKey(RequestContext ctx) {
		Cookie cke = ctx.cookie(COOKIE_NAME);
		String REG_VALUE = null;
		//检查验证码相关的cookie是否存在,cookie是类似这样的new HttpCookie("_reg_key",20个随机字符串)
		if(cke==null||StringUtils.isBlank(cke.getValue())){
			REG_VALUE = RandomStringUtils.randomAlphanumeric(20);
			ctx.cookie(COOKIE_NAME, REG_VALUE, 3600, true);
		}
		else
			REG_VALUE = cke.getValue();
		//生成验证码图片上的文字
		String code = RandomStringUtils.randomAlphanumeric(LENGTH).toUpperCase();
		code.replace('0', 'W');
		code.replace('o', 'R');
		code.replace('I', 'E');
		code.replace('1', 'T');
		//把验证码放入缓存，以上面cookie中的20个随机字符串作为key
		CacheManager.set(CACHE_REGION, REG_VALUE, code);
		if(ctx.session()!=null){
			//为了方便，如果当前主机上存在session，顺便把验证码放入key是jsessionId的缓存中
			CacheManager.set(CACHE_REGION, ctx.session().getId(), code);
		}
		return code;
	}
	
    /**
     * 画随机码图
     * @param text
     * @param out
     * @param width
     * @param height
     * @throws IOException
     */
    private static void _Render(String text, OutputStream out, int width, int height) throws IOException {
	    BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);        
	    Graphics2D g = (Graphics2D)bi.getGraphics();
	    
	    g.setColor(Color.WHITE);
	    g.fillRect(0,0,width,height);
    	//g.setColor(Color.RED);
	    //g.drawRect(1,1,width-2,height-2);
	    for(int i=0;i<10;i++){
	    	g.setColor(_GetRandColor(150, 250));
	    	g.drawOval(random.nextInt(110), random.nextInt(24), 5+random.nextInt(10), 5+random.nextInt(10));
	    }
	    Font mFont = new Font("Arial", Font.ITALIC, 28);
	    g.setFont(mFont);
	    g.setColor(_GetRandColor(10,240));
	    g.drawString(text, 10, 30);
	    ImageIO.write(bi, "png", out);
    }
    
    private static Color _GetRandColor(int fc,int bc){//给定范围获得随机颜色
		if (fc > 255) fc = 255;
		if (bc > 255) bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

    public static void main(String[] args) throws IOException {
		String code = RandomStringUtils.randomAlphanumeric(LENGTH).toUpperCase();
		code = code.replace('0', 'W');
		code = code.replace('o', 'R');
		code = code.replace('I', 'E');
		code = code.replace('1', 'T');
		
    	FileOutputStream out = new FileOutputStream("aa.png");
    	_Render(code,out,120,40);
    }
    
}
