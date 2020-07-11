package org.pentaho.di.sdk.steps.tuling;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class TulingStep extends BaseStep implements StepInterface {
	  private static final Class<?> PKG = TulingStepMeta.class; // for i18n purposes
	  
	  private RowMetaInterface outputMeta;
	  private int index = -1;
	  private Proxy proxy = null;
	  
	  public TulingStep( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
	    super( s, stepDataInterface, c, t, dis );
	  }

	  public boolean init( StepMetaInterface smi, StepDataInterface sdi )  {
	    // Casting to step-specific implementation classes is safe
	    TulingStepMeta meta = (TulingStepMeta) smi;
	    TulingStepData data = (TulingStepData) sdi;
	    
	    if ( !super.init( meta, data ) ) {
	      return false;
	    }
	    
	    if(null == meta.getKey() || "" == meta.getKey()) {
	    	System.out.println("Key have not been configured!");
	    	return false;
	    }
	    
	    if(null == meta.getInputField() || "" == meta.getInputField()) {
	    	System.out.println("Input field have not been configured!");
	    	return false;
	    }
	    
/*	    if(null != meta.getProxyHost() && null != meta.getProxyPort()) {
	    	proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(meta.getProxyHost(), meta.getProxyPort().intValue()));
	    }*/
	    
	    return true;
	  }

	  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
		  TulingStepMeta meta = (TulingStepMeta) smi;
		  TulingStepData data = (TulingStepData) sdi;
		  
		  super.dispose( meta, data );
	  }

	  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
	    // safely cast the step settings (meta) and runtime info (data) to specific implementations 
		  TulingStepMeta meta = (TulingStepMeta) smi;
//		  TulingStepData data = (TulingStepData) sdi;

	    Object[] r = getRow();
	    if(null == r) {
	    	setOutputDone();
	    	return false;
	    }
	    
	    if ( first ) {
	      first = false;
	      outputMeta = getInputRowMeta().clone();
	      meta.getFields( outputMeta, getStepname(), null, null, this, repository, metaStore );
		  index = getInputRowMeta().indexOfValue(meta.getInputField());
	    }

	    String question = outputMeta.getString(r, index);
	    try {
			question = URLEncoder.encode(question, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    String url = "http://www.tuling123.com/openapi/api?key="+meta.getKey()+"&info="+question;
	    
	    if(url.length() > 1024) {
	    	url = url.substring(0, 1024);
	    }
	    
        HttpURLConnection conn;
        BufferedInputStream bis;
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		String ret;
		int responseCode;
		try {
			if(null == proxy) {
				conn = (HttpURLConnection)new URL(url).openConnection();
			} else {
				conn = (HttpURLConnection)new URL(url).openConnection(proxy);
			}
	        conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
	        conn.setDoOutput(true);
			conn.getOutputStream().close();
			
			responseCode = conn.getResponseCode();

			bis = new BufferedInputStream(conn.getInputStream());

	        int result = bis.read();
	        while(result != -1) {
	            buf.write((byte) result);
	            result = bis.read();
	        }
	        
	        ret = buf.toString("UTF-8");
	        conn.disconnect();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new KettleException("Wrong input data!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new KettleException("I/O Exception! Please check network status!");
		}
		
		if(200 != responseCode) {
			throw new KettleException("Tuling server error, response code: "+responseCode);
		}
		
		JSONParser parser = new JSONParser();
		JSONObject obj;
		try {
			 obj = (JSONObject) parser.parse(ret);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new KettleException("Cannot parse response json content! "+ret);
		}
		
		r = RowDataUtil.addRowData(r, getInputRowMeta().size(), new String[] {obj.get("text").toString()});

	    putRow(outputMeta, r);
	    
	    // log progress if it is time to to so
	    if ( checkFeedback( getLinesRead() ) ) {
	      logBasic( BaseMessages.getString( PKG, "Tuling.Linenr", getLinesRead() ) ); // Some basic logging
	    }

	    return true;
	  }
}
