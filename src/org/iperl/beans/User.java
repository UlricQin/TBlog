package org.iperl.beans;

import java.sql.Timestamp;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.iperl.service.StorageService;

import my.db.POJO;
import my.mvc.ActionException;
import my.mvc.IUser;

public class User extends POJO implements IUser {

	private static final long serialVersionUID = 6076905147775073492L;
	public static final User INSTANCE = new User();
	public static int PORTRAIT_WIDTH = 50;
	public static int PORTRAIT_HEIGHT = 50;
	public static int PORTRAIT_BWIDTH = 150;
	public static int PORTRAIT_BHEIGHT = 150;

	private String ident;
	private String nickname;
	private String pwd;
	private String portrait;
	private String resume;
	private byte role;
	private int blogcnt;
	private int pageid = 0;
	private Timestamp ctime = new Timestamp(System.currentTimeMillis());

	@Override
	protected boolean IsObjectCachedByID() {
		return true;
	}

	@Override
	public boolean IsBlocked() {
		return getRole() == IUser.ROLE_BANNED;
	}

	@Override
	public String getPwd() {
		return this.pwd;
	}

	@Override
	public byte getRole() {
		return this.role;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPortrait() {
		return portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	public String getResume() {
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}

	public Timestamp getCtime() {
		return ctime;
	}

	public void setCtime(Timestamp ctime) {
		this.ctime = ctime;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public void setRole(byte role) {
		this.role = role;
	}

	public int getBlogcnt() {
		return blogcnt;
	}

	public void setBlogcnt(int blogcnt) {
		this.blogcnt = blogcnt;
	}
	
	public int getPageid() {
		if(this.pageid == 0){
			Page page = Page.INSTANCE.GetByAttr("user", getId());
			if(page == null){
				return 0;
			}
			Long pid = page.getId();
			this.pageid = pid.intValue();
		}
		return pageid;
	}

	public void setPageid(int pageid) {
		this.pageid = pageid;
	}
	
	@Override
	protected Map<String, Object> ListInsertableFields() {
		Map<String, Object> map = super.ListInsertableFields();
		map.remove("pageid");
		return map;
	}

	public String showName() {
		if (StringUtils.isBlank(getNickname())) {
			return getIdent();
		} else {
			return getNickname();
		}
	}

	// 用户原始的
	public String rawPortrait() {
		return getId() + "_raw";
	}
	
	// 经过首次缩放的
	public String portrait() {
		return getId() + "";
	}
	
	// 小头像 50 * 50
	public String smallPortrait() {
		return getId() + "_s";
	}
	
	// 大头像 150 * 150
	public String bigPortrait() {
		return getId() + "_b";
	}
	
	public String showSmallPortrait(){
		if(StringUtils.isBlank(getPortrait())){
			return "/img/guest.png";
		}
		StorageService ss = StorageService.USERPICS;
		String readPath = ss.getReadPath();
		return readPath + smallPortrait() + getPortrait();
	}
	
	public String showBigPortrait(){
		if(StringUtils.isBlank(getPortrait())){
			return "/img/guest.png";
		}
		StorageService ss = StorageService.USERPICS;
		String readPath = ss.getReadPath();
		return readPath + bigPortrait() + getPortrait();
	}

	public String updateProfile(String ident, String nickname, String resume) {
		String oldIdent = this.getIdent();
		if (!ident.equalsIgnoreCase(oldIdent)) {
			User t = GetByAttr("ident", ident);
			if (t != null) {
				throw new ActionException("The ident: " + ident
						+ " has been used");
			}
		}
		boolean success = updateAttrs(new String[] { "ident", "nickname",
				"resume" }, new Object[] { ident, nickname, resume });
		return success ? "" : "Update fail";
	}
}
