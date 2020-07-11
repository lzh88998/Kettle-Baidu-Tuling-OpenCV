package org.pentaho.di.sdk.steps.speaker;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

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
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@Step(
		  id = "Speaker",
		  name = "Speaker.Name",
		  description = "Speaker.TooltipDesc",
		  image = "org/pentaho/di/sdk/steps/speaker/resources/speaker.svg",
		  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Experimental",
		  i18nPackageName = "org.pentaho.di.sdk.steps.speakder"
		  )
public class SpeakerStepMeta  extends BaseStepMeta implements StepMetaInterface {
	  private static final Class<?> PKG = SpeakerStepMeta.class; // for i18n purposes

	  private String inputField = "";
	  private String outputDevice = "";
	  
	  public SpeakerStepMeta() {
	    super();
	  }

	  public StepDialogInterface getDialog( Shell shell, StepMetaInterface meta, TransMeta transMeta, String name ) {
	    return new SpeakerStepDialog( shell, meta, transMeta, name );
	  }

	  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
	      Trans disp ) {
	    return new SpeakerStep( stepMeta, stepDataInterface, cnr, transMeta, disp );
	  }

	  public StepDataInterface getStepData() {
	    return new SpeakerStepData();
	  }

	  public void setDefault() {
		  setInputField("");
		  setOutputDevice("");
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

	  public void setInputField(String field) {
		  inputField = field;
	  }
	  
	  public String getInputField() {
		  return inputField;
	  }
	  
	  public void setOutputDevice(String dev) {
		  outputDevice = dev;
	  }
	  
	  public String getOutputDevice() {
		  return outputDevice;
	  }
	  
	  public Object clone() {
		  SpeakerStepMeta retval = (SpeakerStepMeta)super.clone();
		  retval.setInputField(inputField);
		  
		  return retval;
	  }

	  public String getXML() throws KettleValueException {
	    StringBuilder xml = new StringBuilder();

	    // only one field to serialize
	    xml.append( XMLHandler.addTagValue( "inputfield", inputField) );
	    xml.append( XMLHandler.addTagValue( "outputdevice", outputDevice) );

	    return xml.toString();
	  }

	  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
	    try {
	    	setInputField( XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "inputfield" ) ) );
	    	setOutputDevice( XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "outputdevice" ) ) );
	    } catch ( Exception e ) {
	      throw new KettleXMLException( "Speaker plugin unable to read step info from XML node", e );
	    }
	  }

	  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
	      throws KettleException {
	    try {
	      rep.saveStepAttribute( id_transformation, id_step, "inputfield", inputField ); //$NON-NLS-1$
	      rep.saveStepAttribute( id_transformation, id_step, "outputdevice", outputDevice ); //$NON-NLS-1$
	    } catch ( Exception e ) {
	      throw new KettleException( "Unable to save step into repository: " + id_step, e );
	    }
	  }

	  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
	      throws KettleException {
	    try {
	    	inputField  = rep.getStepAttributeString( id_step, "inputfield" ); //$NON-NLS-1$
	    	outputDevice  = rep.getStepAttributeString( id_step, "outputdevice" ); //$NON-NLS-1$
	    } catch ( Exception e ) {
	      throw new KettleException( "Unable to load step from repository", e );
	    }
	  }

	  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
	      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
	  }
	  
	  public String[] getSourceLineNames() {
		  ArrayList<String> ret = new ArrayList<String>();
		    Mixer.Info[] infos = AudioSystem.getMixerInfo();
		    for(int i = 0; i < infos.length; i++) {
	    		try
	    		{
	    			SourceDataLine target = AudioSystem.getSourceDataLine(getAudioFormat(), infos[i]);
	    			target.open();
	    			target.start();
	    			target.stop();
	    			target.close();
	    			
	    			ret.add(infos[i].toString());
	    		}
	    		catch(Exception e)
	    		{
	    		}
		    }
		    return ret.toArray(new String[] {});
	  }
	  
	  public SourceDataLine getSourceDataLine() {
		    Mixer.Info[] infos = AudioSystem.getMixerInfo();
		    for(int i = 0; i < infos.length; i++) {
		    	if(infos[i].toString().compareTo(outputDevice) == 0) {
		    		try
		    		{
		    			SourceDataLine source = AudioSystem.getSourceDataLine(getAudioFormat(), infos[i]);
		    			
		    			return source;
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
	    
	    if(outputDevice == null || outputDevice == "") {
		    cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
		    		BaseMessages.getString( PKG, "Speaker.CheckResult.ReceivingRows.ERROR" ), stepMeta );
		    		remarks.add( cr );
		    return;
	    }
	    
	    if(null == getSourceDataLine()) {
		    cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
		    		BaseMessages.getString( PKG, "Speaker.CheckResult.ReceivingRows.ERROR" ), stepMeta );
		    		remarks.add( cr );
		    return;
	    }

  	cr = new CheckResult( CheckResult.TYPE_RESULT_OK,
			        BaseMessages.getString( PKG, "Speaker.CheckResult.ReceivingRows.OK" ), stepMeta );
		remarks.add( cr );
	  }
}
