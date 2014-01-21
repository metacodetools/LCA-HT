package harmonizationtool;

import java.util.ArrayList;
import java.util.List;

import harmonizationtool.ViewData.MyColumnLabelProvider;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.edit.CSVEdittingSupport;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.ITableProvider;
import harmonizationtool.model.TableProvider;
import harmonizationtool.query.HarmonyQuery;
import harmonizationtool.query.QueryResults;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ResultsView extends ViewPart {
	public static final String ID = "HarmonizationTool.ResultsViewID";

	private TableViewer viewer;
	private static List<Object> columns = new ArrayList<Object>();
	private TableColumn columnSelected = null;
	private QueryResults queryResults = null;

	/**
	 * The content provider class is responsible for providing objects to the view. It can wrap
	 * existing objects in adapters or simply return objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore it and always show the same content (like Task
	 * List, for example).
	 */
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent instanceof Object[]) {
				return (Object[]) parent;
			}
			return new Object[0];
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
//		viewer.addDoubleClickListener(new IDoubleClickListener(){
//			@Override
//			public void doubleClick(DoubleClickEvent event) {
//				ISelection selection = event.getSelection();
//				System.out.println("selection=" + selection);
//			}});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	public void update(List<String> data){
//		viewer.setInput(data.toArray());
	}
	
	public void iUpdate(List<String> data){
//		viewer.setInput(data.toArray());
	}

	public void update(QueryResults queryResults) {
		try {
			this.queryResults = queryResults;
			System.err.println("queryResults="+queryResults);
			System.err.println("queryResults.getColumnHeaders()="+queryResults.getColumnHeaders());
			System.out.println("queryResults.getColumnHeaders().toString()="+queryResults.getColumnHeaders().toString());
			viewer.setContentProvider(new ArrayContentProvider());
			final Table table = viewer.getTable();
			removeColumns(table);
			createColumns(viewer, queryResults);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			viewer.setContentProvider(new ArrayContentProvider());
			TableProvider tableProvider = queryResults.getTableProvider();
			System.out.println("tableProvider.getData().size()="+tableProvider.getData().size());
			System.out.println("tableProvider.getData().toString()="+tableProvider.getData().toString());
			viewer.setInput(tableProvider.getData());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void iUpdate(QueryResults iQueryResults) {
		try {
			this.queryResults = iQueryResults;
			System.err.println("iQueryResults="+iQueryResults);
			System.err.println("iQueryResults.getColumnHeaders()="+iQueryResults.getColumnHeaders());
			System.out.println("iQueryResults.getColumnHeaders().toString()="+iQueryResults.getColumnHeaders().toString());
			viewer.setContentProvider(new ArrayContentProvider());
			final Table table = viewer.getTable();
			removeColumns(table);
			createIColumns(viewer, iQueryResults);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			viewer.setContentProvider(new ArrayContentProvider());
			ITableProvider iTableProvider = iQueryResults.getITableProvider();
			System.out.println("iTableProvider.getDataXform().size()="+iTableProvider.getDataXform().size());
			System.out.println("iTableProvider.getDataXform().toString()="+iTableProvider.getDataXform().toString());
			viewer.setInput(iTableProvider.getDataXform());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * removes columns from the given table
	 * 
	 * @param table
	 */
	private void removeColumns(Table table) {
		System.out.println(this.getClass().getName() + ".removeColumns(table)");
		table.setRedraw(false);
		while (table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
		try {
			table.setRedraw(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void createColumns(final TableViewer viewer,QueryResults queryResults) {
		DataRow columnHeaders = queryResults.getColumnHeaders();
//		System.out.println("key=" + key);
//		if (key != null) {
			// Define the menu and assign to the table
//			headerMenu = new Menu(viewer.getTable());
//			viewer.getTable().setMenu(headerMenu);
//			initializeColumnHeaderMenu(headerMenu);

//			ModelProvider modelProvider = ModelKeeper.getModelProvider(key);
//			List<DataRow> dataRowList = modelProvider.getData();
//			DataRow dataRow = dataRowList.get(0);
//			int numCol = dataRow.getColumnValues().size();
//			ArrayList<String> titles = new ArrayList<String>();
//			ArrayList<Integer> bounds = new ArrayList<Integer>();
//			for (int i = 1; i <= numCol; i++) {
//				// titles.add("COL" + i);
//				titles.add("Ignore");
//				bounds.add(100);
//			}
		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<Integer> bounds = new ArrayList<Integer>();
			for (String header : columnHeaders.getColumnValues()) {
				titles.add(header);
				bounds.add(100);
			}
			String[] titlesArray = new String[titles.size()];
			titles.toArray(titlesArray);
			int[] boundsArray = new int[bounds.size()];
			int indx = 0;
			for (Integer integer : bounds) {
				boundsArray[indx++] = integer;
			}
			for (int i = 0; i < titles.size(); i++) {
				TableViewerColumn col = createTableViewerColumn(titlesArray[i], boundsArray[i], i);
				col.setLabelProvider(new MyColumnLabelProvider(i));
				columns.add(col);
			}
//		}
	}
	
	private void createIColumns(final TableViewer viewer,QueryResults iQueryResults) {
		ITableProvider iTableProvider = iQueryResults.getITableProvider();
		System.out.println("iQueryResults.getColumnHeaders().getSize() : "+iQueryResults.getColumnHeaders().getSize());
		System.out.println("iQueryResults.getITableProvider() : "+iQueryResults.getITableProvider());
		System.out.println("iQueryResults.getITableProvider().getHeaderNames() : "+iQueryResults.getITableProvider().getHeaderNames());
		System.out.println("iQueryResults.getITableProvider().getHeaderNames().size() : "+iQueryResults.getITableProvider().getHeaderNames().size());
		iTableProvider.setColumnNames(iTableProvider.getHeaderNames());
		iTableProvider.setXformNames(iTableProvider.getHeaderNames());
		DataRow columnXformHeaders = iTableProvider.getColumnXformHeaders();
		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<Integer> bounds = new ArrayList<Integer>();
			for (String header : columnXformHeaders.getColumnValues()) {
				titles.add(header);
				bounds.add(100);
			}
			String[] titlesArray = new String[titles.size()];
			titles.toArray(titlesArray);
			int[] boundsArray = new int[bounds.size()];
			int indx = 0;
			for (Integer integer : bounds) {
				boundsArray[indx++] = integer;
			}
			for (int i = 0; i < titles.size(); i++) {
				TableViewerColumn col = createTableViewerColumn(titlesArray[i], boundsArray[i], i);
				col.setLabelProvider(new MyColumnLabelProvider(i));
				columns.add(col);
			}
//		}
	}
	/**
	 * convenience method for creating a TableViewerColumn
	 * 
	 * @param title
	 * @param bound
	 * @param colNumber
	 * @return
	 */
	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		viewerColumn.setEditingSupport(new CSVEdittingSupport(viewer, colNumber));
//		column.addListener(eventType, new Listener(){});
		column.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(e.toString());
				System.out.println(e.getSource().getClass());
				if (e.getSource() instanceof TableColumn) {
					TableColumn col = (TableColumn) e.getSource();
					System.out.println("col: " + col.getText());
					columnSelected = col;
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println(e.toString());
			}

		});
		// Now add a MenuItem for the colum to the table menu
//		createMenuItem(headerMenu, column);
		return viewerColumn;
	}
	/**
	 * class for generating column labels. This class will handle a variable number of columns
	 * 
	 * @author tec
	 */
	class MyColumnLabelProvider extends ColumnLabelProvider {
		private int myColNum;

		public MyColumnLabelProvider(int colNum) {
			this.myColNum = colNum;
		}


		@Override
		public String getText(Object element) {
			DataRow dataRow = null;
			try {
				dataRow = (DataRow) element;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("element= " + element);
			}
			String s = "";
			try {
				int size = dataRow.getColumnValues().size();
				if (myColNum < size) {
					s = dataRow.getColumnValues().get(myColNum);
				}
			} catch (Exception e) {
				System.out.println("dataRow=" + dataRow);
				e.printStackTrace();
			}
			return s;
		}
	}

	public QueryResults getQueryResults() {
		return queryResults;
	}


}
