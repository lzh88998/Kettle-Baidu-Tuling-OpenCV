package org.pentaho.di.sdk.steps.recorder;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@Step(
		  id = "Recorder",
		  name = "Recorder.Name",
		  description = "Recorder.TooltipDesc",
		  image = "org/pentaho/di/sdk/steps/recorder/resources/recorder.svg",
		  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Experimental",
		  i18nPackageName = "org.pentaho.di.sdk.steps.recorder"
		  )
public class RecorderStepMeta  extends BaseStepMeta implements StepMetaInterface {
	  private static final Class<?> PKG = RecorderStepMeta.class; // for i18n purposes

	  private int voiceParameter = 10;
	  private int recordingMode = 0;
	  private String inputDevice = "";
	  
	  public RecorderStepMeta() {
	    super();
	  }

	  public StepDialogInterface getDialog( Shell shell, StepMetaInterface meta, TransMeta transMeta, String name ) {
	    return new RecorderStepDialog( shell, meta, transMeta, name );
	  }

	  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
	      Trans disp ) {
	    return new RecorderStep( stepMeta, stepDataInterface, cnr, transMeta, disp );
	  }

	  public StepDataInterface getStepData() {
	    return new RecorderStepData();
	  }

	  public void setDefault() {
		  setVoiceParameter(10);
		  setRecordingMode(0);
		  setInputDevice("");
	  }
	  
	  // Audio Format
	  private float sampleRate = 16000;
	  private int sampleSizeInBits = 16;
	  private int channels = 1;
	  private boolean signed = true;
	  private boolean bigEndian = false;
	  
	  private AudioFormat getAudioFormat() {
		  return new AudioFormat (sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	  }

	  public void setVoiceParameter(int threshold) {
		  voiceParameter = threshold;
	  }
	  
	  public int getVoiceParameter() {
		  return voiceParameter;
	  }
	  
	  public void setRecordingMode(int mode) {
		  recordingMode = mode;
	  }
	  
	  public int getRecordingMode() {
		  return recordingMode;
	  }
	  
	  public void setInputDevice(String dev) {
		  inputDevice = dev;
	  }
	  
	  public String getInputDevice() {
		  return inputDevice;
	  }

	  public Object clone() {
		  RecorderStepMeta retval = (RecorderStepMeta)super.clone();
		  retval.setVoiceParameter(voiceParameter);
		  retval.setRecordingMode(recordingMode);
		  retval.setInputDevice(inputDevice);
		  
		  return retval;
	  }

	  public String getXML() throws KettleValueException {
	    StringBuilder xml = new StringBuilder();

	    // only one field to serialize
	    xml.append( XMLHandler.addTagValue( "voiceparameter", voiceParameter) );
	    xml.append( XMLHandler.addTagValue( "recordingmode", recordingMode) );
	    xml.append( XMLHandler.addTagValue( "inputdevice", inputDevice) );

	    return xml.toString();
	  }

	  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
	    try {
	    	setVoiceParameter( Integer.parseInt(XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "voiceparameter" ) ) ) );
	    	setRecordingMode( Integer.parseInt(XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "recordingmode" )  ) ) );
	    	setInputDevice( XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "inputdevice" ) ) );
	    } catch ( Exception e ) {
	      throw new KettleXMLException( "Recorder plugin unable to read step info from XML node", e );
	    }
	  }

	  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
	      throws KettleException {
	    try {
	      rep.saveStepAttribute( id_transformation, id_step, "voiceparameter", voiceParameter ); //$NON-NLS-1$
	      rep.saveStepAttribute( id_transformation, id_step, "recordingmode", recordingMode ); //$NON-NLS-1$
	      rep.saveStepAttribute( id_transformation, id_step, "inputdevice", inputDevice ); //$NON-NLS-1$
	    } catch ( Exception e ) {
	      throw new KettleException( "Unable to save step into repository: " + id_step, e );
	    }
	  }

	  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
	      throws KettleException {
	    try {
	    	voiceParameter = (int)rep.getStepAttributeInteger(id_step, "voiceparameter");
	    	recordingMode = (int) rep.getStepAttributeInteger(id_step, "recordingmode");
	    	inputDevice  = rep.getStepAttributeString( id_step, "inputdevice" ); //$NON-NLS-1$
	    } catch ( Exception e ) {
	      throw new KettleException( "Unable to load step from repository", e );
	    }
	  }

	  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
	      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

		  ValueMetaInterface v = new ValueMetaString( getBinaryStreamField() );
		  v.setOrigin( name );
		  inputRowMeta.addValueMeta( v );
	  }
	  
	  public String[] getTargetLineNames() {
		  ArrayList<String> ret = new ArrayList<String>();
		    Mixer.Info[] infos = AudioSystem.getMixerInfo();
		    for(int i = 0; i < infos.length; i++) {
	    		try
	    		{
	    			TargetDataLine target = AudioSystem.getTargetDataLine(getAudioFormat(), infos[i]);
	    			target.open();
	    			target.start();
	    			target.stop();
	    			target.close();
	    			
	    			ret.add(infos[i].getName());
	    			System.out.println(infos[i].getName());
	    		}
	    		catch(Exception e)
	    		{
	    		}
		    }
		    return ret.toArray(new String[] {});
	  }
	  
	  public TargetDataLine getTargetDataLine() {
		  System.out.println("Input Device:"+inputDevice);
		    Mixer.Info[] infos = AudioSystem.getMixerInfo();
		    for(int i = 0; i < infos.length; i++) {
				  System.out.println("Info Device:"+infos[i].getName());
		    	if(infos[i].getName().compareTo(inputDevice) == 0) {
		    		try
		    		{
		    			TargetDataLine target = AudioSystem.getTargetDataLine(getAudioFormat(), infos[i]);
		    			
		    			return target;
		    		}
		    		catch(Exception e)
		    		{
		    			return null;
		    		}
		    	}
		    }
		    return null;
	  }

	  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
	      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
	      IMetaStore metaStore ) {
	    CheckResult cr;
	    
	    if(inputDevice == null || inputDevice == "") {
		    cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
		    		BaseMessages.getString( PKG, "Recorder.CheckResult.ReceivingRows.ERROR" ), stepMeta );
		    		remarks.add( cr );
		    return;
	    }
	    
	    if(null == getTargetDataLine()) {
		    cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
		    		BaseMessages.getString( PKG, "Recorder.CheckResult.ReceivingRows.ERROR" ), stepMeta );
		    		remarks.add( cr );
		    return;
	    }

    	cr = new CheckResult( CheckResult.TYPE_RESULT_OK,
			        BaseMessages.getString( PKG, "Recorder.CheckResult.ReceivingRows.OK" ), stepMeta );
		remarks.add( cr );
	  }
	  
	  public String getBinaryStreamField() {
		  return "Microphone Stream";
	  }

	  @Override
	  public StepIOMetaInterface getStepIOMeta() {
	    return new StepIOMeta( false, true, false, false, false, false );
	  }
}
