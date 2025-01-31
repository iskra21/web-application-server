package util;

import java.util.HashMap;
import java.util.Map;

public class HttpSessions {
	static Map<String,HttpSession> sessions = new HashMap<String,HttpSession>();

	public static HttpSession getSession(String id) {
		HttpSession session = sessions.get(id);
		
		if (session == null) {
			session = new HttpSession(id);
			sessions.put(id, session);
		}
		
		return session;
	}

	public static void remove(String id) {
		sessions.remove(id);
	}
}
