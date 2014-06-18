package harmonizationtool.handler;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.views.View;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;
import harmonizationtool.dialog.MetaDataDialog;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.FileMD;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import harmonizationtool.utils.FileEncodingUtil;
import harmonizationtool.utils.NonAsciiChar;
import harmonizationtool.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVStrategy;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

public class ImportCSV implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public static final String ID = "harmonizationtool.handler.ImportCSV";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Logger runLogger = Logger.getLogger("run");

		System.out.println("executing Import CSV");
		// ModelProvider modelProvider = new ModelProvider();
		TableProvider tableProvider = new TableProvider();
		// FileMD(String filename, String path, long size, Date lastModified, Date readTime)
		FileMD fileMD = new FileMD();

		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] { "*.csv" });
		String workingDirectory = Util.getPreferenceStore().getString("workingDirectory");
		if (workingDirectory.length() > 0) {
			fileDialog.setFilterPath(workingDirectory);
		} else {
			String homeDir = System.getProperty("user.home");
			fileDialog.setFilterPath(homeDir);
		}

		String path = fileDialog.open();
		File file = null;
		if (path != null) {
			file = new File(path);

			runLogger.info("LOAD CSV " + path);

			if (!file.exists()) {
				runLogger.warn("# File not found\n");

//				String msg = "File does not exist!";
//				Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
//				System.out.println(msg);
				return null;
			}
		}

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fileReader == null) {
//			String msg = "Can not read CSV file!";
			runLogger.error("# File not readable\n");

//			Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
//			System.out.println(msg);
			return null;
		}
		// FileEncodingUtil checkFileEncoding = new FileEncodingUtil();
		List<NonAsciiChar> nonAsciiCandidates = FileEncodingUtil.getFirstNonAsciiChar(path, 0);
		String cr = System.getProperty("line.separator");
		for (int i = 0; i < nonAsciiCandidates.size(); i++) {
			NonAsciiChar nonAsciiChar = nonAsciiCandidates.get(i);
			String message = "# Character Encoding info:" + cr;
			message += "#    File: " + path + cr;
			message += "#    Encoding: " + nonAsciiChar.encoding + cr;
			message += "#    First non-ASCII character: ->" + nonAsciiChar.character + "<-" + cr;
			message += "#    Occuring on line: " + nonAsciiChar.lineNumber + cr;
			message += "#    Characters to the left: " + nonAsciiChar.colNumber + cr;
			runLogger.info(message);
		}

		// checkFileEncoding.firstNonAsciiChars("basic");
		// String issues = checkFileEncoding.showFileIssues(path);
		// if (issues != null) {
		// runLogger.warn("# Non-ASCII issues found:\n" + issues);
		// System.out.println("Issues found with file: " + path + ":\n" +
		// issues);
		// }
		CSVParser parser = new CSVParser(fileReader, CSVStrategy.EXCEL_STRATEGY);
		String[] values = null;
		try {
			values = parser.getLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (values == null) { // BLANK FILE STILL HAS values (BUT ZERO LENGTH)
//			String msg = "No content in CSV file!";
			runLogger.warn("# No content in CSV file!");

//			Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
//			System.out.println(msg);
			return null;
		}
		fileMD.setFilename(file.getName());
		fileMD.setPath(path);
		fileMD.setByteCount(file.length());
		fileMD.setLastModified(new Date(file.lastModified()));
		Date readStartTime = new Date();
		fileMD.setReadTime(readStartTime);
		runLogger.info("# File read at: " + Util.getLocalDateFmt(readStartTime));
		runLogger.info("# File last modified: " + Util.getLocalDateFmt(new Date(file.lastModified())));
		runLogger.info("# File size: " + file.length());
		FlowsWorkflow.setFileMD(fileMD);

		// IF WE GOT CONTENT, THEN SAVE THIS FILE (MODEL) AND ADD IT TO THE MENU
		TableKeeper.saveTableProvider(path, tableProvider);

//		View view = (View) Util.findView(View.ID);
//		view.add(fileMD);

		while (values != null) {
			// printValues(parser.getLineNumber(),values);
			DataRow dataRow = initDataRow(values);
			tableProvider.addDataRow(dataRow);
			// System.out.println(dataRow);
			try {
				values = parser.getLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CSVTableView cSVTableView = (CSVTableView) page.findView(CSVTableView.ID);
		if (cSVTableView == null){
			try {
				Util.showView(CSVTableView.ID);
				cSVTableView = (CSVTableView) page.findView(CSVTableView.ID);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		assert cSVTableView != null : "cSVTableView cannot be null";

		String title = cSVTableView.getTitle();
		System.out.println("title= " + title);
		cSVTableView.update(path);

		// BRING UP THE DATA FILE VIEW
		// try {
		// Util.showView(View.ID);
		// } catch (PartInitException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// ... AND BRING UP THE DATA CONTENTS VIEW

		try {
			Util.showView(cSVTableView.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date readEndTime = new Date();
		runLogger.info("# File read time (in seconds): " + readEndTime.compareTo(readStartTime));

//		String msg = "Finished reading file: " + path;
//		Util.findView(View.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);

		// NOW OPEN DIALOG WITH SOME PRE-POPULATE fileMD INFO
		MetaDataDialog dialog = new MetaDataDialog(Display.getCurrent().getActiveShell(), fileMD);

		dialog.create();
		dialog.open();
//		if (dialog.open() == MetaDataDialog.CANCEL) {
//			// remove fileMD from Data Files viewer
////			view.remove(fileMD);
//		}

		return null;
	}

	private DataRow initDataRow(String[] values) {
		DataRow dataRow = new DataRow();
		for (String s : values) {
			dataRow.add(s);
		}
		return dataRow;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

}
