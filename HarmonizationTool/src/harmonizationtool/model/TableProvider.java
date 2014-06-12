package harmonizationtool.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Menu;
//import org.eclipse.swt.widgets.TableColumn;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TableProvider {
	private static int subRowNum;
	private DataRow headerRow = new DataRow();
	private List<DataRow> data = new ArrayList<DataRow>();
	private List<Resource> uriList = new ArrayList<Resource>();
	private Menu menu = null;

	// public List<Resource> uris = new ArrayList<Resource>();
	public static final String SUBROW_PREFIX = "sr";
	public static final String SUBROW_NAMEHEADER = "srName";
	public static final String SUBROW_SUB_URI = "srSubURI";
	public static final String SUBROW_SOURCE_TAB_ROW = "srSourceTabRow";

	public void addDataRow(DataRow dataRow) {
		data.add(dataRow);
		dataRow.setRowNumber(data.indexOf(dataRow));
	}

	public void addUri(Resource uri) {
		// if (uri == null){
		// System.out.println("Trying to add null URI with uriList.size() = "+uriList.size());
		// }

		uriList.add(uri);
		System.out.println("Added URI: " + uri + ".  Now the uriList.size() is " + +uriList.size());

	}

	public int getColumnCount() {
		return data.get(0).getSize();
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

	public void setMenu(Menu menu) {
		this.menu = menu;
	}

	public Menu getMenu() {
		return menu;
	}

	public List<DataRow> getData() {
		return data;
	}

	public List<Resource> getUriList() {
		return uriList;
	}

	public int getIndex(DataRow dataRow) {
		return data.indexOf(dataRow);
	}

	public int getUriIndex(Resource uri) {
		return uriList.indexOf(uri);
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
		return tableProvider;
	}

	private class TransformCell {
		public final int rowNum;
		public final String newHeader;

		public TransformCell(int rowNum, String newHeader) {
			this.rowNum = rowNum;
			this.newHeader = newHeader;
		}
	}

	public static TableProvider createTransform0(ResultSetRewindable resultSetRewindable) {
		TableProvider tableProvider = new TableProvider();
		resultSetRewindable.reset();
		List<String> origHeaderNames = resultSetRewindable.getResultVars();
		String dataSetName = "DataSet";
		tableProvider.headerRow.add(dataSetName); // THE FIRST COLUMN IS
													// DataSet
		Map<String, TransformCell> headerMap = new HashMap<String, TransformCell>();
		for (String origHeader : origHeaderNames) {
			int headerSplitPoint = origHeader.indexOf("_");
			if (headerSplitPoint == -1) {
				tableProvider.headerRow.add(origHeader); // NOT A
															// subRowHeader. ADD
															// IT
				TransformCell transformCell = tableProvider.new TransformCell(0, origHeader);
				headerMap.put(origHeader, transformCell);
			} else {
				String origHeaderPrefix = origHeader.substring(0, headerSplitPoint);
				String origHeaderField = origHeader.substring(headerSplitPoint + 1);
				if (origHeaderPrefix.matches("^" + SUBROW_PREFIX + "\\d+$")) {
					subRowNum = Integer.parseInt(origHeaderPrefix.substring(SUBROW_PREFIX.length()));

					if (origHeaderField.equals(SUBROW_NAMEHEADER)) { // DataSet
																		// FIELD
						TransformCell transformCell = tableProvider.new TransformCell(subRowNum, dataSetName);
						headerMap.put(origHeader, transformCell);
					} else if (origHeaderField.equals(SUBROW_SUB_URI)) {
						// tableProvider.addUri(Util.resolveUriFromString(uriString));
						// //
						// NOTHING HERE

						// TransformCell transformCell = tableProvider.new
						// TransformCell(subRowNum, origHeaderField);
						// headerMap.put(origHeader, transformCell);
						// if (subRowNum == 1) {
						// tableProvider.headerNames.add(origHeaderField); \\
						// THIS LINE AND THE ONE BELOW ARE EQUIVALENT
						// }
					} else if (origHeaderField.equals(SUBROW_SOURCE_TAB_ROW)) {
						// NOTHING HERE
					} else {
						TransformCell transformCell = tableProvider.new TransformCell(subRowNum, origHeaderField);
						headerMap.put(origHeader, transformCell);
						if (subRowNum == 1) {
							tableProvider.headerRow.add(origHeaderField);
						}
					}
				} else {
					tableProvider.headerRow.add(origHeader); // NOT A
																// subRowHeader.
																// ADD IT
					TransformCell transformCell = tableProvider.new TransformCell(0, origHeader);
					headerMap.put(origHeader, transformCell);
				}
			}
		}

		System.out.println("tableProvider.headerNames.toString() = " + tableProvider.headerRow.toString());
		System.out.println("headerMap.keySet().toString() = " + headerMap.keySet().toString());

		boolean debugFlag = true;
		resultSetRewindable.reset();
		while (resultSetRewindable.hasNext()) {
			QuerySolution soln = resultSetRewindable.nextSolution();

			// tableProvider.addUri(soln.getResource( SUBROW_SUB_URI ));
			// BUILD A 2-D ARRAY (ROWS AND COLS) CONTAINING THE MULTIPLE
			// SUB-ROWS BASED ON ONE
			// QUERY RESULT ROW
			Map<Integer, DataRow> twoDArray = new HashMap<Integer, DataRow>();
			for (String origHeader : origHeaderNames) {
				if (debugFlag) {
					System.out.println("origHeader =" + origHeader);
				}

				String regexUri = SUBROW_PREFIX + "(\\d)_" + SUBROW_SUB_URI;
				if (origHeader.matches(regexUri)) {
					// Pattern uriMatch = Pattern.compile(regexUri);
					//
					// Matcher matcher = uriMatch.matcher(origHeader);
					// matcher.find();
					// int rowForUriNum =
					// Integer.parseInt(matcher.group(0).trim());

					System.out.println("soln.getResource(\"origHeader\") = " + soln.getResource(origHeader));
					tableProvider.addUri(soln.getResource(origHeader));
				}

				if (headerMap.containsKey(origHeader)) { // SPECIAL HEADERS
															// WON'T MAP,
															// THEY'RE URIs
					TransformCell transformCell = headerMap.get(origHeader);
					int colNum = tableProvider.getHeaderNamesAsStrings().indexOf(transformCell.newHeader);
					int rowNum = transformCell.rowNum;
					DataRow dataRow = twoDArray.get(rowNum);
					if (dataRow == null) {
						twoDArray.put(rowNum, new DataRow());
						dataRow = twoDArray.get(rowNum);
					}
					while (dataRow.getSize() < colNum + 1) {
						dataRow.add("");
						if (debugFlag) {
							System.out.println("rowNum:" + rowNum + " colNum:" + colNum + " dataRow.getSize() is now:" + dataRow.getSize());
						}
					}
					try {
						RDFNode rdfNode = soln.get(origHeader);
						if (rdfNode == null) {
							dataRow.set(colNum, "");
						} else {
							dataRow.set(colNum, rdfNode.toString());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// NOW TAKE NON EMPTY ITEMS FROM dataRow ZERO AND FILL THE COLUMN
			// BELOW
			DataRow dataRowFillSource = twoDArray.get(0);
			for (int col = 0; col < dataRowFillSource.getSize(); col++) {
				if (!dataRowFillSource.get(col).equals("")) {
					for (int row = 1; row < twoDArray.size(); row++) {
						DataRow dataRowJ = twoDArray.get(row);
						while (dataRowJ.getSize() < col + 1) {
							dataRowJ.add("");
						}
						String valTwoFillCol = dataRowFillSource.get(col);
						dataRowJ.set(col, valTwoFillCol);
						if (!dataRowJ.get(0).equals("")) { // DON'T ADD A ROW IF
															// THERE IS NO HIT!!
							tableProvider.addDataRow(dataRowJ);
						}
					}
				}
			}
			debugFlag = false;
		}
		return tableProvider;

		// throw new IllegalArgumentException("implement me");
	}

}
