package org.pentaho.di.sdk.steps.sensor;

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

import rpi.sensehat.api.SenseHat;
import rpi.sensehat.api.dto.IMUData;
import rpi.sensehat.api.dto.IMUDataRaw;

public class SensorStep extends BaseStep implements StepInterface  {
	  private static final Class<?> PKG = SensorStepMeta.class; // for i18n purposes
	  
	  private RowMetaInterface outputMeta;
	  private SenseHat senseHat = null;
	  private int count = 0;
	  
	  public SensorStep( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
	    super( s, stepDataInterface, c, t, dis );
	  }

	  public boolean init( StepMetaInterface smi, StepDataInterface sdi )  {
	    // Casting to step-specific implementation classes is safe
	    SensorStepMeta meta = (SensorStepMeta) smi;
	    SensorStepData data = (SensorStepData) sdi;
	    
	    if ( !super.init( meta, data ) ) {
	      return false;
	    }
	    outputMeta = new RowMeta();
	    try {
			meta.getFields(outputMeta, getStepname(), null, null, this, repository, metaStore);
		} catch (KettleStepException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	    
	    senseHat = new SenseHat();
	    count = 0;
	    
	    return true;
	  }

	  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
		  SensorStepMeta meta = (SensorStepMeta) smi;
		  SensorStepData data = (SensorStepData) sdi;
		  
		  super.dispose( meta, data );
	  }

	  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
	    // safely cast the step settings (meta) and runtime info (data) to specific implementations 
		  SensorStepMeta meta = (SensorStepMeta) smi;
//		  SensorStepData data = (SensorStepData) sdi;

	    if ( first ) {
	      first = false;
	      getRow();
	    }
	    
	    if(meta.getMaxCount() == 0 || count < meta.getMaxCount())
	    {
		    count++;

		    Object[] r = RowDataUtil.allocateRowData(outputMeta.getFieldNames().length);
		    r[0] = (double)senseHat.environmentalSensor.getHumidity();
		    r[1] = (double)senseHat.environmentalSensor.getPressure();
		    r[2] = (double)senseHat.environmentalSensor.getTemperature();
		    
		    IMUDataRaw data =senseHat.IMU.getAccelerometerRaw();
		    r[3] = (double)data.getX();
		    r[4] = (double)data.getY();
		    r[5] = (double)data.getZ();
	
		    putRow(outputMeta, r);
		    
		    // log progress if it is time to to so
		    if ( checkFeedback( getLinesRead() ) ) {
		      logBasic( BaseMessages.getString( PKG, "Sensor.Linenr", getLinesRead() ) ); // Some basic logging
		    }
	
		    return true;
	    }
	    else
	    {
            setOutputDone(); // signal end to receiver(s)
            return false;
	    }
	  }
}
