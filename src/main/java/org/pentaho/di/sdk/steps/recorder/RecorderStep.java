package org.pentaho.di.sdk.steps.recorder;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class RecorderStep extends BaseStep implements StepInterface {
	  private static final Class<?> PKG = RecorderStepMeta.class; // for i18n purposes
	  
	  private TargetDataLine target = null;
	  private RowMetaInterface metaInfo = null;
	  
	  public RecorderStep( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
	    super( s, stepDataInterface, c, t, dis );
	  }

	  public boolean init( StepMetaInterface smi, StepDataInterface sdi )  {
	    // Casting to step-specific implementation classes is safe
	    RecorderStepMeta meta = (RecorderStepMeta) smi;
	    RecorderStepData data = (RecorderStepData) sdi;
	    
	    if ( !super.init( meta, data ) ) {
	      return false;
	    }
	    
	    if(null == meta.getTargetDataLine()) {
	    	System.out.println("Speaker device have not been configured!");
	    	return false;
	    }
	    
	    metaInfo = new RowMeta();
	    try {
			meta.getFields(metaInfo, getStepname(), null, null, this, repository, metaStore);
		} catch (KettleStepException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
	    
	    target = meta.getTargetDataLine();
	    try {
			target.open();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	    target.start();

	    return true;
	  }

	  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
		  RecorderStepMeta meta = (RecorderStepMeta) smi;
		  RecorderStepData data = (RecorderStepData) sdi;
	    
		  if(null != target) {
			  target.stop();
			  target.close();
			  target = null;
		  }
		  
		  super.dispose( meta, data );
	  }

	  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
	    // safely cast the step settings (meta) and runtime info (data) to specific implementations 
		  RecorderStepMeta meta = (RecorderStepMeta) smi;
//		  RecorderStepData data = (RecorderStepData) sdi;

	    if ( first ) {
	      first = false;
	      getRow();
//	      metaInfo = getInputRowMeta().clone();
//	      meta.getFields( metaInfo, getStepname(), null, null, this, repository, metaStore );
	    } else {
            setOutputDone(); // signal end to receiver(s)
            
            if(null != target) {
            	target.stop();
            	target.close();
            	target = null;
            }
            
        	return false;
	    }
	    
	    byte[] buffer = new byte[2000]; // 2000 bytes for 1000 sample points @ 16bit
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        if(meta.getRecordingMode() == 1) {
            // Sound detect mode wait for start
            boolean bSpeak = false;
        	while(!isStopped() && !bSpeak) {
        		while(!isStopped() && target.available() < 2000);
    	    	int len = target.read(buffer, 0, buffer.length);

    	    	for(int i = 0; i < len; i +=2) {
    	    		int temp = buffer[i+1];
    	    		temp <<= 8;
    	    		temp += buffer[i];
    	    		if(Math.abs(temp) > meta.getVoiceParameter()) {
    	    			bSpeak = true;
    	    			break;
    	    		}
    	    	}
    		}

            if(isStopped()) {
                setOutputDone(); // signal end to receiver(s)
                
                if(null != target) {
                	target.stop();
                	target.close();
                	target = null;
                }
                
            	return false;
            }
        }
        
        // started recording
        boolean bExit = false;
        int counter = 0;
        while(!isStopped() && !bExit) {
    		while(!isStopped() && target.available() < 2000);
	    	int len = target.read(buffer, 0, buffer.length);
	    	if(meta.getRecordingMode() == 1) {
	    		// Sound detect mode
		    	boolean bSpeak = false;
		    	for(int i = 0; i < len; i +=2) {
		    		int temp = buffer[i+1];
		    		temp <<= 8;
		    		temp += buffer[i];
		    		if(Math.abs(temp) > meta.getVoiceParameter()) {
		    			bSpeak = true;
		    			break;
		    		}
		    	}
		    	
		    	if(bSpeak) {
		    		counter = 0;
		    	} else {
		    		counter++;
		    	}
		    	
		    	if(counter >= 16){
		    		bExit = true;
		    	} else {
		    		baos.write(buffer, 0, len);
		    	}
	    	} else {
	    		baos.write(buffer, 0, len);
	    		counter++;
	    		
	    		if(counter >= meta.getVoiceParameter()*16) { // 16 round is 1S @ 16K sample rate
	    			bExit = true;
	    		}
	    	}
        }

        if(isStopped()) {
            setOutputDone(); // signal end to receiver(s)
            
            if(null != target) {
            	target.stop();
            	target.close();
            	target = null;
            }
            
        	return false;
        }

		byte[] arr = baos.toByteArray();
		baos.reset();
		
		Object[] r = RowDataUtil.allocateRowData(metaInfo.getFieldNames().length);
		r[0] = java.util.Base64.getEncoder().encodeToString(arr);
//		r[0] = arr;
		
		putRow(metaInfo, r);

	    // log progress if it is time to to so
	    if ( checkFeedback( getLinesRead() ) ) {
	      logBasic( BaseMessages.getString( PKG, "Recorder.Linenr", getLinesRead() ) ); // Some basic logging
	    }

        return true;
	  }

}
