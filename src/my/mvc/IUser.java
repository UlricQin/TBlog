package my.mvc;

public interface IUser {
	
	public static final byte ROLE_BANNED = -1;
	public static final byte ROLE_GENERAL = 0;
	public static final byte ROLE_EDITOR = 1;
	public static final byte ROLE_ADMIN = 2;
	
	public boolean IsBlocked();
	
	public long getId();
	
	public String getPwd();
	
	public byte getRole();

}
