package org.iperl.dto;

import java.util.List;

public class Pagination {

	private int cnt;
	private List list;

	public Pagination() {

	}

	public Pagination(int cnt, List list) {
		this.cnt = cnt;
		this.list = list;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}
	
	public static int safePageNo(int pageNo) {
		return pageNo <= 0 ? 1 : pageNo;
	}

}
