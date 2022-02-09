package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
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
        	HttpRequest req = new HttpRequest(in);
        	HttpResponse res = new HttpResponse(out);
        	String url = req.getUrl();

        	if ("/user/create".equals(url)) {
        		User user = new User(req.getParameter("userId"), req.getParameter("password"), req.getParameter("name"), req.getParameter("email"));
        		DataBase.addUser(user);
        		res.sendRedirect("/index.html");
        	} else if ("/user/list".equals(url)) {
        		if (isLoggined(req)) {
        			StringBuilder body = new StringBuilder();
        			Collection<User> users = DataBase.findAll();
        			body.append("<table border='1'>");
        			body.append("<tr><td>UserId</td><td>Password</td><td>Name</td><td>E-Mail</td></tr>");
        			for (User user:users) {
        				body.append("<tr>");
        				body.append("<td>"+user.getUserId()+"</td>");
        				body.append("<td>"+user.getPassword()+"</td>");
        				body.append("<td>"+user.getName()+"</td>");
        				body.append("<td>"+user.getEmail()+"</td>");
        				body.append("</tr>");
        			}
        			body.append("</table>");
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
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLoggined(HttpRequest req) {
    	String value = req.getHeader("Cookie");
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

	private boolean loginCorrect(HttpRequest req) {
    	User user = DataBase.findUserById(req.getParameter("userId"));
    	if ((user != null) && user.getPassword().equals(req.getParameter("password"))) {
    		return true;
    	}
    	return false;
	}
}
