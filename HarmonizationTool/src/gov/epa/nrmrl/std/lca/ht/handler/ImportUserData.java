package gov.epa.nrmrl.std.lca.ht.handler;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataRow;
import gov.epa.nrmrl.std.lca.ht.dataModels.DataSourceKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.FileMD;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableKeeper;
import gov.epa.nrmrl.std.lca.ht.dataModels.TableProvider;
import gov.epa.nrmrl.std.lca.ht.dialog.MetaDataDialog;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowProperty;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;

public class ImportUserData implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public static final String ID = "gov.epa.nrmrl.std.lca.ht.handler.ImportUserData";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Logger runLogger = Logger.getLogger("run");
		TableProvider tableProvider = new TableProvider();
		FileMD fileMD = new FileMD();

		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] { "*.csv;*.zip;*.n3;*.ttl;*.rdf;*.jsonld;*.json" });
		String inputDirectory = Util.getPreferenceStore().getString("inputDirectory");
		if (inputDirectory.length() > 0) {
			fileDialog.setFilterPath(inputDirectory);
		} else {
			String homeDir = System.getProperty("user.home");
			fileDialog.setFilterPath(homeDir);
		}

		String path = fileDialog.open();
		if (path == null) {
			runLogger.info("# Cancelling CSV file read");
			return null;
		}
		File file = new File(path);
		if (!path.matches(".*\\.csv")){
			loadUserDataFromRDFFile(file);
			return null;
		}

		runLogger.info("LOAD CSV " + path);

		if (!file.exists()) {
			runLogger.warn("# File not found\n");
			return null;
		}

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fileReader == null) {
			runLogger.error("# File not readable\n");
			return null;
		}

		CSVParser parser = new CSVParser(fileReader, CSVStrategy.EXCEL_STRATEGY);
		// FIXME - IF THE CSV FILE HAS WINDOWS CARRIAGE RETURNS, THE HT DOESN'T SPLIT ON THEM, SO YOU GET ONE ROW, MANY
		// COLUMNS
		String[] values = null;
		try {
			values = parser.getLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (values == null) { // BLANK FILE STILL HAS values (BUT ZERO LENGTH)
			runLogger.warn("# No content in CSV file!");
			return null;
		}
		fileMD.setFilename(file.getName());
		fileMD.setPath(path);
		fileMD.setByteCount(file.length());
		fileMD.setModifiedDate(new Date(file.lastModified()));
		Date readDate = new Date();
		fileMD.setReadDate(readDate);
		runLogger.info("# File read at: " + Util.getLocalDateFmt(readDate));
		runLogger.info("# File last modified: " + Util.getLocalDateFmt(new Date(file.lastModified())));
		runLogger.info("# File size: " + file.length());

		System.out.println("All's fine before opening dialog");
		MetaDataDialog dialog = new MetaDataDialog(Display.getCurrent().getActiveShell(), fileMD);
		System.out.println("meta initialized");
		dialog.create();
		System.out.println("meta created");
		boolean thing;
		if (thing = dialog.open() == MetaDataDialog.CANCEL) { // FIXME
			System.out.println("cancel!");

			fileMD.remove();
			return null;
		}
		System.out.println("thing = " + thing);
		System.out.println("Got past opening dialog");
		tableProvider.setFileMD(fileMD);
		System.out.println("FileMD set in tableProvider");

		tableProvider.setDataSourceProvider(dialog.getCurDataSourceProvider());
		System.out.println("DataSource set in tableProvider");

		TableKeeper.saveTableProvider(path, tableProvider);
		System.out.println("Save tableProvider in TableKeeper");

		// READ THE FILE NOW
		try {
			while (values != null) {
				DataRow dataRow = initDataRow(values);
				tableProvider.addDataRow(dataRow); // SLOW PROCESS: JUNO FIXME
				values = parser.getLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Date readEndDate = new Date();
		int secondsRead = (int) ((readEndDate.getTime() - readDate.getTime()) / 1000);
		runLogger.info("# File read time (in seconds): " + secondsRead);

		try {
			Util.showView(CSVTableView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		System.out.println("About to update CSVTableView");
		CSVTableView.update(path);

		return null;
	}
	
	public static void loadUserDataFromRDFFile(File file) {
		DataSourceKeeper.placeOrphanDataInNewOrphanDataset();
		List<String> currentNames = DataSourceKeeper.getDataSourceNamesInTDB();
		int priorDataSetCount = DataSourceKeeper.countDataSourcesInTDB();
		FileMD fileMD = new FileMD();

		String fileName = file.getName();
		String path = file.getPath();
		Logger runLogger = Logger.getLogger("run");

		runLogger.info("LOAD RDF " + path);
		String sep = File.separator;

		// String path = fileDialog.getFilterPath();
		// runLogger.info("# Read Master RDF data from " + path);
		//
		// String[] fileList = fileDialog.getFileNames();
		// // String sep = System.getProperty("file.separator");
		// String sep = File.separator;

		// System.out.println("path= " + path);
		// IRIResolver thing = IRIResolver.create("http://openlca.org/schema/v1.0/");
		// File firstFile = null;
		// for (String fileName : fileList) {
		// System.out.println("fileName= " + fileName);
		// if (!fileName.startsWith(sep)) {
		// fileName = path + sep + fileName;
		// }
		// if (firstFile == null){
		// firstFile = new File(fileName);
		// }

		long was = ActiveTDB.getModel().size();
		long startTime = System.currentTimeMillis();
		if (!fileName.matches(".*\\.zip")) {
			try {
				String inputType = "SKIP";
				if (fileName.matches(".*\\.rdf")) {
					inputType = "RDF/XML";
				} else if (fileName.matches(".*\\.n3")) {
					inputType = "N3";
				} else if (fileName.matches(".*\\.ttl")) {
					inputType = "TTL";
				} else if (fileName.matches(".*\\.jsonld")) {
					inputType = "JSON-LD";
				} else if (fileName.matches(".*\\.json")) {
					inputType = "JSON-LD";
				}
				InputStream inputStream = new FileInputStream(path);
				runLogger.info("LOAD RDF " + fileName);
				ImportMasterRDFHandler.readStreamCountNewDataSources(inputStream, inputType);
				// ActiveTDB.syncTDBtoLCAHT();

			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (fileName.matches(".*\\.zip.*")) {
			try {
				@SuppressWarnings("resource")
				ZipFile zf = new ZipFile(path);
				runLogger.info("LOAD RDF (zip file)" + fileName);

				Enumeration<?> entries = zf.entries();

				while (entries.hasMoreElements()) {
					ZipEntry ze = (ZipEntry) entries.nextElement();
					String inputType = "SKIP";
					if (ze.getName().matches(".*\\.rdf")) {
						inputType = "RDF/XML";
					} else if (ze.getName().matches(".*\\.n3")) {
						inputType = "N3";
					} else if (ze.getName().matches(".*\\.ttl")) {
						inputType = "TTL";
					} else if (ze.getName().matches(".*\\.jsonld")) {
						inputType = "JSON-LD";
					} else if (ze.getName().matches(".*\\.json")) {
						inputType = "JSON-LD";
					}
					if (inputType != "SKIP") {
						BufferedReader zipStream = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
//						runLogger.info("  # zip file contains: " + ze.getName());
						ImportMasterRDFHandler.readBufferCountNewDataSources(zipStream, inputType);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ActiveTDB.syncTDBtoLCAHT();

		float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
//		System.out.println("Time elapsed: " + elapsedTimeSec);
		long now = ActiveTDB.getModel().size();
		long change = now - was;
		System.out.println("Was:" + was + " Added:" + change + " Now:" + now);
		runLogger.info("  # RDF triples before: " + NumberFormat.getIntegerInstance().format(was));
		runLogger.info("  # RDF triples after:  " + NumberFormat.getIntegerInstance().format(now));
		runLogger.info("  # RDF triples added:  " + NumberFormat.getIntegerInstance().format(change));
		int postDataSetCount = DataSourceKeeper.countDataSourcesInTDB();
		System.out.println("postDataSetCount = " + postDataSetCount);
		String proposedName = fileName;
		if (priorDataSetCount < postDataSetCount) {
			List<String> newNames = DataSourceKeeper.getDataSourceNamesInTDB();
			for (String name : newNames) {
				if (!currentNames.contains(name)) {
					proposedName = name;
				}
			}
		}

		// ==============

		fileMD.setFilename(fileName);
		fileMD.setPath(path);
		fileMD.setByteCount(file.length());
		fileMD.setModifiedDate(new Date(file.lastModified()));
		Date readDate = new Date();
		fileMD.setReadDate(readDate);
		runLogger.info("# File read at: " + Util.getLocalDateFmt(readDate));
		runLogger.info("# File last modified: " + Util.getLocalDateFmt(new Date(file.lastModified())));
		runLogger.info("# File size: " + file.length());

		System.out.println("All's fine before opening dialog");
		MetaDataDialog dialog = new MetaDataDialog(Display.getCurrent().getActiveShell(), fileMD, proposedName);
		System.out.println("meta initialized");
		dialog.create();
		System.out.println("meta created");
		boolean thing;
		if (thing = dialog.open() == MetaDataDialog.CANCEL) { // FIXME
			System.out.println("cancel!");

			fileMD.remove();
			return;
		}
		System.out.println("thing = " + thing);
		System.out.println("Got past opening dialog");

		// RUN QUERY TO PARSE THE FILE DATA NOW

		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select \n");
		b.append("  ?flowable  \n");
//		b.append("  (fn:substring(str(?f), ?flowable_length - 35) as ?flowable_uuid) \n");
		b.append("  ?cas \n");
		b.append("  ?formula \n");
		b.append("  ?context_general \n");
		b.append("  ?context_specific \n");
//		b.append("  (fn:substring(str(?cat), ?cat_length - 35) as?context_specific_uuid) \n");
		b.append("  ?reference_unit \n");
//		b.append("  (fn:substring(str(?ru), ?ru_length - 35) as?reference_unit_uuid) \n");
		b.append("  ?flow_property \n");
//		b.append("  (fn:substring(str(?fp), ?fp_length - 35) as?flow_property_uuid) \n");
		b.append(" \n");
		b.append("where { \n");
		b.append(" \n");
		b.append("  #--- FLOWABLE \n");
		b.append("  ?f olca:flowType olca:ELEMENTARY_FLOW . \n");
		b.append("  ?f olca:name ?flowable . \n");
		b.append("  optional { \n");
		b.append("    ?f olca:cas ?cas . \n");
		b.append("  } \n");
		b.append("  optional { \n");
		b.append("    ?f olca:formula ?formula . \n");
		b.append("  } \n");
		b.append("bind (fn:string-length(str(?f)) as ?flowable_length) \n");
		b.append(" \n");
		b.append("  #--- FLOW CONTEXT \n");
		b.append("  ?f olca:category ?cat . \n");
		b.append("  ?cat olca:name ?context_specific . \n");
		b.append("  ?cat olca:parentCategory ?parentCat . \n");
		b.append("  ?parentCat olca:name ?context_general . \n");
		b.append("  bind (fn:string-length(str(?cat)) as ?cat_length) \n");
		b.append(" \n");
		b.append(" \n");
		b.append("  #--- FLOW PROPERTY \n");
		b.append("  ?f olca:flowPropertyFactors ?fpf . \n");
		b.append("  ?fpf olca:flowProperty ?fp . \n");
		b.append("  ?fp olca:name ?flow_property . \n");
		b.append("  bind (fn:string-length(str(?fp)) as ?fp_length) \n");
		b.append(" \n");
		b.append("  ?fp olca:unitGroup ?ug . \n");
		b.append("  ?ug olca:referenceUnit ?ru . \n");
		b.append("  ?ru olca:name ?reference_unit . \n");
		b.append("  bind (fn:string-length(str(?ru)) as ?ru_length) \n");
		b.append(" \n");
		b.append("} \n");
		b.append("order by ?flowable  \n");

		String query = b.toString();

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();

		TableProvider tableProvider = TableProvider.createUserData((ResultSetRewindable) resultSet);

		tableProvider.setFileMD(fileMD);
		System.out.println("FileMD set in tableProvider");

		tableProvider.setDataSourceProvider(dialog.getCurDataSourceProvider());
		System.out.println("DataSource set in tableProvider");

		TableKeeper.saveTableProvider(path, tableProvider);
		System.out.println("Save tableProvider in TableKeeper");
		
		tableProvider.getHeaderRow().add(""); // THIS MAKES THE SIZE OF THE HEADER ROW ONE GREATER TODO: ADD A COLUMN COUNT FIELD TO TABLES

		Date readEndDate = new Date();
		int secondsRead = (int) ((readEndDate.getTime() - readDate.getTime()) / 1000);
		runLogger.info("# File read time (in seconds): " + secondsRead);

		try {
			Util.showView(CSVTableView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		System.out.println("About to update CSVTableView");
		tableProvider.setLCADataPropertyProvider(1, Flowable.getDataPropertyMap().get(Flowable.flowableNameString));
		tableProvider.setLCADataPropertyProvider(2, Flowable.getDataPropertyMap().get(Flowable.casString));
		tableProvider.setLCADataPropertyProvider(3, Flowable.getDataPropertyMap().get(Flowable.chemicalFormulaString));
		tableProvider.setLCADataPropertyProvider(4, FlowContext.getDataPropertyMap().get(FlowContext.flowContextGeneral));
		tableProvider.setLCADataPropertyProvider(5, FlowContext.getDataPropertyMap().get(FlowContext.flowContextSpecific));
		tableProvider.setLCADataPropertyProvider(6, FlowProperty.getDataPropertyMap().get(FlowProperty.flowPropertyUnit));
		tableProvider.setLCADataPropertyProvider(7, FlowProperty.getDataPropertyMap().get(FlowProperty.flowPropertyString));
		CSVTableView.update(path);
		return;

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