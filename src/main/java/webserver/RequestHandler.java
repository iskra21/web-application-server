package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import db.DataBase;
import model.User;
import util.HttpRequest;
import util.HttpResponse;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class.getName());

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	// Http 요청을 처리할 클래스를 만든다.
        	// Http Request를 읽어 온다. URI 3파트, 다음 헤더 :로 분리된 것, body
        	// URI에 따라 처리할 일을 구분한다. html로 끝나는 것. ico로 끝나는 것. ?이 있으면 CGI
        	// 첫재줄은 action uri version action은 enum으로 구분하자
        	// 읽어온다, action과 uri에 따라 할 일을 정한다.
        	
        	HttpRequest req = new HttpRequest(in);
        	
        	//byte[] body = "Hello World".getBytes();
            //DataOutputStream dos = new DataOutputStream(out);        	
        	
        	String url = req.getUrl();
        	HttpResponse res = new HttpResponse(out);
        	
        	if ("/user/create".equals(url)) {
        		User user = new User(req.getParameter("userId"), req.getParameter("password"), req.getParameter("name"), req.getParameter("email"));
        		DataBase.addUser(user);
        		res.sendRedirect("/index.html");
        	} else if ("/user/list".equals(url)) {
        		if (isLoggined(req)) {
        			StringBuilder body = new StringBuilder();
        			Collection<User> users = DataBase.findAll();
        			body.append("...");
        			for (User user:users) {
        				body.append(user.getUserId()+user.getPassword()+user.getName()+user.getEmail());
        			}
        			body.append("...");
        			res.forwardBody(body.toString());
        		} else {
        			res.sendRedirect("/index.html");
        		}
        	} else if ("/user/login".equals(url)) {
        		if (loginCorrect(req)) {
        			res.addHeader("Set-Cookie", "logined=true");
        			res.sendRedirect("/index.html");
        		} else {
        			res.addHeader("Set-Cookie", "logined=false");
        			res.sendRedirect("/user/login_failed.html");
        		}
        	} else { // .html;.css;.js 경우
        		res.forward("./webapp/"+url);
        	}
        	
        	if (isResourceRequest(req)) {
        		// html 이나 ico 요청, get 이고 / 또는 html 또는 ico로 끝날 때, webapp 어디서 붙일까?
        		body = getResource(req.getUri());
                response200Header(dos, body.length);
                responseBody(dos, body);
        	} else if (isCSS(req)) {
        		body = getResource(req.getUri());
        		response200CSSHeader(dos, body.length);
        		responseBody(dos, body);
        	} else if (isRegister(req)) {
        		// cgi 요청, /user/create 호출 해야 함. 보통 확장자로 구분할듯.
        		model.User user = doRegister(req);
        		db.DataBase.addUser(user);
        		log.debug(user.toString());
                response302Header(dos);
        	} else if (isLogin(req)) {
        		boolean logined = doLogin(req);
        		if (logined) {
        			log.debug("login successful");
        			response200LoginOkHeader(dos);
        		} else {
        			log.debug("login failed");
        			response401LoginFailedHeader(dos);
        		}
        		
        	} else if (isList(req)){
        		if (isLoggined(req)) {
        			body = doList();
        			response200Header(dos, body.length);
        			responseBody(dos, body);
        		} else {
        			response302Header(dos);
        		}
        	
            }else {
                response200Header(dos, body.length);
                responseBody(dos, body);        		
        	}

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200CSSHeader(DataOutputStream dos, int length) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css\r\n");
            dos.writeBytes("Content-Length: " + length + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }		
	}

	private boolean isCSS(RequestParser req) {
        if("GET".equals(req.getMethod()) && req.getUri().matches("[\\w\\-\\/]*\\.css")) {
    		return true;
    	}
		return false;
	}

	private byte[] doList() {
    	Collection<User> users = db.DataBase.findAll();
    	StringBuilder str = new StringBuilder();
    	
    	str.append("ID\tPassword\tName\tE-Mail\r\n");
    	for(User user:users) {
    		str.append(user.getUserId()+"\t"+user.getPassword()+"\t"+user.getName()+"\t"+user.getEmail()+"\r\n");
    	}
    	return str.toString().getBytes();
	}

	private boolean isLoggined(RequestParser req) {
    	String value = req.findKey("Cookie");
    	if (Strings.isNullOrEmpty(value)) {
    		return false;
    	}
    	Map<String,String> cookie = util.HttpRequestUtils.parseCookies(value);
    	String logginedOrNot = cookie.get("logined");
    	if (!Strings.isNullOrEmpty(logginedOrNot) && Boolean.parseBoolean(logginedOrNot)) {
    		return true;
    	}
    	return false;
	}

	private boolean isList(RequestParser req) {
    	String method = req.getMethod();
    	String uri = req.getUri();
    	
    	int index = uri.indexOf('?');
    	log.debug("Position of ?: {}", index);
    	if (index != -1) {
    		uri = uri.substring(0, index);
    	}
    	if (method.matches("(GET)|(POST)") && uri.matches("[\\w\\-\\/]*list")) {
    		return true;
    	}
		return false;
	}

	private void response401LoginFailedHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /user/login_failed.html\r\n");
            dos.writeBytes("Set-Cookie: logined=false\r\n");            
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }	
	}

	private void response200LoginOkHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html\r\n");
            dos.writeBytes("Set-Cookie: logined=true\r\n");            
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }	
    }

	private boolean doLogin(RequestParser req) throws UnsupportedEncodingException {
    	String method = req.getMethod();
    	String uri = req.getUri();
    	model.User loginUser = null;

    	if ("POST".equals(method)) {
    		loginUser = parseCGI(req.getContentsBody().toString());
    	} else if ("GET".equals(method)) {
    		uri = URLDecoder.decode(uri, "UTF-8");
    		int index = uri.indexOf('?');
    		uri = uri.substring(index+1);
    		loginUser = parseCGI(uri);
    	}
    	model.User findUser = db.DataBase.findUserById(loginUser.getUserId());
    	if ((findUser != null) && findUser.getPassword().equals(loginUser.getPassword())) {
    		return true;
    	}
    	return false;
	}

	private boolean isLogin(RequestParser req) {
    	String method = req.getMethod();
    	String uri = req.getUri();    	
    	int index = uri.indexOf('?');    	
    	if (index != -1) {
    		uri = uri.substring(0, index);
    	}   	
    	if (method.matches("(GET)|(POST)") && uri.matches("[\\w\\-\\/]*login")) {
    		return true;
    	}
		return false;
	}

	private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }		
	}

	private model.User doRegister(RequestParser req) throws UnsupportedEncodingException {
    	String method = req.getMethod();
    	String uri = req.getUri();
    	
    	if ("GET".equals(method)) {
    		uri = URLDecoder.decode(uri, "UTF-8");
    		int index = uri.indexOf('?');
    		uri = uri.substring(index+1);
    		return parseCGI(uri);
    	} else if ("POST".equals(method)) {
    		return parseCGI(req.getContentsBody().toString());
    	}
    	
		return null;
	}

	private byte[] getResource(String uri) throws IOException {
    	String prefix = "./webapp";    	
    	String defaultFile = "index.html";
    	
    	uri = uri.endsWith("/") ? prefix + uri + defaultFile : prefix + uri;

    	if (File.separatorChar != '/') uri = uri.replace('/', File.separatorChar);

    	return Files.readAllBytes(new File(uri).toPath());
	}

	private boolean isRegister(RequestParser req) {
		// TODO Auto-generated method stub
    	String method = req.getMethod();
    	String uri = req.getUri();
    	
    	int index = uri.indexOf('?');
    	
    	log.debug("Position of ?: {}", index);
    	
    	if (index != -1) {
    		uri = uri.substring(0, index);
    	}
    	
    	if (method.matches("(GET)|(POST)") && uri.matches("[\\w\\-\\/]*create")) {
    		return true;
    	}
    	
		return false;
	}

	private boolean isResourceRequest(RequestParser req) {
		// TODO Auto-generated method stub

    	if("GET".equals(req.getMethod()) && req.getUri().matches("[\\w\\-\\/]*(\\.html)|\\/")) {
    		return true;
    	}
    	
		return false;
	}

	private model.User parseCGI(String cgi) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
    	String[] tokens = cgi.split("\\&");
    	HashMap<String,String> map = new HashMap<String,String>(tokens.length);
    	for(String token:tokens) {
    		String[] tokenElements = token.split("\\=");
    		map.put(tokenElements[0].toLowerCase(), tokenElements[1]);
    	}
		return new model.User(map.get("userid"), map.get("password"), map.get("name"), map.get("email"));
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
