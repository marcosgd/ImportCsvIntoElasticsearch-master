package com.tecmaral.elastic.es;

import java.util.List;


public interface ESManager {
	
	public void addBulk(List<String> headers, List<String> values) throws Exception;
	
}
