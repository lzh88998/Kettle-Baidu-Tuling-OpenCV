package org.pentaho.di.sdk.steps.opencv;

import java.io.File;
import java.util.List;

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
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
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
  id = "OpenCVStep",
  name = "OpenCV.Name",
  description = "OpenCV.TooltipDesc",
  image = "org/pentaho/di/sdk/steps/opencv/resources/opencv.svg",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Experimental",
  i18nPackageName = "org.pentaho.di.sdk.steps.opencv"
  )
public class OpenCVStepMeta extends BaseStepMeta implements StepMetaInterface {

  private static final Class<?> PKG = OpenCVStepMeta.class; // for i18n purposes
  
  private String filePath = null;

  public OpenCVStepMeta() {
    super();
  }

  public StepDialogInterface getDialog( Shell shell, StepMetaInterface meta, TransMeta transMeta, String name ) {
    return new OpenCVStepDialog( shell, meta, transMeta, name );
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans disp ) {
    return new OpenCVStep( stepMeta, stepDataInterface, cnr, transMeta, disp );
  }

  public StepDataInterface getStepData() {
    return new OpenCVStepData();
  }

  public void setDefault() {
    setFilePath("");
  }
  
  public void setFilePath(String path) {
	  filePath = path;
  }
  
  public String getFilePath() {
	  return filePath;
  }

  public Object clone() {
	  OpenCVStepMeta retval = (OpenCVStepMeta)super.clone();
	  retval.setFilePath(filePath);
	  return retval;
  }

  public String getXML() throws KettleValueException {
    StringBuilder xml = new StringBuilder();

    // only one field to serialize
    xml.append( XMLHandler.addTagValue( "filepath", filePath ) );
    return xml.toString();
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {
    	setFilePath( XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "filepath" ) ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Demo plugin unable to read step info from XML node", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
      throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "filepath", filePath ); //$NON-NLS-1$
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step into repository: " + id_step, e );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
      throws KettleException {
    try {
    	filePath  = rep.getStepAttributeString( id_step, "filepath" ); //$NON-NLS-1$
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load step from repository", e );
    }
  }

  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

	  ValueMetaInterface v = new ValueMetaInteger("Frame index");
	  v.setOrigin( name );
	  inputRowMeta.addValueMeta( v );
	  
	  v = new ValueMetaInteger( "Item index" );
	  v.setOrigin( name );
	  inputRowMeta.addValueMeta( v );

	  v = new ValueMetaInteger( "X" );
	  v.setOrigin( name );
	  inputRowMeta.addValueMeta( v );
	
	  v = new ValueMetaInteger( "Y" );
	  v.setOrigin( name );
	  inputRowMeta.addValueMeta( v );

	  v = new ValueMetaInteger( "Width" );
	  v.setOrigin( name );
	  inputRowMeta.addValueMeta( v );

	  v = new ValueMetaInteger( "Height" );
	  v.setOrigin( name );
	  inputRowMeta.addValueMeta( v );
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    // See if there are input streams leading to this step!
    if ( filePath != null) {
      File f = new File(filePath);
      if(f.exists()) {
	      cr = new CheckResult( CheckResult.TYPE_RESULT_OK,
	        BaseMessages.getString( PKG, "OpenCV.CheckResult.ReceivingRows.OK" ), stepMeta );
	      remarks.add( cr );
	      
	      return;
      }
    }
    cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
	BaseMessages.getString( PKG, "OpenCV.CheckResult.ReceivingRows.ERROR" ), stepMeta );
	remarks.add( cr );
  }
  
  @Override
  public StepIOMetaInterface getStepIOMeta() {
    return new StepIOMeta( false, true, false, false, false, false );
  }
}
