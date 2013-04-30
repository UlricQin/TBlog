package org.iperl.action;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpSession;

import my.img.JavaImgHandler;
import my.mvc.ActionException;
import my.mvc.Annotation;
import my.mvc.IUser;
import my.mvc.RequestContext;
import my.toolbox.CoreTool;
import my.util.Multimedia;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.iperl.beans.Invitation;
import org.iperl.beans.User;
import org.iperl.service.PageService;
import org.iperl.service.StorageService;
import org.iperl.service.UserService;
import org.iperl.util.InputChecker;

public class UserAction extends BaseAction {
	
	private static final int MAX_PORTRAIT_SIZE = 1 * 1024 * 1024;

	@Annotation.JSONOutputEnabled
	public void reg(RequestContext ctx) throws IOException {
		String ident = ctx.param("ident", "").trim();
		String pwd = ctx.param("pwd", "").trim();
		String nickname = ctx.param("nickname", "").trim();
		String resume = ctx.param("resume", "").trim();
		String invitation = ctx.param("invitation", "").trim();
		
		Invitation invi = Invitation.INSTANCE.GetByAttr("invitation", invitation);
		if(invi == null || invi.getUsed() == 1){
			throw new ActionException("Invitation is not valid");
		}
		
		invi.updateAttr("used", 1);

		if (!InputChecker.identIsOK(ident)) {
			throw new ActionException(
					"UserID is blank or format error. Check please");
		}

		User t = User.INSTANCE.GetByAttr("ident", ident);
		if (t != null) {
			throw new ActionException("The UserID: " + ident
					+ " is already in use");
		}

		User user = new User();
		user.setIdent(ident);
		user.setPwd(DigestUtils.shaHex(pwd));
		user.setNickname(nickname);
		user.setResume(resume);
		long id = user.Save();

		ctx.saveUserInCookie(user, true);
		ctx.output_json(new String[] { "msg", "id" }, new Object[] { "", id });
	}

	@Annotation.JSONOutputEnabled
	@Annotation.UserRoleRequired()
	public void profile(RequestContext ctx) throws IOException {
		String ident = ctx.param("ident", "").trim();
		String nickname = ctx.param("nickname", "").trim();
		String resume = ctx.param("resume", "").trim();

		if (!InputChecker.identIsOK(ident)) {
			throw new ActionException(
					"UserID is blank or format error. Check please");
		}

		User me = CoreTool.user();
		String msg = me.updateProfile(ident, nickname, resume);
		if (StringUtils.isBlank(msg)) {
			msg = "更新成功";
		}
		ctx.output_json("msg", msg);
	}

	public void logout(RequestContext ctx) throws IOException {
		HttpSession session = ctx.session(false);
		if (session != null) {
			session.invalidate();
		}
		ctx.deleteUserInCookie();
		ctx.redirect("/");
	}
	
	@Annotation.JSONOutputEnabled
	@Annotation.UserRoleRequired()
	public void chpwd(RequestContext ctx) throws IOException {
		User user = CoreTool.user();
		String old_passwd = ctx.param("old_passwd", "");
		String new_passwd = ctx.param("new_passwd", "");
		String new_passwd2 = ctx.param("new_passwd2", "");
		if (!new_passwd.equals(new_passwd2)) {
			throw new ActionException("两次输入的密码应该一致");
		}
		if (!user.getPwd().equals(DigestUtils.shaHex(old_passwd))) {
			throw new ActionException("旧密码输入有误,请重试");
		}
		user.updateAttr("pwd", DigestUtils.shaHex(new_passwd));
		ctx.deleteUserInCookie();
		ctx.output_json(new String[] { "msg" }, new Object[] { "" });
	}

	@Annotation.JSONOutputEnabled
	public void login(RequestContext ctx) throws IOException {
		String ident = ctx.param("ident", "").trim();
		String pwd = ctx.param("pwd", "").trim();
		if (!InputChecker.identIsOK(ident)) {
			throw new ActionException(
					"UserID is blank or format error. Check please");
		}

		User user = UserService.Login(ident, pwd);

		ctx.saveUserInCookie(user, true);
		ctx.output_json("msg", "");
	}

	@Annotation.UserRoleRequired(role = IUser.ROLE_ADMIN)
	@Annotation.JSONOutputEnabled
	public void updateRole(RequestContext ctx) throws IOException {
		int role = ctx.param("role", 0);
		User user = safeUser(ctx);
		user.updateAttr("role", role);
		ctx.output_json("msg", "");
	}

	@Annotation.UserRoleRequired(role = IUser.ROLE_ADMIN)
	@Annotation.JSONOutputEnabled
	public void del(RequestContext ctx) throws IOException {
		User user = safeUser(ctx);
		user.Delete();
		ctx.output_json("msg", "");
	}
	
	/**
	 * 上传头像
	 * @param req
	 * @param res
	 * @throws IOException 
	 */
	@Annotation.PostMethod
	@Annotation.UserRoleRequired
	@Annotation.JSONOutputEnabled
	public void up_portrait(RequestContext ctx) throws IOException {
		if(!ctx.isUpload()){
			throw new ActionException("Need a file to upload");
		}
		
		File imgFile = ctx.image("img");
		if(imgFile == null) {
			throw new ActionException("Img file not accept");
		}
		
		if(imgFile.length() > MAX_PORTRAIT_SIZE ) {
			throw new ActionException("Img file is too large");
		}
		User loginUser = (User)ctx.user();
		
		StorageService ss = StorageService.USERPICS;
		
		String ext = "." + FilenameUtils.getExtension(imgFile.getName());
		
		int[] sizes = Multimedia.saveImage(imgFile, ss.getBasePath() + loginUser.rawPortrait() + ext);
		
		if(sizes != null){
			int[] scaledSizes = JavaImgHandler.getShrinkSize(sizes[0], sizes[1], 640);
			Multimedia.saveImage(imgFile, ss.getBasePath() + loginUser.portrait() + ext, scaledSizes[0], scaledSizes[1]);
			
			loginUser.updateAttr("portrait", ext);
			ctx.output_json(
				new String[]{"img","width","height"}, 
				new Object[]{ss.getReadPath() + loginUser.portrait() + ext +"?r="+Math.random(), scaledSizes[0], scaledSizes[1]}
			);
		}
	}

	/**
	 * 保存头像
	 * @param req
	 * @param res
	 * @throws IOException 
	 */
	@Annotation.PostMethod
	@Annotation.UserRoleRequired
	@Annotation.JSONOutputEnabled
	public void save_portrait(RequestContext ctx) throws IOException {
		int top = ctx.param("top", 0);
		int left = ctx.param("left", 0);
		int width = ctx.param("width", 0);
		int height = ctx.param("height", 0);
		
		User user = (User)ctx.user();
		String portrait = user.portrait();
		String bigPortrait = user.bigPortrait();
		String smallPortrait = user.smallPortrait();
		
		StorageService ss = StorageService.USERPICS;
		String basePath = ss.getBasePath();
		String ext = user.getPortrait();
		
		Multimedia.saveImage(new File(basePath + portrait + ext), basePath + smallPortrait + ext, 
		  top, left, width, height, User.PORTRAIT_WIDTH, User.PORTRAIT_HEIGHT);
		Multimedia.saveImage(new File(basePath + portrait + ext), basePath + bigPortrait + ext, 
				  top, left, width, height, User.PORTRAIT_BWIDTH, User.PORTRAIT_BHEIGHT);
		ctx.output_json("msg", "");
	}
	
	@Annotation.PostMethod
	@Annotation.UserRoleRequired
	@Annotation.JSONOutputEnabled
	public void savePage(RequestContext ctx) throws IOException {
		String content = ctx.param("content", "");
		checkBlank(new String[]{"content"}, new String[]{content});
		User user = CoreTool.user();
		PageService.save(content, user);
		ctx.output_json("msg", "");
	}

}
