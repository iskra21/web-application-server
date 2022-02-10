package webserver;

import java.util.HashMap;
import java.util.Map;

public class ControllerMapping {
	static Map<String,Controller> controllerMap = new HashMap<String,Controller>();
	
	static {
    	controllerMap.put("/user/create", new CreateUserController());
    	controllerMap.put("/user/list", new ListUserController());
    	controllerMap.put("/user/login", new LoginController());
	}
	
	public static Controller getController(String url) {
		return controllerMap.get(url);
	}
}
