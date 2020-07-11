package org.pentaho.di.sdk.steps.recorder;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.sdk.steps.recorder.RecorderStepMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class RecorderStepDialog  extends BaseStepDialog implements StepDialogInterface {

	  private static Class<?> PKG = RecorderStepMeta.class; // for i18n purposes  

	  private RecorderStepMeta meta = null;
	  
	  private Label wlDevice;
	  private FormData fdlDevice;
	  
	  private CCombo wDevice;
	  private FormData fdDevice;
	  
	  private Label wlMode;
	  private FormData fdlMode;
	  
	  private CCombo wMode;
	  private FormData fdMode;

	  private Label wlParameter;
	  private FormData fdlParameter;
	  
	  private TextVar wParameter;
	  private FormData fdParameter;

	  public RecorderStepDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
	    super( parent, (BaseStepMeta) in, transMeta, sname );
	    meta = (RecorderStepMeta) in;
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
	    shell.setText( BaseMessages.getString( PKG, "Recorder.Shell.Title" ) );
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

	    // Device selection
	    wlDevice = new Label( shell, SWT.RIGHT );
	    wlDevice.setText( BaseMessages.getString( PKG, "Recorder.Device.Label" ) );
	    props.setLook( wlDevice );
	    fdlDevice = new FormData();
	    fdlDevice.left = new FormAttachment( 0, 0 );
	    fdlDevice.right = new FormAttachment( middle, -margin );
	    fdlDevice.top = new FormAttachment( lastControl, margin );
	    wlDevice.setLayoutData( fdlDevice );

	    wDevice = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
	    wDevice.setItems( meta.getTargetLineNames() );
	    props.setLook( wDevice );
	    wDevice.addModifyListener( lsMod );
	    fdDevice = new FormData();
	    fdDevice.left = new FormAttachment( middle, 0 );
	    fdDevice.top = new FormAttachment( lastControl, margin );
	    fdDevice.right = new FormAttachment( 100, -margin );
	    wDevice.setLayoutData( fdDevice );
	    
	    lastControl = wDevice;
	    
	    // The Recording Mode field.
	    //
	    wlMode = new Label( shell, SWT.RIGHT );
	    wlMode.setText( BaseMessages.getString( PKG, "Recorder.Dialog.RecordingMode" ) );
	    props.setLook( wlMode );
	    fdlMode = new FormData();
	    fdlMode.top = new FormAttachment( lastControl, margin );
	    fdlMode.left = new FormAttachment( 0, 0 );
	    fdlMode.right = new FormAttachment( middle, -margin );
	    wlMode.setLayoutData( fdlMode );
	    
	    wMode = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
	    wMode.setItems( new String[] {BaseMessages.getString( PKG, "Recorder.Dialog.AlwaysRecording" ), BaseMessages.getString( PKG, "Recorder.Dialog.SoundDetect" )} );
	    props.setLook( wMode );
	    wMode.addModifyListener( lsMod );
	    fdMode = new FormData();
	    fdMode.top = new FormAttachment( lastControl, margin );
	    fdMode.left = new FormAttachment( middle, 0 );
	    fdMode.right = new FormAttachment( 100, -margin );
	    wMode.setLayoutData( fdMode );
	    
	    wMode.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent( Event e ) {
	    		if(null != wlParameter) {
	    			if(wMode.getSelectionIndex() == 0) {
	    			    wlParameter.setText( BaseMessages.getString( PKG, "Recorder.Dialog.RecordingDuration" ) );
	    			} else if(wMode.getSelectionIndex() == 1) {
	    			    wlParameter.setText( BaseMessages.getString( PKG, "Recorder.Dialog.VoiceThreshold" ) );
	    			}
	    		}
	    		
	    		if(null != wParameter) {
	    			if(wMode.getSelectionIndex() == 0) {
	    			    wParameter.setText( "10" );
	    			} else if(wMode.getSelectionIndex() == 1) {
	    			    wParameter.setText( "1024" );
	    			}
	    		}
	    	}
	    });

	    lastControl = wMode;

	    // The Parameter Value field.
	    //
	    wlParameter = new Label( shell, SWT.RIGHT );
	    if(meta.getRecordingMode() == 0) {
		    wlParameter.setText( BaseMessages.getString( PKG, "Recorder.Dialog.RecordingDuration" ) );
	    } else {
		    wlParameter.setText( BaseMessages.getString( PKG, "Recorder.Dialog.VoiceThreshold" ) );
	    }
	    props.setLook( wlParameter );
	    fdlParameter = new FormData();
	    fdlParameter.top = new FormAttachment( lastControl, margin );
	    fdlParameter.left = new FormAttachment( 0, 0 );
	    fdlParameter.right = new FormAttachment( middle, -margin );
	    wlParameter.setLayoutData( fdlParameter );
	    
	    wParameter = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    props.setLook( wParameter );
	    wParameter.addModifyListener( lsMod );
	    fdParameter = new FormData();
	    fdParameter.top = new FormAttachment( lastControl, margin );
	    fdParameter.left = new FormAttachment( middle, 0 );
	    fdParameter.right = new FormAttachment( 100, -margin );
	    wParameter.setLayoutData( fdParameter );

	    lastControl = wParameter;

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
	    
    	wDevice.setText(meta.getInputDevice() == null ? "" : meta.getInputDevice());
    	wMode.select(meta.getRecordingMode());
    	wParameter.setText(Integer.toString(meta.getVoiceParameter()));

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
			Integer.parseInt(wParameter.getText());
		}
		catch(NumberFormatException e) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
			messageBox.setText(BaseMessages.getString(PKG, "Recorder.Dialog.ThresholdErrorTitle"));
			messageBox.setMessage(wlParameter.getText() + BaseMessages.getString(PKG, "Recorder.Dialog.ThresholdErrorMessage"));
			messageBox.open();
			return;
		}
		
	    stepname = wStepname.getText();
	    meta.setInputDevice(wDevice.getText());
	    meta.setRecordingMode(wMode.getSelectionIndex());
	    meta.setVoiceParameter(Integer.parseInt(wParameter.getText()));
	    // Setting the  settings to the meta object
//	    meta.setOutputField( wHelloFieldName.getText() );
	    // close the SWT dialog window
	    dispose();
	  }
}
