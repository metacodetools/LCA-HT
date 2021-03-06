package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class FileMDKeeper {
	private static List<FileMD> fileMDList = new ArrayList<FileMD>();

	private FileMDKeeper() {
	}

	public static boolean add(FileMD fileMD) {
		if (fileMDList.contains(fileMD)){
			return false;
		}
		return fileMDList.add(fileMD);
	}

	public static boolean remove(FileMD fileMD) {
		fileMD.remove();
		return fileMDList.remove(fileMD);
	}

	public static List<Integer> getIDs() {
		List<Integer> ids = new ArrayList<Integer>();
		Iterator<FileMD> iterator = fileMDList.iterator();
		while (iterator.hasNext()) {
			ids.add(fileMDList.indexOf(iterator.next()));
		}
		return ids;
	}

	public static List<String> getNames() {
		List<String> results = new ArrayList<String>();
		Iterator<FileMD> iterator = fileMDList.iterator();
		while (iterator.hasNext()) {
			results.add(iterator.next().getFilename());
		}
		Collections.sort(results);
		return results;
	}

	public static FileMD get(int index) {
		if (index < 0) {
			return null;
		}
		if (index >= fileMDList.size()) {
			return null;
		}
		return fileMDList.get(index);
	}

	public static boolean hasIndex(int index) {
		try {
			fileMDList.get(index);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Integer indexOf(FileMD fileMD) {
		return fileMDList.indexOf(fileMD);
	}

	public static int indexOfDataSourceName(String name) {
		Iterator<FileMD> iterator = fileMDList.iterator();
		while (iterator.hasNext()) {
			FileMD fileMD = (FileMD) iterator.next();
			if (fileMD.getFilename().equals(name)) {
				return FileMDKeeper.indexOf(fileMD);
			}
		}
		return -1;
	}

	public static int size() {
		return fileMDList.size();
	}

	public static FileMD getFileMDByTdbResource(Resource tdbResource) {
		Iterator<FileMD> iterator = fileMDList.iterator();
		while (iterator.hasNext()) {
			FileMD fileMD = iterator.next();
			Resource resource = fileMD.getTdbResource();
			if (resource.equals(tdbResource)) {
				return fileMD;
			}
		}
		return null;
	}
	
	
	public static int getIndexByTdbResource(Resource tdbResource) {
		Iterator<FileMD> iterator = fileMDList.iterator();
		while (iterator.hasNext()) {
			FileMD fileMD = iterator.next();
			Resource resource = fileMD.getTdbResource();
			if (resource.equals(tdbResource)) {
				return fileMDList.indexOf(fileMD);
			}
		}
		FileMD fileMD = new FileMD(tdbResource);
		add(fileMD);
		iterator = fileMDList.iterator();
		while (iterator.hasNext()) {
			FileMD fileMDinKeeper = iterator.next();
			Resource resource = fileMD.getTdbResource();
			if (resource.equals(tdbResource)) {
				return fileMDList.indexOf(fileMD);
			}
		}
		return -1;
	}


	public static FileMD getByName(String name) {
		for (FileMD fileMD : fileMDList) {
			if (fileMD.getFilename().equals(name)) {
				return fileMD;
			}
		}
		return null;
	}

	public static List<FileMD> getFileMDList() {
		return fileMDList;
	}

	public static void setFileMDList(List<FileMD> fileMDList) {
		FileMDKeeper.fileMDList = fileMDList;
	}
//	public static void syncFromTDB() {
//		Model tdbModel = ActiveTDB.getModel(null);
//		ResIterator iterator = tdbModel.listSubjectsWithProperty(RDF.type, LCAHT.DataFile);
//		while (iterator.hasNext()) {
//			Resource fileMDRDFResource = iterator.next();
//			// NOW SEE IF THE FileMD IS IN THE FileMDKeeper YET
//			int fileMDIndex = getIndexByTdbResource(fileMDRDFResource);
//			System.out.println("another file found in TDB");
//			if (fileMDIndex < 0) {
//				System.out.println("... new one");
//				new FileMD(fileMDRDFResource);
//			}
//		}
//	}
}
