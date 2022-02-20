package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import webserver.RequestHandler;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class.getName());
	
	private String method;
	private String url;
	private String httpVersion;
	private HttpSession session;
	private Map<String,String> headers;
	private Map<String,String> params;
	private Map<String,String> cookies;
	
	public HttpRequest(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		String line = reader.readLine();
		if (line == null) {
			return;
		}
		
		String[] tokens = line.split(" ");
		this.method = tokens[0];
		this.url = tokens[1];
		this.httpVersion = tokens[2];
		
    	this.headers = new HashMap<String,String>();
    	while(!"".equals(line = reader.readLine())) {
    		HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
    		headers.put(pair.getKey(), pair.getValue());
    	}
    	
    	if (!Strings.isNullOrEmpty(line = getHeader("Cookie"))) {
    		this.cookies = HttpRequestUtils.parseCookies(line);
    	}
    	
    	if (!Strings.isNullOrEmpty(line = this.cookies.get("JSESSIONID"))) {
    		this.session = HttpSessions.getSession(line);
    	} else {
    		this.session = new HttpSession(line = UUID.randomUUID().toString()); 
    		HttpSessions.registerSession(line, this.session);
    	}
    	
    	// 본문 읽기
    	int index = url.indexOf('?');
    	if ("POST".equals(this.method) && (line = getHeader("Content-Length")) != null) {
    		this.params = HttpRequestUtils.parseQueryString(URLDecoder.decode(IOUtils.readData(reader, Integer.parseInt(line)), "UTF-8"));
    	} else if ("GET".equals(this.method) && index != -1) {
    		this.params = HttpRequestUtils.parseQueryString(URLDecoder.decode(line.substring(index+1), "UTF-8"));
    	}
	}

	public String getMethod() {
		return this.method;
	}

	public String getUrl() {
		return this.url;
	}

	public String getHeader(String key) {
		return this.headers.get(key);
	}
	
	public String getParameter(String key) {
		return this.params.get(key);
	}

	public HttpSession getSession() {
		return this.session;
	}

	public String getCookie(String key) {
		return cookies.get(key);
	}
}
