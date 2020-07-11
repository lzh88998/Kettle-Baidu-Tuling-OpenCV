package org.pentaho.di.sdk.steps.tuling;

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
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@Step(
		  id = "TulingStep",
		  name = "Tuling.Name",
		  description = "Tuling.TooltipDesc",
		  image = "org/pentaho/di/sdk/steps/tuling/resources/tuling.svg",
		  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Experimental",
		  i18nPackageName = "org.pentaho.di.sdk.steps.tuling"
		  )
public class TulingStepMeta  extends BaseStepMeta implements StepMetaInterface {
	  private static final Class<?> PKG = TulingStepMeta.class; // for i18n purposes

	  private String key = "";
	  private String inputField = "";
//	  private String proxyHost;
//	  private Integer proxyPort;
	  
	  public TulingStepMeta() {
	    super();
	  }

	  public StepDialogInterface getDialog( Shell shell, StepMetaInterface meta, TransMeta transMeta, String name ) {
	    return new TulingStepDialog( shell, meta, transMeta, name );
	  }

	  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
	      Trans disp ) {
	    return new TulingStep( stepMeta, stepDataInterface, cnr, transMeta, disp );
	  }

	  public StepDataInterface getStepData() {
	    return new TulingStepData();
	  }

	  public void setDefault() {
		  setInputField("");
		  setKey("");
	  }

	  public void setInputField(String field) {
		  inputField = field;
	  }
	  
	  public String getInputField() {
		  return inputField;
	  }
	  
	  public void setKey(String k) {
		  key = k;
	  }
	  
	  public String getKey() {
		  return key;
	  }
	  
/*	  public void setProxyHost(String host) {
		  proxyHost = host;
	  }
	  
	  public String getProxyHost() {
		  return proxyHost;
	  }
	  
	  public void setProxyPort(Integer port) {
		  proxyPort = port;
	  }
	  
	  public Integer getProxyPort() {
		  return proxyPort;
	  }*/
	  
	  public Object clone() {
		  TulingStepMeta retval = (TulingStepMeta)super.clone();
		  retval.setInputField(inputField);
		  retval.setKey(key);
//		  retval.setProxyHost(proxyHost);
//		  retval.setProxyPort(proxyPort);
		  
		  return retval;
	  }

	  public String getXML() throws KettleValueException {
	    StringBuilder xml = new StringBuilder();

	    // only one field to serialize
	    xml.append( XMLHandler.addTagValue( "inputfield", inputField) );
	    xml.append( XMLHandler.addTagValue( "key", key) );
//	    xml.append( XMLHandler.addTagValue( "proxyhost", proxyHost) );
//	    xml.append( XMLHandler.addTagValue( "proxyport", proxyPort) );

	    return xml.toString();
	  }

	  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
	    try {
	    	setInputField( XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "inputfield" ) ) );
	    	setKey( XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "key" ) ) );
/*	    	setProxyHost( XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "proxyhost" ) ) );
	    	if(null != XMLHandler.getSubNode( stepnode, "proxyport" )) {
		    	setProxyPort( Integer.parseInt(XMLHandler.getNodeValue( XMLHandler.getSubNode( stepnode, "proxyport" ) ) ) );
	    	} else {
	    		setProxyPort(null);
	    	}*/
	    } catch ( Exception e ) {
	      throw new KettleXMLException( "Tuling plugin unable to read step info from XML node", e );
	    }
	  }

	  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
	      throws KettleException {
	    try {
	      rep.saveStepAttribute( id_transformation, id_step, "inputfield", inputField ); //$NON-NLS-1$
	      rep.saveStepAttribute( id_transformation, id_step, "key", key ); //$NON-NLS-1$
//	      rep.saveStepAttribute(id_transformation, id_step, "proxyhost", proxyHost);
//	      rep.saveStepAttribute(id_transformation, id_step, "proxyport", proxyPort);
	    } catch ( Exception e ) {
	      throw new KettleException( "Unable to save step into repository: " + id_step, e );
	    }
	  }

	  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
	      throws KettleException {
	    try {
	    	inputField  = rep.getStepAttributeString( id_step, "inputfield" ); //$NON-NLS-1$
	    	key  = rep.getStepAttributeString( id_step, "key" ); //$NON-NLS-1$
/*	    	proxyHost  = rep.getStepAttributeString( id_step, "proxyhost" ); //$NON-NLS-1$
	    	if(null != rep.getStepAttributeString( id_step, "proxyport" )) {
		    	proxyPort  = Integer.parseInt(rep.getStepAttributeString( id_step, "proxyport" )); //$NON-NLS-1$
	    	} else {
	    		proxyPort = null;
	    	}*/
	    } catch ( Exception e ) {
	      throw new KettleException( "Unable to load step from repository", e );
	    }
	  }

	  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
	      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

		  ValueMetaInterface v = new ValueMetaString( getOutputFieldName() );
		  v.setOrigin( name );
		  
		  inputRowMeta.addValueMeta( v );
	  }
	  
	  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
	      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
	      IMetaStore metaStore ) {
	    CheckResult cr;
	    
	    if(key == null || key == "") {
		    cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
		    		BaseMessages.getString( PKG, "Tuling.CheckResult.Key.ERROR" ), stepMeta );
		    		remarks.add( cr );
		    return;
	    }
	    
	    if(inputField == null || inputField == "") {
		    cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
		    		BaseMessages.getString( PKG, "Tuling.CheckResult.ReceivingRows.ERROR" ), stepMeta );
		    		remarks.add( cr );
		    return;
	    }

	cr = new CheckResult( CheckResult.TYPE_RESULT_OK,
			        BaseMessages.getString( PKG, "Tuling.CheckResult.ReceivingRows.OK" ), stepMeta );
		remarks.add( cr );
	  }
	  
	  public String getOutputFieldName() {
		  return "Answer";
	  }
}
