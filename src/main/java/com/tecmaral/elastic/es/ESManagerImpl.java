package com.tecmaral.elastic.es;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;


public class ESManagerImpl implements ESManager {

	public static final Log log = LogFactory.getLog(ESManagerImpl.class);

	private String elasticsearcHost;
	private String elasticsearchNombreCluster;
	private String indice;
	private String tipoDocumento;
	private Integer bulkSize;
	private Integer bulkConcurrentRequests;
	private BulkProcessor bulk;
	private Client client;
	
	public static final String OUT_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String IN_DATE_PATTERN = "dd/mm/yyyy HH:mm:ss";
	
	
	public ESManagerImpl setElasticsearcHost(String elasticsearcHost) {
		this.elasticsearcHost = elasticsearcHost;
		return this;
	}
	public ESManagerImpl setElasticsearchNombreCluster(String elasticsearchNombreCluster) {
		this.elasticsearchNombreCluster = elasticsearchNombreCluster;
		return this;
	}
	public ESManagerImpl setIndice(String indice) {
		this.indice = indice;
		return this;
	}
	public ESManagerImpl setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
		return this;
	}
	public ESManagerImpl setBulkSize(Integer bulkSize) {
		this.bulkSize = bulkSize;
		return this;
	}
	public ESManagerImpl setBulkConcurrentRequests(Integer bulkConcurrentRequests) {
		this.bulkConcurrentRequests = bulkConcurrentRequests;
		return this;
	}

	//public ESManagerImpl(String elasticsearcHost, String elasticsearchNombreCluster, String indice, String tipoDocumento) {}
	public ESManagerImpl() {}
	
	@SuppressWarnings("resource")
	@PostConstruct
	private void init() throws Exception {
//		Settings settings = ImmutableSettings.settingsBuilder()
//			.put("cluster.name", elasticsearchNombreCluster)
//			.put("client.transport.sniff", true)
//			.build();
		
		Settings settings = Settings.settingsBuilder()
				.put("cluster.name", elasticsearchNombreCluster)
		        .put("client.transport.sniff", true).build();
//		//client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(elasticsearcHost, 9300));
//		client = new TransportClient(settings)..addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host1"), 9300))
		this.client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticsearcHost), 9300));
//		PutMappingRequestBuilder pmrb = this.client.admin().indices().preparePutMapping(this.indice)
//                .setType(this.tipoDocumento);
//		  pmrb.setSource(this.getMappingSource());
//		PutMappingResponse putMappingResponse = pmrb.execute().actionGet();
//		if (!putMappingResponse.isAcknowledged()) {
//			log.error("Could not create index [" + this.indice + " ].");
//        } else {
//        	log.debug("Successfully created index [" + this.tipoDocumento + " ].");
//        }
		bulk = prepareBulk();
	}
	
	@PreDestroy
	public void shutdown() {
		try {
			bulk.awaitClose(15, TimeUnit.MINUTES);
		} catch (InterruptedException e) {e.printStackTrace();}
		client.close();
	}
	
	@Override
	public void addBulk(List<String> headers, List<String> values) throws Exception {
		XContentBuilder json = jsonBuilder().startObject();
		for (int i=0;i<headers.size();i++) {
			String header = headers.get(i).toLowerCase();
			String value = values.get(i);
			if ("initdate".equals(header)){
				value = toString(value,IN_DATE_PATTERN,OUT_DATE_PATTERN);
			}
			json.field(header, value);
		}
		
		IndexRequest index= new IndexRequest(this.indice, this.tipoDocumento);
			bulk.add(index.source(json));
	}

	
	//	private String getMappingSource() {
	//		
	//		String mapping="";
	//		try {
	//			mapping = XContentFactory.jsonBuilder().startObject().startObject(this.tipoDocumento).startObject("properties")
	//			        .startObject("location").field("type", "geo_point").endObject()
	//			        .startObject("language").field("type", "string").endObject()
	//			        .startObject("user").field("type", "string").endObject()
	//			        .startObject("mention").field("type", "string").endObject()
	//			        .startObject("in_reply").field("type", "not_analyzed").endObject()
	//			        .startObject("retweet").field("type", "string").endObject()
	//			        .endObject().endObject().endObject().string();
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		return mapping;
	//		
	//	}
	
	
	private BulkProcessor prepareBulk() throws Exception {
		return bulk = BulkProcessor.builder(this.client, new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long id, BulkRequest bulk) {
				log.info(String.format("Bulk %d preparado para ser lanzado con un size de %d bytes", id, bulk.estimatedSizeInBytes()));
			}			
			@Override
			public void afterBulk(long id, BulkRequest bulk, Throwable exc) {
				log.error(String.format("Bulk %d procesado con error: %s", id, exc.getMessage() ));
			}
			@Override
			public void afterBulk(long id, BulkRequest bulkReq, BulkResponse bulkResp) {
				log.info(String.format("Bulk %d procesado, en %d milisegundos", id, bulkResp.getTookInMillis() ));
				if (bulkResp.hasFailures()) {
					log.error(String.format("Bulk %d procesado con fallos en %d milisegundos", id, bulkResp.getTookInMillis() ));
					log.error(bulkResp.buildFailureMessage());
				}
			}
		})
		.setBulkActions(this.bulkSize)
		.setConcurrentRequests(this.bulkConcurrentRequests)
		.build();
	}	
	
	 
	public static Date toDate(String fecha, DateFormat df)
	{
		Date date = null;
		try
		{
			if(df!=null && fecha!=null && fecha.trim().length()>0) {
				date = df.parse(fecha);
			}
		}
		catch (Exception e)
		{
			log.error("Error en el parseo de la fecha " + fecha + " con " + df);
		}
		return date;
	}
	
	public static String toString(Date date, String pattern)
	{
		String fecha = null;
		try
		{
			if(date!=null && pattern!=null && pattern.trim().length()>0)
			{
				DateFormat df = new SimpleDateFormat(pattern);
				fecha = df.format(date);
			}
		}
		catch(Exception e)
		{
			log.error("Error al dar formato a la fecha: " + date);
		}
		return fecha;
	}
	
	public static Date toDate(String fecha, String pattern)
	{
		Date date = null;
		try
		{
			if(fecha!=null && fecha.trim().length()>0 && pattern!=null && pattern.trim().length()>0)
			{
				DateFormat df = new SimpleDateFormat(pattern);
				date = df.parse(fecha);
			}
		}
		catch(Exception e)
		{
			log.error("Error en el parseo de la fecha " + fecha + " con el patrón " + pattern);
		}
		return date;
	}
	
	public static String toString(String inDate, String inPattern, String outPattern)
	{
		Date date = toDate(inDate, inPattern);
		String outDate = toString(date, outPattern);
		return outDate+".000Z";
	}


}