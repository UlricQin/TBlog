package my.db;

/**
 * @author Ulric
 */
public class DBException extends RuntimeException {
	
	private static final long serialVersionUID = -4607978556048816003L;

	public DBException(){
		super();
	}
	
	public DBException(String s){
		super(s);
	}
	
	public DBException(Throwable t){
		super(t);
	}
	
	public DBException(String s, Throwable t){
		super(s, t);
	}

}
