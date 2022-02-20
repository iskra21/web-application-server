package util;

import java.util.HashMap;
import java.util.Map;

public class HttpSessions {
	static Map<String,HttpSession> sessions = new HashMap<String,HttpSession>();

	public static HttpSession getSession(String id) {
		return sessions.get(id);
	}

	public static HttpSession registerSession(String id, HttpSession session) {
		return sessions.put(id, session);
	}
}
