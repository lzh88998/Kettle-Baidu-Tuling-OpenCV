package org.pentaho.di.sdk.steps.tuling;

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
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class TulingStepDialog  extends BaseStepDialog implements StepDialogInterface {

	  private static Class<?> PKG = TulingStepMeta.class; // for i18n purposes  

	  private TulingStepMeta meta = null;
	  
	  private Label wlKey;
	  private FormData fdlKey;
	  
	  private TextVar wKey;
	  private FormData fdKey;

	  private Label wlField;
	  private FormData fdlField;
	  
	  private CCombo wField;
	  private FormData fdField;

/*	  private Label wlProxyHost;
	  private FormData fdlProxyHost;
	  
	  private TextVar wProxyHost;
	  private FormData fdProxyHost;

	  private Label wlProxyPort;
	  private FormData fdlProxyPort;
	  
	  private TextVar wProxyPort;
	  private FormData fdProxyPort;*/

	  public TulingStepDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
	    super( parent, (BaseStepMeta) in, transMeta, sname );
	    meta = (TulingStepMeta)in;
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
	    shell.setText( BaseMessages.getString( PKG, "Tuling.Shell.Title" ) );
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
	          BaseMessages.getString( PKG, "Tuling.Error.Title" ),
	          BaseMessages.getString( PKG, "Tuling.Error.Message" ), e );
	        previousFields = new RowMeta();
	      }

	    wlField = new Label( shell, SWT.RIGHT );
	    wlField.setText( BaseMessages.getString( PKG, "Tuling.Feild.Label" ) );
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
	    
	    // Token input
	    wlKey = new Label( shell, SWT.RIGHT );
	    wlKey.setText( BaseMessages.getString( PKG, "Tuling.Token.Label" ) );
	    props.setLook( wlKey );
	    fdlKey = new FormData();
	    fdlKey.left = new FormAttachment( 0, 0 );
	    fdlKey.right = new FormAttachment( middle, -margin );
	    fdlKey.top = new FormAttachment( lastControl, margin );
	    wlKey.setLayoutData( fdlKey );

	    wKey = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    props.setLook( wKey );
	    wKey.addModifyListener( lsMod );
	    fdKey = new FormData();
	    fdKey.left = new FormAttachment( middle, 0 );
	    fdKey.top = new FormAttachment( lastControl, margin );
	    fdKey.right = new FormAttachment( 100, -margin );
	    wKey.setLayoutData( fdKey );
	    
	    lastControl = wKey;

/*	    
	    // Proxy Host input
	    wlProxyHost = new Label( shell, SWT.RIGHT );
	    wlProxyHost.setText( BaseMessages.getString( PKG, "Tuling.ProxyHost.Label" ) );
	    props.setLook( wlProxyHost );
	    fdlProxyHost = new FormData();
	    fdlProxyHost.left = new FormAttachment( 0, 0 );
	    fdlProxyHost.right = new FormAttachment( middle, -margin );
	    fdlProxyHost.top = new FormAttachment( lastControl, margin );
	    wlProxyHost.setLayoutData( fdlProxyHost );

	    wProxyHost = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    props.setLook( wProxyHost );
	    wProxyHost.addModifyListener( lsMod );
	    fdProxyHost = new FormData();
	    fdProxyHost.left = new FormAttachment( middle, 0 );
	    fdProxyHost.top = new FormAttachment( lastControl, margin );
	    fdProxyHost.right = new FormAttachment( 100, -margin );
	    wProxyHost.setLayoutData( fdProxyHost );
	    
	    lastControl = wProxyHost;

	    // Proxy Port input
	    wlProxyPort = new Label( shell, SWT.RIGHT );
	    wlProxyPort.setText( BaseMessages.getString( PKG, "Tuling.ProxyPort.Label" ) );
	    props.setLook( wlProxyPort );
	    fdlProxyPort = new FormData();
	    fdlProxyPort.left = new FormAttachment( 0, 0 );
	    fdlProxyPort.right = new FormAttachment( middle, -margin );
	    fdlProxyPort.top = new FormAttachment( lastControl, margin );
	    wlProxyPort.setLayoutData( fdlProxyPort );

	    wProxyPort = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    props.setLook( wProxyPort );
	    wProxyPort.addModifyListener( lsMod );
	    fdProxyPort = new FormData();
	    fdProxyPort.left = new FormAttachment( middle, 0 );
	    fdProxyPort.top = new FormAttachment( lastControl, margin );
	    fdProxyPort.right = new FormAttachment( 100, -margin );
	    wProxyPort.setLayoutData( fdProxyPort );
	    
	    lastControl = wProxyPort;*/

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
	    
	    wKey.setText(meta.getKey() == null ? "" : meta.getKey());
	    wField.setText(meta.getInputField() == null ? "" : meta.getInputField());
//	    wProxyHost.setText(meta.getProxyHost() == null ? "" : meta.getProxyHost());
//	    wProxyPort.setText(meta.getProxyPort() == null ? "" : meta.getProxyPort().toString());

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
/*		  Integer port = null;
		  if(null != wProxyPort.getText() && "" != wProxyPort.getText())
		  {
			try {
				port = Integer.parseInt(wProxyPort.getText());
			}
			catch(NumberFormatException e) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
				messageBox.setText(BaseMessages.getString(PKG, "Tuling.ProxyPort.Error"));
				messageBox.setMessage(BaseMessages.getString(PKG, "Tuling.ProxyPort.Error"));
				messageBox.open();
				return;
			}
		  }
		*/
	    stepname = wStepname.getText();
	    meta.setKey(wKey.getText());
	    meta.setInputField(wField.getText());
//	    meta.setProxyHost(wProxyHost.getText());
//	    meta.setProxyPort(port);
	    // Setting the  settings to the meta object
//	    meta.setOutputField( wHelloFieldName.getText() );
	    // close the SWT dialog window
	    dispose();
	  }

}
