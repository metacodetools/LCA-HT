package gov.epa.nrmrl.std.lca.ht.utils;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class ResourceIdMgr {
	private static List<Resource> idList = new ArrayList<Resource>();

	
	
	private ResourceIdMgr() {
	}

	
	
	public static int add(Resource resource) {
		if (!idList.contains(resource)) {
			idList.add(resource);
		}
		int index = idList.indexOf(resource);
		return index;
	}

	
	
	public static int getId(Resource resource) {
		return add(resource);
	}

	
	
	public static Resource getResource(int index) {
		return idList.get(index);
	}

	
	
	public static boolean contains(int index) {
		try {
			Resource resource = idList.get(index);
			if (resource == null) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
