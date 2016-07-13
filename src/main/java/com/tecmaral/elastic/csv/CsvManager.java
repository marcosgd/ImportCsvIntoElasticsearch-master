package com.tecmaral.elastic.csv;

import java.util.List;

public interface CsvManager {

	public List<String> headers() throws Exception;
	public List<String> line() throws Exception;
	
}
