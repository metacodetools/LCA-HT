package harmonizationtool;

//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import harmonizationtool.model.DataRow;
import harmonizationtool.model.TableKeeper;
import harmonizationtool.model.TableProvider;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.MouseEvent;
//import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.jface.viewers.ComboViewer;

/**
 * @author tec
 * 
 */
public class ViewData extends ViewPart {
	public ViewData() {
	}

	public static final String ID = "HarmonizationTool.viewData";
	private static String key = null;
	private TableViewer tableViewer;
	private Table table;
	private static List<Object> columns = new ArrayList<Object>();
	// the menu that is displayed when column header is right clicked
	private Menu headerMenu;
	private TableColumn columnSelected = null;
	private Combo combo;

	public static final String IMPACT_ASSESSMENT_METHOD_HDR = "Impact Assessment Method";
	// e.g. ReCiPe or TRACI

	public static final String IMPACT_CAT_HDR = "Impact_Category";
	// e.g. climate change
	// public static final Resource ImpactCategory = ECO.ImpactCategory;

	public static final String IMPACT_CAT_INDICATOR_HDR = "Impact category indicator";
	// e.g. infrared radiative forcing

	public static final String IMPACT_CHARACTERIZATION_MODEL_HDR = "Impact Characterization Model"; // e.g.
	// IPCC Global Warming Potential (GWP)

	public static final String IMPACT_CAT_REF_UNIT_HDR = "Impact cat ref unit";
	// e.g. kg CO2 eq

	public static final String CAT1_HDR = "Category"; // e.g. air
	public static final String CAT2_HDR = "Subcategory"; // e.g. low population
	public static final String CAT3_HDR = "Sub-subcategory";

	public static final String NAME_HDR = "Flowable Name";
	public static final String CASRN_HDR = "CASRN";
	public static final String ALT_NAME_HDR = "Flowable Alt_Name";

	// ECO.ImpactCharacterizationFactor;
	public static final String CHAR_FACTOR_HDR = "Characterization factor";
	// THIS IS THE (float) NUMBER

	public static final String FLOW_UNIT_HDR = "Flow Unit";
	// e.g. kg

	public static final String IGNORE_HDR = "Ignore";



//	@Override
//	public void dispose() {
//		super.dispose();
//	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(null);
		
		ComboViewer comboViewer = new ComboViewer(parent, SWT.READ_ONLY);
		combo = comboViewer.getCombo();
		combo.setVisible(false);

		combo.setBounds(0, 0, 297, 469);
		combo.add(IMPACT_ASSESSMENT_METHOD_HDR);
		combo.add(IMPACT_CAT_HDR);
		combo.add(IMPACT_CAT_INDICATOR_HDR);
		combo.add(IMPACT_CHARACTERIZATION_MODEL_HDR);
		combo.add(IMPACT_CAT_REF_UNIT_HDR);
		combo.add("");
		combo.add(FLOW_UNIT_HDR);
		combo.add(CHAR_FACTOR_HDR);
		combo.add("");
		combo.add(CAT1_HDR);
		combo.add(CAT2_HDR);
		combo.add(CAT3_HDR);
		combo.add("");
		combo.add(NAME_HDR);
		combo.add(CASRN_HDR);
		combo.add(ALT_NAME_HDR);
		combo.add("");
		combo.add(IGNORE_HDR);
		combo.setBounds(0 , 0, 200, 40);

		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.READ_ONLY);
		table = tableViewer.getTable();
		table.setBounds(0, 0, 650, 650);

		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				System.out.println("============row selected=======");
//				System.out.println(selection.getClass().getName());
				Object element = selection.getFirstElement();
//				System.out.println(element.getClass().getName());
				DataRow dataRow = (DataRow) element;
				TableProvider tableProvider = TableKeeper.getTableProvider(key);
				int index = tableProvider.getIndex(dataRow);
				System.out.println("index=" + index);
			}
		});

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}

	public void update(String key) {
		this.key = key;

		tableViewer.setContentProvider(new ArrayContentProvider());
//		final Table table = tableViewer.getTable();
		removeColumns(table);
//		createColumns(tableViewer);
		createColumns();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tableViewer.setContentProvider(new ArrayContentProvider());
		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		// System.out.println("tableProvider.getData()="+tableProvider.getData());
		// System.out.println("tableProvider.getData().toString()="+tableProvider.getData().toString());
		tableViewer.setInput(tableProvider.getData());
		// viewer.refresh();

	}

	public void clearView(String key) {
		if ((key != null) && (key.equals(this.key))) {
//			final Table table = tableViewer.getTable();
			tableViewer.setInput(null);
			removeColumns(table);
			table.setHeaderVisible(false);
			table.setLinesVisible(false);
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
		table.setRedraw(true);
	}

	private void createColumns() {
		System.out.println("key=" + key);
		if (key != null) {
			// Define the menu and assign to the table
			headerMenu = new Menu(table);
//			table.setMenu(headerMenu);
			initializeColumnHeaderMenu();

			TableProvider tableProvider = TableKeeper.getTableProvider(key);
			DataRow header = tableProvider.getHeaderNames();
			List<DataRow> tableData = tableProvider.getData();
			DataRow dataRow = tableData.get(0);
			while(header.getSize() < dataRow.getSize()){
				header.add("ignore");
			}
			int numCol = header.getSize();
			System.out.println("numCol = " + numCol);

			for (int i = 0; i < numCol; i++) {
				if (header.get(i) == null) {
					header.set(i, "ignore");
				}
				TableViewerColumn col = createTableViewerColumn(header.get(i),
						100, i);
				col.setLabelProvider(new MyColumnLabelProvider(i));
				// tableProvider.addHeaderName(titlesArray[i],col.hashCode());
				columns.add(col);
			}
			saveColumnNames();
		}
	}

	/**
	 * class for generating column labels. This class will handle a variable
	 * number of columns
	 * 
	 * @author tec
	 */
	class MyColumnLabelProvider extends ColumnLabelProvider {
		private int myColNum;

		public MyColumnLabelProvider(int colNum) {
//			System.out.println("column was "+colNum);
			this.myColNum = colNum;
//			System.out.println("column now "+colNum);

		}

		@Override
		public String getText(Object element) {
//			System.out.println("getText from column: "+myColNum);
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

	/**
	 * convenience method for creating a TableViewerColumn
	 * 
	 * @param title
	 * @param bound
	 * @param colNumber
	 * @return
	 */
	private TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer,
				SWT.NONE, colNumber);
		final TableColumn column = viewerColumn.getColumn();
//		viewerColumn.
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(false);
		column.setToolTipText("Column "+colNumber);		
		
		column.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof TableColumn) {
					TableColumn col = (TableColumn) e.getSource();
					// set columnSelected; this is used when editing the column
					// headers
					columnSelected = col;
					int colSelectionIndex = Integer.parseInt(col.getToolTipText().substring(7));
					headerMenu.setLocation(colSelectionIndex*100, 0);
//					System.out.println("headerMenu.getParentItem().getSelection() ="+headerMenu.getParentItem().getSelection());
//					System.out.println("headerMenu.getParentItem() ="+headerMenu.getParentItem());					headerMenu.setLocation(colSelectionIndex, 0);
					headerMenu.setVisible(true);

//					combo.setBounds(colSelectionIndex * 100, 0, 200, 20);
//					combo.setVisible(true);
//					combo.setFocus();
//					setColumnHeaderMenu();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.getSource() instanceof TableColumn) {
					TableColumn col = (TableColumn) e.getSource();
					// set columnSelected; this is used when editing the column
					// headers
					columnSelected = col;
					int colSelectionIndex = Integer.parseInt(col.getToolTipText().substring(7));
					System.out.println("colSelectionIndex ="+colSelectionIndex);
					headerMenu.setLocation(colSelectionIndex*100, 0);
//					System.out.println("headerMenu.getParentItem().getSelection() ="+headerMenu.getParentItem().getSelection());
//					System.out.println("headerMenu.getParentItem() ="+headerMenu.getParentItem());
					headerMenu.setVisible(true);
//					combo.setBounds(colSelectionIndex * 100, 0, 200, 20);
//					combo.setVisible(true);
//					combo.setFocus();
					
				}
			}

		});

		return viewerColumn;
	}
	
	private void setColumnHeaderMenu() {
//		ColumnSelectionListener colListener = new ColumnSelectionListener();
//		MenuItem menuItem = new MenuItem(Shell parentShell, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
//		menuItem.setText(CASRN_HDR);


	}


	/**
	 * this method initializes the headerMenu with menuItems and a
	 * ColumnSelectionListener
	 * 
	 * @param menu
	 *            headerMenu which allows user to rename the columns
	 */
	private void initializeColumnHeaderMenu() {
//		ColumnSelectionListener colListener = new ColumnSelectionListener();
//		menu.setData(columnSelected);
		headerMenu.addListener(SWT.Selection, new ColumnSelectionListener() {
		      public void handleEvent(Event e) {
		  		System.out.println("widgetSelected e= "+e);
		          System.out.println("Select All");
		          
		        }
		      });
		headerMenu.setLocation(300, 500);
		MenuItem menuItem = new MenuItem(headerMenu, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(CASRN_HDR);

		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(NAME_HDR);

		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(ALT_NAME_HDR);

		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(CAT1_HDR);

		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(CAT2_HDR);

		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(CAT3_HDR);

		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(IMPACT_CAT_HDR);

		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(IMPACT_CAT_REF_UNIT_HDR);

		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(CHAR_FACTOR_HDR);

		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(FLOW_UNIT_HDR);

		menuItem = new MenuItem(headerMenu, SWT.NORMAL);
//		menuItem.addListener(SWT.Selection, colListener);
		menuItem.setText(IGNORE_HDR);
		// menuItem = new MenuItem(parent, SWT.NORMAL);
		// menuItem.addListener(SWT.Selection, colListener);
		// menuItem.setText("Custom...");
	}

	/**
	 * once the user has selected a column header for change this Listener will
	 * set the column header to the value selected by the user. If the user
	 * selects "Custom...", then a dialog is displayed so the user can enter a
	 * custom value for the column header.
	 * 
	 * @author tec 919-541-1500
	 * 
	 */
	private class ColumnSelectionListener implements Listener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets
		 * .Event)
		 * 
		 * this event handler is called when the user right clicks on the column
		 * to change the column header name.
		 * columnSelected.setText(menuItemText) will set the column header to
		 * the value of the menu item selected. If "Custom..." is selected, then
		 * the user can set a custom name for the column header.
		 */
		@Override
		public void handleEvent(Event event) {
			if ((event.widget instanceof MenuItem) && (columnSelected != null)) {
				String menuItemText = ((MenuItem) event.widget).getText();
				if (menuItemText != null) {
					if (menuItemText.equals("Custom...")) {
						// allow the user to define a custom header name
						InputDialog inputDialog = new InputDialog(getViewSite()
								.getShell(), "Column Name Dialog",
								"Enter a custom column label", "", null);
						inputDialog.open();
						int returnCode = inputDialog.getReturnCode();
						if (returnCode == InputDialog.OK) {
							String val = inputDialog.getValue();
							columnSelected.setText(val);
						}
					} else {
						columnSelected.setText(menuItemText);
					}

				}
				// save the column names to the TableProvider in case the data
				// table needs to be
				// re-displayed
				saveColumnNames();
			}
		}

	}

	/**
	 * this method retrieves the column header text values from the column
	 * components and passes them to the TableProvider so they can be retrieved
	 * when the data table is re-displayed
	 */
	private void saveColumnNames() {
		List<String> columnNames = new ArrayList<String>();
//		TableColumn[] tableColumns = tableViewer.getTable().getColumns();
		TableColumn[] tableColumns = table.getColumns();
		for (TableColumn tableColumn : tableColumns) {
			columnNames.add(tableColumn.getText());
		}
		TableProvider tableProvider = TableKeeper.getTableProvider(key);
		tableProvider.setHeaderNames(columnNames);
	}
}
