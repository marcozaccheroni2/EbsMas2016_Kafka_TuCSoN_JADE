package utils;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Json {
	
	ArrayList<String> json;
	
	public Json(){
		this.json = new ArrayList<String>();
	}
	
	public void add(String key, String value){
		this.json.add("\"" + key + "\":\"" + value + "\"");
	}
	
	public void add(String key, int value){
		this.json.add("\"" + key + "\":" + value);
	}
	
	public String toString(){
		return "{" + String.join(", ", this.json) + "}";
	}
	
	static public JsonNode parse(String msg) throws JsonProcessingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(msg);
	}
	
}
