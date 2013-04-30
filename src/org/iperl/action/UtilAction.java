package org.iperl.action;

import java.io.IOException;
import java.util.UUID;

import org.iperl.beans.Invitation;

import my.img.ImageCaptchaService;
import my.mvc.Annotation;
import my.mvc.IUser;
import my.mvc.RequestContext;

public class UtilAction extends BaseAction {

	public void captcha(RequestContext ctx) throws IOException {
		ImageCaptchaService.get(ctx);
	}
	
	@Annotation.UserRoleRequired(role=IUser.ROLE_ADMIN)
	public void invitation(RequestContext ctx) throws IOException {
		String uuid = UUID.randomUUID().toString();
		Invitation invitation = new Invitation(uuid, 0);
		invitation.Save();
		ctx.print(uuid);
	}

}
