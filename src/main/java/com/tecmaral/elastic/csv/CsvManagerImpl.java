package com.tecmaral.elastic.csv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvManagerImpl implements CsvManager {

	@SuppressWarnings("unused")
	private String filePath;
	private String charSeparator;
	private String encodingFile;
	private BufferedReader br;
	private List<String> headers;
	
	public CsvManagerImpl(String filePath, String charSepator, String charset) throws Exception {
		this.filePath = filePath;
		this.charSeparator = charSepator;
		this.encodingFile = charset;
		br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), encodingFile));		
	}
	
	private List<String> columns(String line) {
		List<String> columns = new ArrayList<String>();
		Scanner scan = new Scanner(line);
		if (this.charSeparator.equals("|"))
    		scan.useDelimiter("\\"+this.charSeparator);
    	else
    		scan.useDelimiter(this.charSeparator);
		while (scan.hasNext()) {
    		columns.add(scan.next().trim());
    	}
		scan.close();
		// Fix last columns without data
		if (headers != null && headers.size() -1 == columns.size())
			columns.add("");
		
		return columns;
	}
	@Override
	public List<String> headers() throws Exception {
		String line = br.readLine();
		this.headers = columns(line);
		return this.headers;
	}

	@Override
	public List<String> line() throws Exception {
		String line= br.readLine();
		if (line == null) {
			br.close();
			return null;
	    }
		return columns(line);
	}

}
