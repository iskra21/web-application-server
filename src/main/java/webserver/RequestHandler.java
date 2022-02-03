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
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;

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
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        	String[] lines = readAllLines(reader);
        	if (lines == null) {
        		return;
        	}
        	log.debug("First line of request: {}", lines[0]);
        	String[] tokens = lines[0].split(" ");
        	log.debug("First token: {}, second token {}", tokens[0], tokens[1]);
        	String[] ttokens = tokens[1].split("\\?");
        	byte[] body = "Hello World".getBytes();
        	if (tokens[0].equals("GET") && tokens[1].equals("/index.html")) {
        		log.debug("Reading the index.html file.");
        		body = Files.readAllBytes(new File(".\\webapp\\index.html").toPath());
        	} else if (tokens[0].equals("GET") && tokens[1].equals("/user/form.html")) {
        		log.debug("Reading the  file.");
        		body = Files.readAllBytes(new File(".\\webapp\\user\\form.html").toPath());
        	} else if (tokens[0].equals("GET") && ttokens[0].equals("/user/create")) {
        		log.debug("/user/create");
        		model.User user = parseGET(ttokens[1]);
        		log.debug(user.toString());
        	}

            DataOutputStream dos = new DataOutputStream(out);
            // byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private model.User parseGET(String string) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
    	String decodedURI = URLDecoder.decode(string, "UTF-8"); 
    	String[] tokens = decodedURI.split("\\&");
    	HashMap<String,String> map = new HashMap<String,String>(tokens.length);
    	for(String token:tokens) {
    		String[] tokenElements = token.split("\\=");
    		map.put(tokenElements[0].toLowerCase(), tokenElements[1]);
    	}
		return new model.User(map.get("userid"), map.get("password"), map.get("name"), map.get("email"));
	}

	private String[] readAllLines(BufferedReader reader) throws IOException {
		// TODO Auto-generated method stub
    	List<String> l = new ArrayList<String>();
    	int i = 0;
    	String line;
    	while(!"".equals(line = reader.readLine())) {
    		log.debug("request[{}]: {}", i++, line);
    		l.add(line);
    	}
    	
		return (String[]) l.toArray(new String[l.size()]);
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
