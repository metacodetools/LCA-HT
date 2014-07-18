package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;
import harmonizationtool.vocabulary.FEDLCA;
import harmonizationtool.vocabulary.LCAHT;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataSourceProvider {
	private String dataSourceName;
	private String version = "";
	private String comments = "";
	private Person contactPerson = null;
	private List<FileMD> fileMDList = new ArrayList<FileMD>();
	// private List<Annotation> annotationList = new ArrayList<Annotation>();
	private Resource tdbResource;
	private static final Resource rdfClass = ECO.DataSource;

	private boolean isMaster = false;

	public DataSourceProvider() {
		this.tdbResource = ActiveTDB.createResource(rdfClass);
	}

	public DataSourceProvider(Resource tdbResource) {
		if (DataSourceKeeper.getByTdbResource(tdbResource) < 0) {
			this.tdbResource = tdbResource;
			syncFromTDB();
			DataSourceKeeper.add(this);
		}
	}

	public Person getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(Person contactPerson) {
		this.contactPerson = contactPerson;
		if (contactPerson == null) {
			return;
		}
		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		try {
			tdbResource.removeAll(FEDLCA.hasContactPerson);
			tdbResource.addProperty(FEDLCA.hasContactPerson, contactPerson.getTdbResource());
			ActiveTDB.tdbDataset.commit();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public Resource getTdbResource() {

		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		this.tdbResource = tdbResource;
	}

	public void addFileMD(FileMD fileMD) {
		if (fileMD == null) {
			return;
		}

		fileMDList.add(fileMD);

		if (!ActiveTDB.tdbModel.contains(tdbResource, LCAHT.containsFile, fileMD.getTdbResource())) {
			// ActiveTDB.addAnything(tdbResource, LCAHT.containsFile, fileMD.getTdbResource());
			// THE ABOVE SHOULD BE ADDED: TO SAFELY ADD A TRIPLE WHETHER THE OBJECT IS AN RDFNode, A Literal, OR AN
			// OBJECT (TO BE TYPED)

			// --- BEGIN SAFE -WRITE- TRANSACTION ---
			ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
			try {
				tdbResource.addProperty(LCAHT.containsFile, fileMD.getTdbResource());
				ActiveTDB.tdbDataset.commit();
			} finally {
				ActiveTDB.tdbDataset.end();
			}
			// ---- END SAFE -WRITE- TRANSACTION ---
		}
	}

	public List<FileMD> getFileMDList() {
		return fileMDList;
	}

	public List<FileMD> getFileMDListNewestFirst() {
		List<FileMD> results = new ArrayList<FileMD>();
		for (FileMD fileMD : fileMDList) {
			Date readDate = fileMD.getReadDate();
			int index = results.size();
			for (FileMD newFileMD : results) {
				Date newReadDate = newFileMD.getReadDate();
				if (readDate.after(newReadDate)) {
					index = results.indexOf(newFileMD);
					break;
				}
			}
			results.add(index, fileMD);
		}
		return results;
	}

	public List<FileMD> getFileMDListOldestFirst() {
		List<FileMD> results = new ArrayList<FileMD>();
		for (FileMD fileMD : fileMDList) {
			Date readDate = fileMD.getReadDate();
			int index = results.size();
			for (FileMD newFileMD : results) {
				Date newReadDate = newFileMD.getReadDate();
				if (readDate.before(newReadDate)) {
					index = results.indexOf(newFileMD);
					break;
				}
			}
			results.add(index, fileMD);
		}
		return results;
	}

	public void remove(FileMD fileMD) {
		// fileMD.remove(); -- BETTER NOT REMOVE THIS IN CASE SOME OTHER DATASOURCE HAS THIS FILE
		fileMDList.remove(fileMD);
		ActiveTDB.removeStatement(tdbResource, LCAHT.containsFile, fileMD.getTdbResource());
	}

	public void removeFileMDList() {
		// for (FileMD fileMD : fileMDList) { -- BETTER NOT REMOVE THIS IN CASE SOME OTHER DATASOURCE HAS THIS FILE
		// fileMD.remove();
		// }
		fileMDList = null;
	}

	public void remove() {
		removeFileMDList();
		ActiveTDB.removeAllObjects(tdbResource, RDF.type);
		ActiveTDB.removeAllObjects(tdbResource, RDFS.label);
		ActiveTDB.removeAllObjects(tdbResource, DCTerms.hasVersion);
		ActiveTDB.removeAllObjects(tdbResource, RDFS.comment);
		ActiveTDB.removeAllObjects(tdbResource, FEDLCA.hasContactPerson);
		ActiveTDB.removeAllObjects(tdbResource, LCAHT.containsFile);
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
		ActiveTDB.replaceLiteral(tdbResource, RDFS.label, dataSourceName);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
		ActiveTDB.replaceLiteral(tdbResource, DCTerms.hasVersion, version);
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
		ActiveTDB.replaceLiteral(tdbResource, RDFS.comment, comments);
	}

	public void syncFromTDB() {
		RDFNode rdfNode;
		rdfNode = tdbResource.getProperty(RDFS.label).getObject();

		if (rdfNode == null) {
			dataSourceName = DataSourceKeeper.uniquify("unkownName");
		} else {
			dataSourceName = ActiveTDB.getStringFromLiteral(rdfNode);
		}

		rdfNode = tdbResource.getProperty(DCTerms.hasVersion).getObject();
		if (rdfNode == null) {
			version = "";
		} else {
			version = ActiveTDB.getStringFromLiteral(rdfNode);
		}

		rdfNode = tdbResource.getProperty(RDFS.comment).getObject();
		if (rdfNode == null) {
			comments = "";
		} else {
			comments = ActiveTDB.getStringFromLiteral(rdfNode);
		}

		rdfNode = tdbResource.getProperty(FEDLCA.hasContactPerson).getObject();
		if (rdfNode == null) {
			contactPerson = null;
		} else {
			contactPerson = new Person(rdfNode.asResource());
		}

		StmtIterator stmtIterator = tdbResource.listProperties(LCAHT.containsFile);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			rdfNode = statement.getObject();
			int fileMDIndex = FileMDKeeper.getIndexByTdbResource(rdfNode.asResource());
			System.out.println("file Index: " + fileMDIndex);
			FileMD fileMD;
			if (fileMDIndex > -1) {
				fileMD = FileMDKeeper.get(fileMDIndex);
			} else {
				fileMD = new FileMD(rdfNode.asResource());
			}
			addFileMD(fileMD);
		}
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}
}
