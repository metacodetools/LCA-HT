package gov.epa.nrmrl.std.lca.ht.dialog;

import gov.epa.nrmrl.std.lca.ht.preferences.Initializer;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.ui.PlatformUI;

public class StorageLocationDialog extends TitleAreaDialog {
	
	public static final int RET_SUCCESS=0;
	public static final int RET_NO_DIRECTORY = 1;
	public static final int RET_SHOW_PREFS = 2;

	
	private Text text;
	
	Button prefsButton = null;
	
	String storageLocation = null;
	
	boolean showPrefs = false;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public StorageLocationDialog(Shell parentShell) {
		super(parentShell);
		this.setBlockOnOpen(true);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		
		setTitle("Choose Storage Location");
		setMessage("The Harmonization Tool (HT) requires the user to specify directories for local storage.  Please choose a location to store its data.");
		
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayout(new GridLayout(3, false));
		container.setLayoutData(gd);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		/*Web*/
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		
		Label label = new Label(container, SWT.NONE);
		label.setText("Directory");
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));

		text = new Text(container, SWT.SINGLE | SWT.BORDER);
		
		storageLocation = Util.getInitialStorageLocation();

		text.setText(storageLocation);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd.grabExcessHorizontalSpace = true;
		text.setLayoutData(gd);
		
		final Button button = new Button(container, SWT.NONE);
		button.setText("Browse");
		button.addSelectionListener(new SelectionListener() {
 
			public void widgetDefaultSelected(SelectionEvent e) {
			}
 
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(button.getShell(),  SWT.OPEN  );
				dlg.setFilterPath(storageLocation);
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null) return;
				text.setText(path);
			}
		});
		button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		prefsButton = new Button(container, SWT.CHECK);
		prefsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showPrefs = prefsButton.getSelection();
			}
		});
		prefsButton.setText("Choose individual locations");
		prefsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		new Label(container, SWT.NONE);
		

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(635, 300);
	}
	
	
	
	@Override
	protected void okPressed() {
		if (showPrefs) {
			this.setReturnCode(RET_SHOW_PREFS);
			close();
		}
		else if (Initializer.initializeDefaultPreferences(text.getText())) {
			setReturnCode(RET_SUCCESS);
			close();
		}
		else {
			StringBuilder b = new StringBuilder();
			b.append("Sorry, but the selected directory is not accessible.  Please ensure the selected directory is available and writeable.");
			String errMsg = b.toString();
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			new GenericMessageBox(shell, "Error", errMsg);
			text.setText(Util.getInitialStorageLocation());
		}
	}
	
	@Override
	protected void cancelPressed() {
		if (Initializer.initializeDefaultPreferences(text.getText())) {
			setReturnCode(RET_SUCCESS);
			close();
		}
		else {
			String errMsg = "The selected directory is not accessible - exiting now.";
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			new GenericMessageBox(shell, "Error", errMsg);
			close();
			System.exit(1);
		}
	}
	
}