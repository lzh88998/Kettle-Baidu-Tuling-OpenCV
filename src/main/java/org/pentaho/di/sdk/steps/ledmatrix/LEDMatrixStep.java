package org.pentaho.di.sdk.steps.ledmatrix;

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

import rpi.sensehat.api.SenseHat;
import rpi.sensehat.api.dto.Color;

public class LEDMatrixStep extends BaseStep implements StepInterface  {
	  private static final Class<?> PKG = LEDMatrixStepMeta.class; // for i18n purposes
	  
	  private RowMetaInterface outputMeta;
	  private int index = -1;
	  
	  private SenseHat senseHat = null;
	  
	  public LEDMatrixStep( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
	    super( s, stepDataInterface, c, t, dis );
	  }

	  public boolean init( StepMetaInterface smi, StepDataInterface sdi )  {
	    // Casting to step-specific implementation classes is safe
	    LEDMatrixStepMeta meta = (LEDMatrixStepMeta) smi;
	    LEDMatrixStepData data = (LEDMatrixStepData) sdi;
	    
	    if ( !super.init( meta, data ) ) {
	      return false;
	    }
	    
	    if(null == meta.getInputField() || "" == meta.getInputField()) {
	    	System.out.println("Input field have not been configured!");
	    	return false;
	    }

	    senseHat = new SenseHat();
	    
	    return true;
	  }

	  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
		  LEDMatrixStepMeta meta = (LEDMatrixStepMeta) smi;
		  LEDMatrixStepData data = (LEDMatrixStepData) sdi;
		  
		  super.dispose( meta, data );
	  }

	  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
	    // safely cast the step settings (meta) and runtime info (data) to specific implementations 
		  LEDMatrixStepMeta meta = (LEDMatrixStepMeta) smi;
//		  LEDMatrixStepData data = (LEDMatrixStepData) sdi;

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
	    
	    //senseHat.ledMatrix.showMessage(text);
	    if(null != text && "" != text) {
	    	senseHat.ledMatrix.showLetter(text.substring(0, 1), Color.RED, Color.of(0, 0, 0));
	    } else {
	    	senseHat.ledMatrix.clear();
	    }

	    putRow(outputMeta, r);
	    
	    // log progress if it is time to to so
	    if ( checkFeedback( getLinesRead() ) ) {
	      logBasic( BaseMessages.getString( PKG, "LEDMatrix.Linenr", getLinesRead() ) ); // Some basic logging
	    }

	    return true;
	  }
}
