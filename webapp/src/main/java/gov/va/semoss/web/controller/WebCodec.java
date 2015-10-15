package gov.va.semoss.web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import gov.va.semoss.user.RemoteUserImpl;
import gov.va.semoss.user.User.UserProperty;
import gov.va.semoss.web.io.DbInfo;
import gov.va.semoss.web.security.SemossUser;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class WebCodec {

	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static WebCodec instance;
	
	private static final Logger log = Logger.getLogger( WebCodec.class );
	
	private WebCodec(){ }
	
	public static WebCodec instance(){
		if (instance == null){
			instance = new WebCodec();
		}
		return instance;
	}
	
	public Object parse(String encodedJSON){
		String json = null;
		Map<?,?> jsonMap = null;
		try {
			json = java.net.URLDecoder.decode(encodedJSON, "UTF-8");
			jsonMap = mapper.readValue(json, Map.class);
			String className = (String)jsonMap.get("vcamp_class");
			return parse(jsonMap, className);
		} catch (UnsupportedEncodingException e) {
			log.error("Exception occurred when decoding json object.", e);
		} catch (JsonParseException e) {
			log.error("Exception occurred when decoding json object.", e);
		} catch (JsonMappingException e) {
			log.error("Exception occurred when decoding json object.", e);
		} catch (IOException e) {
			log.error("Exception occurred when decoding json object.", e);
		}
		return null;
	}
	
	private Object parse(Map<?,?> map, String className) {
		Object object = null;
		if (className.equals(SemossUser.class.getName())){
			object = parseUser(map);
		}
		if (className.equals(DbInfo.class.getName())){
			object = parseDatabase(map);
		}
		if (className.equals(HashMap.class.getName())){
			object = parseAccessRights(map);
		}
		
		return object;
	}
	
	private RemoteUserImpl parseUser(Map<?,?> map){
		RemoteUserImpl user = new RemoteUserImpl((String)map.get("username"));
		user.setProperty(UserProperty.USER_FULLNAME, (String)map.get("displayName"));
		user.setProperty(UserProperty.USER_EMAIL, (String)map.get("email"));
		return user;
	}
	
	private DbInfo parseDatabase(Map<?,?> map){
		DbInfo db = new DbInfo();
		db.setName((String)map.get("name"));
		db.setServerUrl((String)map.get("serverUrl"));
		db.setDataUrl((String)map.get("dataUrl"));
		db.setInsightsUrl((String)map.get("insightsUrl"));
		return db;
	}
	
	private HashMap<String, GrantedAuthority> parseAccessRights(Map<?,?> map){
		return null;
	}
	
	
	
}
