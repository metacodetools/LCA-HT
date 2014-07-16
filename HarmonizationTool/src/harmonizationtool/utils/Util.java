package harmonizationtool.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.Activator;
import harmonizationtool.vocabulary.ECO;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class Util {
	private Util() {
	}

	public static String getGMTDateFmt(Date date) {
		if (date == null){
			return null;
		}
		// SimpleDateFormat dateFormatGmt = new
		// SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormatGmt.format(date);
	}

	public static String getLocalDateFmt(Date date) {
		if (date == null){
			return null;
		}
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatLocal.setTimeZone(TimeZone.getDefault());
		return dateFormatLocal.format(date);
	}

	public static Date setDateFmt(String string) throws ParseException {
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatLocal.setTimeZone(TimeZone.getDefault());
		return dateFormatLocal.parse(string);
	}

	public static String splitCamelCase(String s) {
		String pattern = String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])");
		String result = s.replaceAll(pattern, " ");
		String s1 = result.substring(0, 1);
		String s2 = result.substring(1);
		return s1.toUpperCase() + s2;
	}

	public static String escape(String s) {
		char[] chars = s.toCharArray();
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '\\') {
				b.append("\\");
			}
			if (chars[i] == '"') {
				b.append("\\");
			}
			// if (chars[i] == ',') {
			// b.append("\\");
			// }
			// if (chars[i] == '\'') {
			// b.append("\\");
			// }
			// if (chars[i] == '`') {
			// b.append("\\");
			// }
			b.append(chars[i]);
		}
		return b.toString();
	}

	public static IViewPart findView(String viewID) {
		// FIRST TRY TO FIND IT
		// IF YOU CAN'T, THEN SHOW IT AND FIND IT
		IViewPart view;
		try {
			view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewID);
		} catch (Exception e) {
			try {
				Util.showView(viewID);
				view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewID);
			} catch (PartInitException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				view = null;
			}
		}
		return view;
	}

	public static void showView(String viewID) throws PartInitException {
		System.out.println("viewID = " + viewID);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID);
	}

	public static IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public static Resource resolveUriFromString(String uriString) {
		Resource uri = ActiveTDB.tdbModel.createResource();
		if (uriString.startsWith("http:") || uriString.startsWith("file:")) {
			uri = ActiveTDB.tdbModel.getResource(uriString);
		} else {
			ResIterator iterator = (ActiveTDB.tdbModel.listSubjectsWithProperty(RDF.type, ECO.Substance));
			while (iterator.hasNext()) {
				Resource resource = iterator.next();
				if (resource.isAnon()) {
					AnonId anonId = (AnonId) resource.getId();
					if (uriString.equals(anonId.toString())) {
						uri = resource;
					}
				}
			}
		}
		return uri;
	}
	// public static IStatusLineManager getStatusLine(){
	// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().
	// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getV
	// getViewSite().getActionBars().getStatusLineManager();
	// }
}
