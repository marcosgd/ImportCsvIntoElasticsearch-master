package com.tecmaral.elastic;

import java.util.List;

import javax.annotation.Resource;

import com.tecmaral.elastic.csv.CsvManager;
import com.tecmaral.elastic.es.ESManager;

public class LoadCsvImpl implements LoadCsv {

	@Resource
	private CsvManager csvManagerImpl;
	@Resource
	private ESManager eSManagerImpl;
	
	@Override
	public void process() throws Exception {
		List<String> headers = csvManagerImpl.headers();
		List<String> columns = null;
		while ((columns = csvManagerImpl.line()) != null) {
			eSManagerImpl.addBulk(headers, columns);
		}
	}

}
