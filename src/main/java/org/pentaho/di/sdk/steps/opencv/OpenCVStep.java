package org.pentaho.di.sdk.steps.opencv;

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

import org.opencv.videoio.*;
import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.*;

public class OpenCVStep extends BaseStep implements StepInterface {
	
	static 
	{
		System.out.print(System.getProperty("java.library.path"));
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

  private static final Class<?> PKG = OpenCVStepMeta.class; // for i18n purposes

  private Mat m = new Mat();
  private CascadeClassifier classifier = null;
  private Size min = new Size(50, 50);
  private Size max = new Size(300, 300);
  private Scalar red = new Scalar(255, 0, 0);
  private VideoCapture cap = null;
  private RowMetaInterface metaInfo = null;
  private long count = 0;
  
  public OpenCVStep( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
    super( s, stepDataInterface, c, t, dis );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    // Casting to step-specific implementation classes is safe
    OpenCVStepMeta meta = (OpenCVStepMeta) smi;
    OpenCVStepData data = (OpenCVStepData) sdi;
    
    if ( !super.init( meta, data ) ) {
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
    

	// Add any step-specific initialization that may be needed here
    // "C:\\models\\haarcascade_frontalface_alt2.xml"
    if(null != meta.getFilePath() && "" != meta.getFilePath()) {
	    classifier = new CascadeClassifier();
		classifier.load(meta.getFilePath());
    } else {
    	classifier = null;
    }

	cap = new VideoCapture(0);
	count = 0;

    return true;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
	  OpenCVStepMeta meta = (OpenCVStepMeta) smi;
	  OpenCVStepData data = (OpenCVStepData) sdi;
    
	  if(null != cap) {
			HighGui.destroyAllWindows();
			cap.release();
			cap = null;
	  }
	  
	  super.dispose( meta, data );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    // safely cast the step settings (meta) and runtime info (data) to specific implementations 
	  OpenCVStepMeta meta = (OpenCVStepMeta) smi;
//	  OpenCVStepData data = (OpenCVStepData) sdi;

    if ( first ) {
      first = false;
      getRow();
//      metaInfo = getInputRowMeta().clone();
//      meta.getFields( metaInfo, getStepname(), null, null, this, repository, metaStore );
    } else {
        if(isStopped() || !HighGui.windows.get("test").frame.isVisible()) {
            setOutputDone(); // signal end to receiver(s)
            
            if(null != cap) {
            	HighGui.destroyAllWindows();
            	cap.release();
            	cap = null;
            }
            
        	return false;
        }
    }
    
    cap.read(m);
	Mat g = new Mat();
	Imgproc.cvtColor(m, g, Imgproc.COLOR_BGR2GRAY);
	MatOfRect faces = new MatOfRect();
	
	if(null != classifier) {
		classifier.detectMultiScale(g, faces, 1.1, 4, Objdetect.CASCADE_DO_ROUGH_SEARCH, min, max);
	    
		Rect[] tempFaces = faces.toArray();
		for(int i = 0; i < tempFaces.length; i++) {

			Object[] r = RowDataUtil.allocateRowData(metaInfo.getFieldNames().length);
		    r[0] = count;
		    r[1] = (long)i;
		    r[2] = (long)tempFaces[i].x;
		    r[3] = (long)tempFaces[i].y;
		    r[4] = (long)tempFaces[i].width;
		    r[5] = (long)tempFaces[i].height;
			
		    putRow(metaInfo, r );

			Imgproc.rectangle(m, tempFaces[i], red, 2, 8, 0);
		}
	}
	
	HighGui.imshow("test", m);
	HighGui.waitKey(-1);
    count++;

    // log progress if it is time to to so
    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( BaseMessages.getString( PKG, "OpenCV.Linenr", getLinesRead() ) ); // Some basic logging
    }

    // indicate that processRow() should be called again
    return true;
  }
}
