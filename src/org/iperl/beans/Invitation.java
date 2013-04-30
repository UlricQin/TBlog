package org.iperl.beans;

import my.db.POJO;

/**
 * @author Ulric Qin<cnperl@163.com>
 * @date 2013-4-28
 */
public class Invitation extends POJO {

	private static final long serialVersionUID = 2877086653334198632L;

	public static Invitation INSTANCE = new Invitation();

	private String invitation;
	private int used;

	public Invitation() {

	}

	public Invitation(String invitation, int used) {
		super();
		this.invitation = invitation;
		this.used = used;
	}

	public String getInvitation() {
		return invitation;
	}

	public void setInvitation(String invitation) {
		this.invitation = invitation;
	}

	public int getUsed() {
		return used;
	}

	public void setUsed(int used) {
		this.used = used;
	}

}
