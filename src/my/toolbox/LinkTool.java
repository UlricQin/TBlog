package my.toolbox;

import my.mvc.RequestContext;

import org.apache.commons.lang.math.NumberUtils;

public class LinkTool {

	public String param(String name, String...def_value) {
		if(RequestContext.get() == null || RequestContext.get().request() == null){
			return (def_value.length>0)?def_value[0]:null;
		}
		String v = RequestContext.get().request().getParameter(name);
		return (v!=null)?v:((def_value.length>0)?def_value[0]:null);
	}
	
	public long param(String name, long def_value) {
		return NumberUtils.toLong(param(name), def_value);
	}

	public int param(String name, int def_value) {
		return NumberUtils.toInt(param(name), def_value);
	}

	public byte param(String name, byte def_value) {
		return (byte)NumberUtils.toInt(param(name), def_value);
	}

}
