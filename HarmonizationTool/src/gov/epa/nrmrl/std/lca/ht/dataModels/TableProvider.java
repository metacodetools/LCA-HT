package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.FlowContext;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.FlowUnit;
import gov.epa.nrmrl.std.lca.ht.flowable.mgr.Flowable;
import gov.epa.nrmrl.std.lca.ht.workflows.FlowsWorkflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TableProvider {
	private DataSourceProvider dataSourceProvider = null;
	private FileMD fileMD = null;

	private DataRow headerRow = new DataRow();
	private List<DataRow> data = new ArrayList<DataRow>();
	private int lastChecked;
	private int lastUpdated;

	private LCADataPropertyProvider[] lcaDataProperties = null;
	private boolean containsUntranslatedOpenLCAData = false;
	
	private boolean existingLcaData = false;
	
	private static Set<String> metaDataColumns = new HashSet();
	static {
		metaDataColumns.add("adhoc");
		metaDataColumns.add("flowableMatch");
		metaDataColumns.add("contextMatch");
		metaDataColumns.add("unitMatch");
		metaDataColumns.add("lcaflowable");
		metaDataColumns.add("flowCtx");
		metaDataColumns.add("flowUnit");
		metaDataColumns.add("mf");
	}
	
	public LCADataPropertyProvider getLCADataPropertyProvider(int colNumber) {
		if (lcaDataProperties == null) {
			lcaDataProperties = new LCADataPropertyProvider[headerRow.getSize()];
		}
		return lcaDataProperties[colNumber];
	}

	public void setLCADataPropertyProvider(int colNumber, LCADataPropertyProvider lcaDataPropertyProvider) {
		if (lcaDataProperties == null) {
			lcaDataProperties = new LCADataPropertyProvider[headerRow.getSize()];
		}
		lcaDataProperties[colNumber] = lcaDataPropertyProvider;
	}

	public void addDataRow(DataRow dataRow) {
		data.add(dataRow);
		dataRow.setRowNumber(data.indexOf(dataRow));
	}

	public int getColumnCount() {
		if (data.size() > 0)
			return data.get(0).getSize();
		return 0;
	}

	public String checkColumnCountConsistency() {
		String issues = "";
		int shorterRows = 0;
		int longerRows = 0;
		int colCount = getColumnCount();
		if (headerRow.getSize() < colCount) {
			issues = "Header row shorter than first data row.\\n";
		} else if (headerRow.getSize() > colCount) {
			issues = "Header row longer than first data row.\\n";
		}
		for (DataRow dataRow : data) {
			if (dataRow.getSize() > colCount) {
				longerRows++;
			} else if (dataRow.getSize() < colCount) {
				shorterRows++;
			}
		}
		if (shorterRows > 0) {
			issues += "Found " + shorterRows + "rows shorter than the first row.\\n";
		}
		if (longerRows > 0) {
			issues += "Found " + longerRows + "rows longer than the first row.\\n";
		}
		if (issues.equals("")) {
			return null;
		}
		return issues;
	}

	public List<DataRow> getData() {
		return data;
	}

	public int getIndex(DataRow dataRow) {
		return data.indexOf(dataRow);
	}

	public DataRow getHeaderRow() {
		return headerRow;
	}

	public List<String> getHeaderNamesAsStrings() {
		List<String> headerNamesAsStrings = new ArrayList<String>();
		// System.out.println("headerNames.getSize()=" + headerNames.getSize());
		Iterator<String> iter = headerRow.getIterator();
		while (iter.hasNext()) {
			headerNamesAsStrings.add(iter.next());
		}
		// System.out.println("returning headerNames.getSize()=" +
		// headerNames.getSize());
		return headerNamesAsStrings;
	}

	public void setHeaderNames(List<String> columnNames) {
		assert columnNames != null : "columnNames cannot be null";
		assert columnNames.size() != 0 : "columnNames cannot be empty";
		if (headerRow == null) {
			headerRow = new DataRow();
		} else {
			headerRow.clear();
		}
		for (String name : columnNames) {
			if (existingLcaData) {
				if (metaDataColumns.contains(name))
					continue;
			}
			headerRow.add(name);
			headerRow.setRowNumber(-1);
		}
	}

	public static TableProvider create(ResultSetRewindable resultSetRewindable) {
		TableProvider tableProvider = new TableProvider();
		resultSetRewindable.reset();
		tableProvider.setHeaderNames(resultSetRewindable.getResultVars());
		while (resultSetRewindable.hasNext()) {
			QuerySolution soln = resultSetRewindable.nextSolution();
			DataRow dataRow = new DataRow();
			tableProvider.addDataRow(dataRow);
			Iterator<String> iterator = tableProvider.getHeaderRow().getIterator();
			while (iterator.hasNext()) {
				String header = iterator.next();
				try {
					RDFNode rdfNode = null;
					rdfNode = soln.get(header);
					if (rdfNode == null) {
						dataRow.add("");

					} else {
						dataRow.add(rdfNode.toString());
						System.out.println("Resource string is " + rdfNode.toString());
						System.out.println("Type of RDFNode = " + RDFNode.class.getName());
						// System.out.println("  soln.getResource(header) =" +
						// soln.getResource(header));
						System.out.println("  soln.get(header)  = " + rdfNode);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Returned " + resultSetRewindable.size() + " results");
		return tableProvider;
	}

	public int getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(int lastChecked) {
		this.lastChecked = lastChecked;
	}

	public int getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(int lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public DataSourceProvider getDataSourceProvider() {
		return dataSourceProvider;
	}

	public void setDataSourceProvider(DataSourceProvider dataSourceProvider) {
		this.dataSourceProvider = dataSourceProvider;
	}

	public FileMD getFileMD() {
		return fileMD;
	}

	public void setFileMD(FileMD fileMD) {
		this.fileMD = fileMD;
	}

	public LCADataPropertyProvider[] getLcaDataProperties() {
		if (lcaDataProperties == null) {
			lcaDataProperties = new LCADataPropertyProvider[headerRow.getSize()];
		}
		return lcaDataProperties;
	}

	public void setLcaDataProperties(LCADataPropertyProvider[] lcaDataProperties) {
		this.lcaDataProperties = lcaDataProperties;
	}

//	public static TableProvider createUserData(ResultSetRewindable resultSetRewindable) {
//		TableProvider tableProvider = new TableProvider();
//		resultSetRewindable.reset();
//		tableProvider.setHeaderNames(resultSetRewindable.getResultVars());
//		// int count = 0;
//		while (resultSetRewindable.hasNext()) {
//			QuerySolution soln = resultSetRewindable.nextSolution();
//			DataRow dataRow = new DataRow();
//			// dataRow.setRowNumber(count);
//			tableProvider.addDataRow(dataRow);
//			Iterator<String> iterator = tableProvider.getHeaderRow().getIterator();
//			while (iterator.hasNext()) {
//				String header = iterator.next();
//				try {
//					RDFNode rdfNode = null;
//					rdfNode = soln.get(header);
//					if (rdfNode == null) {
//						dataRow.add("");
//					} else {
//						dataRow.add(rdfNode.toString());
//
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//		return tableProvider;
//	}
	
	public void setExistingData(boolean existingData) {
		existingLcaData = existingData;
	}

	public void createUserData(ResultSetRewindable resultSetRewindable) {
		resultSetRewindable.reset();
		setHeaderNames(resultSetRewindable.getResultVars());
		// int count = 0;
		
		int matchedFlowables = 0;
		int matchedFlowContexts = 0;
		int matchedFlowUnits = 0;
		
		Map<String, Flowable> uniqueFlowables = new HashMap<String, Flowable>();
		Map<String, FlowContext> uniqueFlowContexts = new HashMap<String, FlowContext>();
		Map<String, FlowUnit> uniqueFlowUnits = new HashMap<String, FlowUnit>();
		while (resultSetRewindable.hasNext()) {
			QuerySolution soln = resultSetRewindable.nextSolution();
			DataRow dataRow = new DataRow();
			// dataRow.setRowNumber(count);
			addDataRow(dataRow);
			Iterator<String> iterator = getHeaderRow().getIterator();
			while (iterator.hasNext()) {
				String header = iterator.next();
				try {
					RDFNode rdfNode = null;
					rdfNode = soln.get(header);
					if (rdfNode == null) {
						dataRow.add("");
					} else {
						dataRow.add(rdfNode.toString());

					}
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
			if (existingLcaData) {
				RDFNode rdfNode = soln.get("lcaflowable");
				String flowable = null;
				if (rdfNode != null)
					flowable = rdfNode.toString();
				Color color = null;
				if (flowable != null &&!uniqueFlowables.containsKey(flowable)) {
					Flowable flowableObj = new Flowable((Resource)rdfNode);
					uniqueFlowables.put(flowable, flowableObj);
					dataRow.setFlowable(flowableObj);
					int adHoc = soln.getLiteral("adhoc").getInt();
					int flowableMatches = soln.getLiteral("flowableMatch").getInt();
					if (adHoc != 0) {
						color = SWTResourceManager.getColor(SWT.COLOR_CYAN);
					} else {
						if (flowableMatches == 1) {
							color = SWTResourceManager.getColor(SWT.COLOR_GREEN);
							++matchedFlowables;
						}
						else if (flowableMatches > 1)
							color = CSVTableView.orange;
						else
							color = SWTResourceManager.getColor(SWT.COLOR_YELLOW);
					}
					dataRow.setFlowableColor(color);
				}
				else if (flowable != null)
					dataRow.setFlowable(uniqueFlowables.get(flowable));
				String flowContext = null;
				rdfNode = soln.get("flowCtx");
				if (rdfNode != null)
					flowContext = rdfNode.toString();
				if (flowContext != null && !uniqueFlowContexts.containsKey(flowContext)) {
					FlowContext fcObj = new FlowContext((Resource)rdfNode, true);
					uniqueFlowContexts.put(flowContext, fcObj);
					dataRow.setFlowContext(fcObj);
					int contextMatches = soln.getLiteral("contextMatch").getInt();
					if (contextMatches > 0) {
						dataRow.setFlowContextColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
						++matchedFlowContexts;
					}
					else
						dataRow.setFlowContextColor(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
				}
				else if (flowContext != null)
					dataRow.setFlowContext(uniqueFlowContexts.get(flowContext));
				String flowUnit = null;
				rdfNode = soln.get("flowUnit");
				if (rdfNode != null)
					flowUnit = rdfNode.toString();
				if (flowUnit != null && !uniqueFlowUnits.containsKey(flowUnit)) {
					FlowUnit fuObj = new FlowUnit((Resource)rdfNode, false);
					uniqueFlowUnits.put(flowUnit, fuObj);
					dataRow.setFlowUnit(fuObj);
					int unitMatches = soln.getLiteral("unitMatch").getInt();
					if (unitMatches > 0) {
						dataRow.setFlowPropertyColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
						++matchedFlowUnits;
					}
					else
						dataRow.setFlowPropertyColor(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
				}
				else if (flowUnit != null)
					dataRow.setFlowUnit(uniqueFlowUnits.get(flowUnit));
				rdfNode = soln.get("mf");
				if (rdfNode != null)
					dataRow.setFlowColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
				else
					dataRow.setFlowColor(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
			}

		}
		
		if (existingLcaData) {
			FlowsWorkflow.showFlowableMatchCount(matchedFlowables, uniqueFlowables.size());
			FlowsWorkflow.showFlowContextMatchCount(matchedFlowContexts, uniqueFlowContexts.size());
			FlowsWorkflow.showFlowUnitMatchCount(matchedFlowUnits, uniqueFlowUnits.size());
		}

		return;
	}

	public void colorExistingRows() {
		if (!existingLcaData)
			return;
		
		Set<Integer> flowColumnNumbers = new HashSet<Integer>();
		Set<Integer> flowableCSVColumnNumbers = new HashSet<Integer>();
		Set<Integer> flowContextCSVColumnNumbers = new HashSet<Integer>();
		Set<Integer> flowPropertyCSVColumnNumbers = new HashSet<Integer>();
		int flowCSVColumnNumberForUUID = -1;

		// NOTE: assignedCSVColumns[0] SHOULD BE NULL (NO DATA IN THAT COLUMN)
		for (int i = 1; i < lcaDataProperties.length; i++) {
			LCADataPropertyProvider lcaDataPropertyProvider = lcaDataProperties[i];
			if (lcaDataPropertyProvider == null) {
				continue;
			}
			if (lcaDataPropertyProvider.getPropertyClass().equals(Flowable.label)) {
				flowableCSVColumnNumbers.add(i);
			} else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowContext.label)) {
				flowContextCSVColumnNumbers.add(i);
			} else if (lcaDataPropertyProvider.getPropertyClass().equals(FlowUnit.label)) {
				flowPropertyCSVColumnNumbers.add(i);
			} else if (lcaDataPropertyProvider.getPropertyClass().equals(Flow.label)) {
				flowColumnNumbers.add(i);
				if (lcaDataPropertyProvider.getPropertyName().equals(Flow.openLCAUUID)) {
					flowCSVColumnNumberForUUID = i;
				}
			}
			
		}
	
		Table table = CSVTableView.getTable();
		List<String> masterFlowUUIDs = CSVTableView.getMasterFlowUUIDs();

		
		for (DataRow row : data) {
			TableItem tableItem = table.getItem(row.getRowNumber());
			
			Color color = row.getFlowableColor();
			if (color != null) {
				for (int column : flowableCSVColumnNumbers)
					tableItem.setBackground(column, color);
			}
			
			color = row.getFlowContextColor();
			if (color != null) {
				for (int column: flowContextCSVColumnNumbers)
					tableItem.setBackground(column, color);
			}
			
			color = row.getFlowPropertyColor();
			if (color != null) {
				for (int column: flowPropertyCSVColumnNumbers)
					tableItem.setBackground(column, color);
			}
			
			color = row.getFlowColor();
			if (color != null) {
				for (int j : flowColumnNumbers) {
					tableItem.setBackground(j, color);
				}
				tableItem.setBackground(0, color);
			}
			if (flowCSVColumnNumberForUUID != -1) {
				String uuid = row.get(flowCSVColumnNumberForUUID - 1);
				if (masterFlowUUIDs.contains(uuid)) {
					tableItem.setBackground(0, CSVTableView.orange);
					tableItem.setBackground(flowCSVColumnNumberForUUID, CSVTableView.orange);
				}
			}
		}
	}
	
	public void setContainsUntranslatedOpenLCAData(boolean b) {
		this.containsUntranslatedOpenLCAData = b;
	}

	public boolean doesContainUntranslatedOpenLCAData() {
		return containsUntranslatedOpenLCAData;
	}
}
