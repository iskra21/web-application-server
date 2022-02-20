package util;

import java.util.HashMap;
import java.util.Map;

public class HttpSession {
	private String id;
	private Map<String,Object> attributes; 
	
	public HttpSession(String id) {
		this.id = id;
		this.attributes = new HashMap<String,Object>();
	}

	public String getId() {
		return this.id;
	}
}
