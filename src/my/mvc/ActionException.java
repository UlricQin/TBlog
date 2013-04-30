package my.mvc;

public class ActionException extends RuntimeException {

	public ActionException(){
		
	}
	
	public ActionException(String msg) {
		super(msg);
	}
	
	public ActionException(Throwable t){
		super(t);
	}

	public ActionException(String s, Throwable t){
		super(s, t);
	}
}
