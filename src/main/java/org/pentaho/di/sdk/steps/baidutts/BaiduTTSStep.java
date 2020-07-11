package org.pentaho.di.sdk.steps.baidutts;

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

public class BaiduTTSStep extends BaseStep implements StepInterface {
	  private static final Class<?> PKG = BaiduTTSStepMeta.class; // for i18n purposes
	  
	  private RowMetaInterface outputMeta;
	  private int index = -1;
	  private Proxy proxy = null;
	  
	  public BaiduTTSStep( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
	    super( s, stepDataInterface, c, t, dis );
	  }

	  public boolean init( StepMetaInterface smi, StepDataInterface sdi )  {
	    // Casting to step-specific implementation classes is safe
	    BaiduTTSStepMeta meta = (BaiduTTSStepMeta) smi;
	    BaiduTTSStepData data = (BaiduTTSStepData) sdi;
	    
	    if ( !super.init( meta, data ) ) {
	      return false;
	    }
	    
	    if(null == meta.getToken() || "" == meta.getToken()) {
	    	System.out.println("Token have not been configured!");
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
		  BaiduTTSStepMeta meta = (BaiduTTSStepMeta) smi;
		  BaiduTTSStepData data = (BaiduTTSStepData) sdi;
		  
		  super.dispose( meta, data );
	  }

	  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
	    // safely cast the step settings (meta) and runtime info (data) to specific implementations 
		  BaiduTTSStepMeta meta = (BaiduTTSStepMeta) smi;
//		  BaiduTTSStepData data = (BaiduTTSStepData) sdi;

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

	    String text = outputMeta.getString(r, index);
	    try {
			text = URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    String url = "http://tsn.baidu.com/text2audio?per=0&spd=5&pit=5&vol=5&cuid=1234567JAVA&aue=4&lan=zh&ctp=1&tok="+meta.getToken()+"&tex="+text;
	    if(url.length() > 1024) {
	    	url = url.substring(0, 1024);
	    }
	    
	    HttpURLConnection conn;
	    BufferedInputStream bis;
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
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
			throw new KettleException("BaiduTTS server error, response code: "+responseCode);
		}
		
		byte[] bytes = buf.toByteArray();
		Object[] ret = new Object[1];
		ret[0] = java.util.Base64.getEncoder().encodeToString(bytes);
		//ret[0] = bytes;
		
		r = RowDataUtil.addRowData(r, getInputRowMeta().size(), ret);

	    putRow(outputMeta, r);
	    
	    // log progress if it is time to to so
	    if ( checkFeedback( getLinesRead() ) ) {
	      logBasic( BaseMessages.getString( PKG, "BaiduTTS.Linenr", getLinesRead() ) ); // Some basic logging
	    }

	    return true;
	  }
}
