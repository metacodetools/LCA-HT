package harmonizationtool;

/*******************************************************************************
 * Copyright (c) 2006, 2010 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *******************************************************************************/

import harmonizationtool.comands.SelectTDB;
import harmonizationtool.vocabulary.ECO;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Demonstrate how to work-around 3.3.1 limitation when it comes to TAB-Traversal and checkbox
 * editors. 3.4 will hopefully provide provide an out-of-the-box fix see bug 198502
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 * 
 */
public class TreeEditorTest {
	public TreeEditorTest(final Shell shell) {
		final TreeViewer treeViewer = new TreeViewer(shell, SWT.BORDER | SWT.FULL_SELECTION);
		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);

		final TreeViewerFocusCellManager mgr = new TreeViewerFocusCellManager(treeViewer, new FocusCellOwnerDrawHighlighter(treeViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(treeViewer) {
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.keyCode == SWT.CR || event.character == ' '))
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TreeViewerEditor.create(treeViewer, mgr, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

		final TextCellEditor textCellEditor = new TextCellEditor(treeViewer.getTree());
		final CheckboxCellEditor checkboxCellEditor = new CheckboxCellEditor(treeViewer.getTree());

		TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("DataSet");
		column.setLabelProvider(new ColumnLabelProvider() {

			// public String getText(Object treeNode) {
			// return "Col 1: " + treeNode.toString();
			// }

			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).colLabels.get(0);
			}

		});
		column.setEditingSupport(new EditingSupport(treeViewer) {
			protected boolean canEdit(Object treeNode) {
				return false;
			}

			protected CellEditor getCellEditor(Object treeNode) {
				return textCellEditor;
			}

			protected Object getValue(Object treeNode) {
				return ((TreeNode) treeNode).sourceRowNum + "";
			}

			protected void setValue(Object treeNode, Object value) {
				((TreeNode) treeNode).sourceRowNum = Integer.parseInt(value.toString());
				treeViewer.update(treeNode, null);
			}
		});

		column = new TreeViewerColumn(treeViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Substance Name");
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).colLabels.get(1);
			}

		});
		column.setEditingSupport(new EditingSupport(treeViewer) {
			protected boolean canEdit(Object treeNode) {
				return true;
			}

			protected CellEditor getCellEditor(Object treeNode) {
				return textCellEditor;
			}

			protected Object getValue(Object treeNode) {
				return ((TreeNode) treeNode).sourceRowNum + "";
			}

			protected void setValue(Object treeNode, Object value) {
				((TreeNode) treeNode).sourceRowNum = Integer.parseInt(value.toString());
				treeViewer.update(treeNode, null);
			}
		});

		column = new TreeViewerColumn(treeViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("CAS");
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).colLabels.get(2);
			}

		});
		column.setEditingSupport(new EditingSupport(treeViewer) {
			protected boolean canEdit(Object treeNode) {
				return true;
			}

			protected CellEditor getCellEditor(Object treeNode) {
				return checkboxCellEditor;
			}

			protected Object getValue(Object treeNode) {
				return new Boolean(((TreeNode) treeNode).bool);
			}

			protected void setValue(Object treeNode, Object value) {
				((TreeNode) treeNode).bool = ((Boolean) value).booleanValue();
				treeViewer.update(treeNode, null);
			}
		});

		treeViewer.setContentProvider(new MyContentProvider());
		treeViewer.setInput(createModel());
		treeViewer.getControl().addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				if ((e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) && mgr.getFocusCell().getColumnIndex() == 2) {
					ColumnViewerEditor editor = treeViewer.getColumnViewerEditor();
					ViewerCell cell = mgr.getFocusCell();

					try {
						Method m = ColumnViewerEditor.class.getDeclaredMethod("processTraverseEvent", new Class[] { int.class, ViewerRow.class, TraverseEvent.class });
						m.setAccessible(true);
						m.invoke(editor, new Object[] { new Integer(cell.getColumnIndex()), cell.getViewerRow(), e });
					} catch (SecurityException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (NoSuchMethodException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalArgumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

		});
	}

	@SuppressWarnings("unchecked")
	private TreeNode createModel() {
		// List<String> rootRowNames = new ArrayList();
		// rootRowNames.add("Data Set");
		// rootRowNames.add("Substance Name");
		// rootRowNames.add("CAS");

		TreeNode root = new TreeNode(null);
		root.colAssocResource.add((Resource) ECO.DataSource);
//		root.colAssocProperty.add((Resource) ECO.hasDataSource);
//		root.colAssocResource.add((Resource) ECO.Substance);
//		root.colAssocProperty.add((Resource) ECO.hasSubstance);
//		root.colAssocResource.add((Resource) ECO.);
//		root.colAssocProperty.add((Resource) ECO.casNumber);

	    root.sourceRowNum = 0;

		TreeNode row;
		TreeNode subRow;
		for (int i = 1; i < 10; i++) {
			List<String> rowNames = new ArrayList();
			rowNames.add("Master List");
			rowNames.add("Benzene " + i);
			rowNames.add("102-32-" + i);
			row = new TreeNode(root, rowNames);
			row.sourceRowNum = i;
			root.child.add(row);

			subRow = new TreeNode(row);
			subRow.colLabels.add("TRACI");
			subRow.colLabels.add("Benzene x" + i);
			subRow.colLabels.add("102-32-" + i);
			subRow.sourceRowNum = i*10+1;
			row.child.add(subRow);

			subRow = new TreeNode(row);
			subRow.colLabels.add("ReCiPe");
			subRow.colLabels.add("Benzene " + i);
			subRow.colLabels.add("102-32-" + i);
			subRow.sourceRowNum = i*10+2;
			row.child.add(subRow);
		}
		return root;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new TreeEditorTest(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

	private class MyContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((TreeNode) inputElement).child.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object treeNode) {
			if (treeNode == null) {
				return null;
			}
			return ((TreeNode) treeNode).parent;
		}

		public boolean hasChildren(Object treeNode) {
			return ((TreeNode) treeNode).child.size() > 0;
		}

	}

	public class TreeNode {
		public TreeNode parent;

		public ArrayList child = new ArrayList();

		public ArrayList<Resource> colAssocResource = new ArrayList<Resource>();
		public ArrayList<Property> colAssocProperty = new ArrayList<Property>();
//		public ArrayList<Object> colContent = new ArrayList();
		public List<String> colLabels = new ArrayList();

		public int sourceRowNum;

		public boolean bool;

		public TreeNode(TreeNode parent) {
			this.parent = parent;
		}

		public TreeNode(TreeNode parent, List<String> colLabels) {
			this.parent = parent;
			this.colLabels = colLabels;
		}

		public String toString() {
			return colLabels.get(0);
		}

		// DON'T NEED SETTERS AND GETTERS, ALL THIS IS PUBLIC
		// public int getSourceRowNum() {
		// return sourceRowNum;
		// }
		//
		// public void setSourceRowNum(int sourceRowNum) {
		// this.sourceRowNum = sourceRowNum;
		// }
	}

}