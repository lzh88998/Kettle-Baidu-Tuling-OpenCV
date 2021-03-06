package org.pentaho.di.sdk.steps.ledmatrix;

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
		  id = "LEDMatrixStep",
		  name = "LEDMatrix.Name",
		  description = "LEDMatrix.TooltipDesc",
		  image = "org/pentaho/di/sdk/steps/ledmatrix/resources/ledmatrix.svg",
		  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Experimental",
		  i18nPackageName = "org.pentaho.di.sdk.steps.ledmatrix"
		  )
public class LEDMatrixStepMeta  extends BaseStepMeta implements StepMetaInterface {
	  private static final Class<?> PKG = LEDMatrixStepMeta.class; // for i18n purposes

	  private String inputField = "";
	  
	  public LEDMatrixStepMeta() {
	    super();
	  }

	  public StepDialogInterface getDialog( Shell shell, StepMetaInterface meta, TransMeta transMeta, String name ) {
	    return new LEDMatrixStepDialog( shell, meta, transMeta, name );
	  }

	  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
	      Trans disp ) {
	    return new LEDMatrixStep( stepMeta, stepDataInterface, cnr, transMeta, disp );
	  }

	  public StepDataInterface getStepData() {
	    return new LEDMatrixStepData();
	  }

	  public void setDefault() {
		  setInputField("");
	  }
	  
	  public void setInputField(String field) {
		  inputField = field;
	  }
	  
	  public String getInputField() {
		  return inputField;
	  }
	  
	  public Object clone() {
		  LEDMatrixStepMeta retval = (LEDMatrixStepMeta)super.clone();
		  retval.setInputField(inputField);
		  
		  return retval;
	  }

	  public String getXML() throws KettleValueException {
	    StringBuilder xml = new StringBuilder();

	    // only one field to serialize
	    xml.append( XMLHandler.addTagValue( "inputfield", inputField) );

	    return xml.toString();
	  }

	  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
	    try {
	    	setInputField( XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "inputfield" ) ) );
	    } catch ( Exception e ) {
	      throw new KettleXMLException( "LEDMatrix plugin unable to read step info from XML node", e );
	    }
	  }

	  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
	      throws KettleException {
	    try {
	      rep.saveStepAttribute( id_transformation, id_step, "inputfield", inputField ); //$NON-NLS-1$
	    } catch ( Exception e ) {
	      throw new KettleException( "Unable to save step into repository: " + id_step, e );
	    }
	  }

	  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
	      throws KettleException {
	    try {
	    	inputField  = rep.getStepAttributeString( id_step, "inputfield" ); //$NON-NLS-1$
	    } catch ( Exception e ) {
	      throw new KettleException( "Unable to load step from repository", e );
	    }
	  }

	  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
	      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
	  }
	  
	  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
	      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
	      IMetaStore metaStore ) {
	    CheckResult cr;

	    if(inputField == null || inputField == "") {
		    cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
		    		BaseMessages.getString( PKG, "LEDMatrix.CheckResult.ReceivingRows.ERROR" ), stepMeta );
		    		remarks.add( cr );
		    return;
	    }

	    cr = new CheckResult( CheckResult.TYPE_RESULT_OK,
			        BaseMessages.getString( PKG, "LEDMatrix.CheckResult.ReceivingRows.OK" ), stepMeta );
		remarks.add( cr );
	  }
}
