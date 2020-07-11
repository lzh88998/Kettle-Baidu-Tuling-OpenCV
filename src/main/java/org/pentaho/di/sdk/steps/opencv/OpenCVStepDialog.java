package org.pentaho.di.sdk.steps.opencv;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

public class OpenCVStepDialog extends BaseStepDialog implements StepDialogInterface {

  private static Class<?> PKG = OpenCVStepMeta.class; // for i18n purposes  

  private OpenCVStepMeta meta = null;
  private TextVar wFilename = null;

  public OpenCVStepDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    meta = (OpenCVStepMeta) in;
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
    shell.setText( BaseMessages.getString( PKG, "OpenCV.Shell.Title" ) );
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

    // Field name configuraion need modify to find file
    // Filename...
    //
    // The filename browse button
    //
    Button wbbFilename = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbbFilename );
    wbbFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    wbbFilename.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForFileOrDirAndAdd" ) );
    FormData fdbFilename = new FormData();
    fdbFilename.top = new FormAttachment( lastControl, margin );
    fdbFilename.right = new FormAttachment( 100, 0 );
    wbbFilename.setLayoutData( fdbFilename );

    // The field itself...
    //
    Label wlFilename = new Label( shell, SWT.RIGHT );
    wlFilename.setText( BaseMessages.getString( PKG, "OpenCV.Shell.Filename" ) );
    props.setLook( wlFilename );
    FormData fdlFilename = new FormData();
    fdlFilename.top = new FormAttachment( lastControl, margin );
    fdlFilename.left = new FormAttachment( 0, 0 );
    fdlFilename.right = new FormAttachment( middle, -margin );
    wlFilename.setLayoutData( fdlFilename );
    wFilename = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFilename );
    wFilename.addModifyListener( lsMod );
    FormData fdFilename = new FormData();
    fdFilename.top = new FormAttachment( lastControl, margin );
    fdFilename.left = new FormAttachment( middle, 0 );
    fdFilename.right = new FormAttachment( wbbFilename, -margin );
    wFilename.setLayoutData( fdFilename );

    wbbFilename.addSelectionListener( new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          FileDialog dialog = new FileDialog( shell, SWT.OPEN );
          dialog.setFilterExtensions( new String[] { "*.xml", "*" } );
          if ( wFilename.getText() != null ) {
            String fname = transMeta.environmentSubstitute( wFilename.getText() );
            dialog.setFileName( fname );
          }

          dialog.setFilterNames( new String[] {
            BaseMessages.getString( PKG, "System.FileType.XMLFiles" ),
            BaseMessages.getString( PKG, "System.FileType.AllFiles" ) } );

          if ( dialog.open() != null ) {
            String str = dialog.getFilterPath() + System.getProperty( "file.separator" ) + dialog.getFileName();
            wFilename.setText( str );
          }
        }
      } );

    // OK and cancel buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    setButtonPositions( new Button[] { wOK, wCancel }, margin, wFilename );

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
    
    wFilename.setText( meta.getFilePath() == null ? "" : meta.getFilePath());

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
//    wHelloFieldName.setText( meta.getOutputField() );
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
    meta.setFilePath(wFilename.getText());
    // Setting the  settings to the meta object
//    meta.setOutputField( wHelloFieldName.getText() );
    // close the SWT dialog window
    dispose();
  }
}
