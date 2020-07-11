package org.pentaho.di.sdk.steps.sensor;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class SensorStepDialog extends BaseStepDialog implements StepDialogInterface {

	private static final Class<?> PKG = SensorStepMeta.class; // for i18n purposes

	  private SensorStepMeta meta = null;

	  private Label wlMaxCount;
	  private FormData fdlMaxCount;
	  
	  private TextVar wMaxCount;
	  private FormData fdMaxCount;
	  
	  public SensorStepDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
	    super( parent, (BaseStepMeta) in, transMeta, sname );
	    meta = (SensorStepMeta)in;
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
	    shell.setText( BaseMessages.getString( PKG, "Sensor.Shell.Title" ) );
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

	    wlMaxCount = new Label( shell, SWT.RIGHT );
	    wlMaxCount.setText( BaseMessages.getString( PKG, "Sensor.MaxCount.Label" ) );
	    props.setLook( wlMaxCount );
	    fdlMaxCount = new FormData();
	    fdlMaxCount.left = new FormAttachment( 0, 0 );
	    fdlMaxCount.right = new FormAttachment( middle, -margin );
	    fdlMaxCount.top = new FormAttachment( lastControl, margin );
	    wlMaxCount.setLayoutData( fdlMaxCount );

	    wMaxCount = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    props.setLook( wMaxCount );
	    wMaxCount.addModifyListener( lsMod );
	    fdMaxCount = new FormData();
	    fdMaxCount.left = new FormAttachment( middle, 0 );
	    fdMaxCount.top = new FormAttachment( lastControl, margin );
	    fdMaxCount.right = new FormAttachment( 100, -margin );
	    wMaxCount.setLayoutData( fdMaxCount );
	    
	    lastControl = wMaxCount;

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
	    
	    wMaxCount.setText(Integer.toString(meta.getMaxCount()));

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
		  
		  try {
			  Integer.parseInt(wMaxCount.getText());
		  }
		  catch(NumberFormatException e) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
				messageBox.setText(BaseMessages.getString(PKG, "Sensor.Dialog.MaxCountErrorTitle"));
				messageBox.setMessage(BaseMessages.getString(PKG, "Sensor.Dialog.MaxCountErrorMessage"));
				messageBox.open();
				return;
		  }
		
	    stepname = wStepname.getText();
	    meta.setMaxCount(Integer.parseInt(wMaxCount.getText()));
	    // Setting the  settings to the meta object
//	    meta.setOutputField( wHelloFieldName.getText() );
	    // close the SWT dialog window
	    dispose();
	  }


}
