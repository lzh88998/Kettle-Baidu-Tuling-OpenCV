package org.pentaho.di.sdk.steps.speaker;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class SpeakerStep extends BaseStep implements StepInterface {
	  private static final Class<?> PKG = SpeakerStepMeta.class; // for i18n purposes
	  
	  private SourceDataLine source = null;
	  private int index = -1;
	  private RowMetaInterface outputMeta;
	  
	  public SpeakerStep( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
	    super( s, stepDataInterface, c, t, dis );
	  }

	  public boolean init( StepMetaInterface smi, StepDataInterface sdi )  {
	    // Casting to step-specific implementation classes is safe
	    SpeakerStepMeta meta = (SpeakerStepMeta) smi;
	    SpeakerStepData data = (SpeakerStepData) sdi;
	    
	    if ( !super.init( meta, data ) ) {
	      return false;
	    }

	    if(null == meta.getInputField() || "" == meta.getInputField()) {
	    	System.out.println("Input field have not been configured!");
	    	return false;
	    }
	    
	    if(null == meta.getSourceDataLine()) {
	    	System.out.println("Speaker device have not been configured!");
	    	return false;
	    }

	    source = meta.getSourceDataLine();
	    try {
	    	source.open();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	    source.start();
	    
	    return true;
	  }

	  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
		  SpeakerStepMeta meta = (SpeakerStepMeta) smi;
		  SpeakerStepData data = (SpeakerStepData) sdi;
	    
		  if(null != source) {
			  source.stop();
			  source.close();
			  source = null;
		  }
		  
		  super.dispose( meta, data );
	  }

	  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
	    // safely cast the step settings (meta) and runtime info (data) to specific implementations 
		  SpeakerStepMeta meta = (SpeakerStepMeta) smi;
//		  SpeakerStepData data = (SpeakerStepData) sdi;

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
	    
	    byte[] buffer;
	    if(getInputRowMeta().getValueMeta(index).isBinary()) {
		    buffer = getInputRowMeta().getBinary(r, index);
	    } else if(getInputRowMeta().getValueMeta(index).isString()) {
	    	buffer = java.util.Base64.getDecoder().decode(getInputRowMeta().getString(r, index));
	    } else {
	    	throw new KettleException("Invalid input type!");
	    }
	    
	    source.write(buffer, 0, buffer.length);
	    
	    putRow(outputMeta, r);

	    // log progress if it is time to to so
	    if ( checkFeedback( getLinesRead() ) ) {
	      logBasic( BaseMessages.getString( PKG, "Speaker.Linenr", getLinesRead() ) ); // Some basic logging
	    }

	      if(isStopped()) {
	          setOutputDone(); // signal end to receiver(s)
	          
	          if(null != source) {
	        	  source.stop();
	        	  source.close();
	        	  source = null;
	          }
	          
	      	return false;
	      }
	      
	      return true;
	  }
}
