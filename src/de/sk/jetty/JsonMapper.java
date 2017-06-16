package de.sk.jetty;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMapper {
	
	static class JsonMapperException extends Exception {
		private static final long serialVersionUID = 1L;
		
		private Exception e;
		
		public JsonMapperException(Exception e) {
			this.e = e;
		}

		@Override
		public String toString() {
			return e.toString();
		}
		
		@Override
		public String getMessage() {
			return e.getMessage();
		}
	}

	private ObjectMapper mapper;
	
	public JsonMapper() {
		mapper = new ObjectMapper();
	}
	
	public <T> T readValue(String content, Class<T> clazz) throws JsonMapperException {
		try {
			return (T) mapper.readValue(content, clazz);
		} catch (IOException e) {
			throw new JsonMapperException(e);
		}
	}
	
	public <T> String writeValue(T value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
