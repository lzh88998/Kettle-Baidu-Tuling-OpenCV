package org.pentaho.di.sdk.steps.ledmatrix;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class LEDMatrixStepDialog extends BaseStepDialog implements StepDialogInterface {

	private static final Class<?> PKG = LEDMatrixStepMeta.class; // for i18n purposes

	  private LEDMatrixStepMeta meta = null;
	  
	  private Label wlField;
	  private FormData fdlField;
	  
	  private CCombo wField;
	  private FormData fdField;

	  public LEDMatrixStepDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
	    super( parent, (BaseStepMeta) in, transMeta, sname );
	    meta = (LEDMatrixStepMeta)in;
	  }

	  public String open() {
	    Shell parent = getParent();
	    Display display = parent.getDisplay();

	    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
	    props.setLook( shell );
	    setShellImage( shell, meta );

	    changed = meta.hasChanged();

	    ModifyListener lsMod = new ModifyListener() {
	      public void modifyText( ModifyEvent e ) {
	        meta.setChanged();
	      }
	    };

	    FormLayout formLayout = new FormLayout();
	    formLayout.marginWidth = Const.FORM_MARGIN;
	    formLayout.marginHeight = Const.FORM_MARGIN;
	    shell.setLayout( formLayout );
	    shell.setText( BaseMessages.getString( PKG, "LEDMatrix.Shell.Title" ) );
	    int middle = props.getMiddlePct();
	    int margin = Const.MARGIN;

	    // Step name configuration
	    wlStepname = new Label( shell, SWT.RIGHT );
	    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
	    props.setLook( wlStepname );
	    fdlStepname = new FormData();
	    fdlStepname.left = new FormAttachment( 0, 0 );
	    fdlStepname.right = new FormAttachment( middle, -margin );
	    fdlStepname.top = new FormAttachment( 0, margin );
	    wlStepname.setLayoutData( fdlStepname );

	    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    wStepname.setText( stepname );
	    props.setLook( wStepname );
	    wStepname.addModifyListener( lsMod );
	    fdStepname = new FormData();
	    fdStepname.left = new FormAttachment( middle, 0 );
	    fdStepname.top = new FormAttachment( 0, margin );
	    fdStepname.right = new FormAttachment( 100, 0 );
	    wStepname.setLayoutData( fdStepname );
	    
	    Control lastControl = wStepname;

	    // Field selection
	      RowMetaInterface previousFields;
	      try {
	        previousFields = transMeta.getPrevStepFields( stepMeta );
	      } catch ( KettleStepException e ) {
	        new ErrorDialog( shell,
	          BaseMessages.getString( PKG, "LEDMatrix.Error.Title" ),
	          BaseMessages.getString( PKG, "LEDMatrix.Error.Message" ), e );
	        previousFields = new RowMeta();
	      }

	    wlField = new Label( shell, SWT.RIGHT );
	    wlField.setText( BaseMessages.getString( PKG, "LEDMatrix.Field.Label" ) );
	    props.setLook( wlField );
	    fdlField = new FormData();
	    fdlField.left = new FormAttachment( 0, 0 );
	    fdlField.right = new FormAttachment( middle, -margin );
	    fdlField.top = new FormAttachment( lastControl, margin );
	    wlField.setLayoutData( fdlField );

	    wField = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
	    wField.setItems( previousFields.getFieldNames() );
	    props.setLook( wField );
	    wField.addModifyListener( lsMod );
	    fdField = new FormData();
	    fdField.left = new FormAttachment( middle, 0 );
	    fdField.top = new FormAttachment( lastControl, margin );
	    fdField.right = new FormAttachment( 100, -margin );
	    wField.setLayoutData( fdField );
	    
	    lastControl = wField;
	    
	    // OK and cancel buttons
	    wOK = new Button( shell, SWT.PUSH );
	    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
	    wCancel = new Button( shell, SWT.PUSH );
	    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
	    setButtonPositions( new Button[] { wOK, wCancel }, margin, lastControl );

	    // Add listeners for cancel and OK
	    lsCancel = new Listener() {
	      public void handleEvent( Event e ) {
	        cancel();
	      }
	    };
	    lsOK = new Listener() {
	      public void handleEvent( Event e ) {
	        ok();
	      }
	    };
	    wCancel.addListener( SWT.Selection, lsCancel );
	    wOK.addListener( SWT.Selection, lsOK );

	    // default listener (for hitting "enter")
	    lsDef = new SelectionAdapter() {
	      public void widgetDefaultSelected( SelectionEvent e ) {
	        ok();
	      }
	    };
	    wStepname.addSelectionListener( lsDef );

	    // Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
	    shell.addShellListener( new ShellAdapter() {
	      public void shellClosed( ShellEvent e ) {
	        cancel();
	      }
	    } );

	    // Set/Restore the dialog size based on last position on screen
	    // The setSize() method is inherited from BaseStepDialog
	    setSize();
	    
	    wField.setText(meta.getInputField() == null ? "" : meta.getInputField());

	    // populate the dialog with the values from the meta object
	    populateDialog();

	    // restore the changed flag to original value, as the modify listeners fire during dialog population  
	    meta.setChanged( changed );

	    // open dialog and enter event loop  
	    shell.open();
	    while ( !shell.isDisposed() ) {
	      if ( !display.readAndDispatch() ) {
	        display.sleep();
	      }
	    }

	    // at this point the dialog has closed, so either ok() or cancel() have been executed
	    // The "stepname" variable is inherited from BaseStepDialog
	    return stepname;
	  }

	  /**
	   * This helper method puts the step configuration stored in the meta object
	   * and puts it into the dialog controls.
	   */
	  private void populateDialog() {
	    wStepname.selectAll();
//	    wHelloFieldName.setText( meta.getOutputField() );
	  }

	  /**
	   * Called when the user cancels the dialog.  
	   */
	  private void cancel() {
	    // The "stepname" variable will be the return value for the open() method.  
	    // Setting to null to indicate that dialog was cancelled.
	    stepname = null;
	    // Restoring original "changed" flag on the met aobject
	    meta.setChanged( changed );
	    // close the SWT dialog window
	    dispose();
	  }

	  /**
	   * Called when the user confirms the dialog
	   */
	  private void ok() {
	    // The "stepname" variable will be the return value for the open() method.  
	    // Setting to step name from the dialog control
		
	    stepname = wStepname.getText();
	    meta.setInputField(wField.getText());
	    // Setting the  settings to the meta object
//	    meta.setOutputField( wHelloFieldName.getText() );
	    // close the SWT dialog window
	    dispose();
	  }


}
