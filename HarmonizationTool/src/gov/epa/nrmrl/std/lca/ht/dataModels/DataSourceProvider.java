package gov.epa.nrmrl.std.lca.ht.dataModels;

import gov.epa.nrmrl.std.lca.ht.sparql.HarmonyQuery2Impl;
import gov.epa.nrmrl.std.lca.ht.sparql.Prefixes;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.vocabulary.ECO;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.LCAHT;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataSourceProvider {
	private static final Resource rdfClass = ECO.DataSource;

	private String dataSourceName;
	private String version = "";
	private String comments = "";
	private Person contactPerson = null;
	private List<FileMD> fileMDList = new ArrayList<FileMD>();
	private List<FileMD> newFileMDList = new ArrayList<FileMD>();
	private Resource tdbResource;
	private Integer referenceDataStatus = null;

	public Exception creation = new Exception();

	public DataSourceProvider() {
		this(true);
	}

	public DataSourceProvider(boolean createTDBResource) {
		boolean success = DataSourceKeeper.add(this);
		DataSourceKeeper.add(this);
		if (createTDBResource)
			this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);

	}

	public DataSourceProvider(Resource tdbResource) {
		if (DataSourceKeeper.getByTdbResource(tdbResource) < 0) {
			if (tdbResource == null) {
				this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
			} else {
				this.tdbResource = tdbResource;
			}
			boolean synced = syncFromTDB(null);
			if (!synced) {
				synced = syncFromTDB(ActiveTDB.importGraphName);
			}
			DataSourceKeeper.add(this);
		}
	}

	public void createTDBResource() {
		if (tdbResource != null)
			return;
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		if (dataSourceName != null)
			setDataSourceName(dataSourceName);
		if (version != null)
			setVersion(version);
		if (comments != null)
			setComments(comments);
		if (contactPerson != null)
			setContactPerson(contactPerson);
		if (referenceDataStatus != null)
			setReferenceDataStatus(referenceDataStatus);
		for (FileMD fileMD : newFileMDList) {
			fileMD.createTDBResource();
			addFileMD(fileMD);
		}
		newFileMDList.clear();
		// private List<AnnotationProvider> annotationList = new ArrayList<AnnotationProvider>();

	}

	public Person getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(Person contactPerson) {
		this.contactPerson = contactPerson;
		if (contactPerson == null) {
			return;
		}
		if (tdbResource != null)
			ActiveTDB.tsReplaceObject(tdbResource, FedLCA.hasContactPerson, contactPerson.getTdbResource());
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
		// Model tdbModel = ActiveTDB.getModel();
		// if (!tdbModel.contains(tdbResource, LCAHT.containsFile, fileMD.getTdbResource())) {
		if (tdbResource != null) {
			if (fileMD.getTdbResource() == null)
				fileMD.createTDBResource();
			ActiveTDB.tsAddGeneralTriple(tdbResource, LCAHT.containsFile, fileMD.getTdbResource(), null);
		} else
			newFileMDList.add(fileMD);
		// }
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
		// fileMD.remove(); -- BETTER NOT REMOVE THIS IN CASE SOME OTHER
		// DATASOURCE HAS THIS FILE
		fileMDList.remove(fileMD);
		if (tdbResource != null)
			ActiveTDB.tsRemoveStatement(tdbResource, LCAHT.containsFile, fileMD.getTdbResource());
	}

	public boolean containsOLCAData() {
		StringBuilder b = new StringBuilder();
		b.append(Prefixes.getPrefixesForQuery());
		b.append("select distinct ?ds \n");
		b.append("where {  \n");
		b.append("  ?ds rdfs:label \"" + getDataSourceName() + "\"^^xsd:string .  \n");
		b.append("  ?f a olca:Flow .  \n");
		b.append("  ?f eco:hasDataSource ?ds .  \n");
		b.append("  ?f olca:category ?fc .  \n");
		b.append("  ?fc a olca:Category .  \n");
		b.append("  ?fc eco:hasDataSource ?ds .  \n");
		b.append("  ?f olca:flowProperties ?fpf .  \n");
		b.append("  ?fpf a olca:FlowPropertyFactor .  \n");
		b.append("  ?fpf eco:hasDataSource ?ds .  \n");
		b.append("  ?ds a eco:DataSource .  \n");
		b.append("} \n");
		b.append("limit 1 \n");

		String query = b.toString();

//		System.out.println("query = " + query);

		HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		harmonyQuery2Impl.setQuery(query);

		ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		if (resultSet.hasNext()) {
			return true;
		}
		return false;
	}

	public void removeFileMDList() {
		// for (FileMD fileMD : fileMDList) { -- BETTER NOT REMOVE THIS IN CASE
		// SOME OTHER DATASOURCE HAS THIS FILE
		// fileMD.remove();
		// }
		fileMDList = null;
	}

	public void remove() {
		String noString = null;
		remove(noString);
	}

	/**
	 * This method is intended to remove a DataSourceProvider including
	 * 1) The DataSourceProvider itself
	 * 2) The TDB objects associated with the ECO.DataSource
	 * 3) Each TDB subject having a ECO.hasDataSource of that ECO.DataSource
	 * 4) Each triple beginning with the subject mentioned in 3)
	 * 
	 * Note: It should be adjusted to consider other triples
	 * 
	 * @param graph String representing the graph from which the ECO.DataSource should be removed
	 */
	public void remove(String graph) {
		removeFileMDList();
		if (tdbResource == null)
			return;
		List<Statement> removeStatements = new ArrayList<Statement>();

		// --- BEGIN SAFE -READ- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(graph);
		Selector selector0 = new SimpleSelector(tdbResource, null, null, null);
		StmtIterator stmtIterator0 = tdbModel.listStatements(selector0);
		while (stmtIterator0.hasNext()) {
			removeStatements.add(stmtIterator0.next());
		}
		Selector selector1 = new SimpleSelector(null, ECO.hasDataSource, tdbResource.asNode());
		StmtIterator stmtIterator1 = tdbModel.listStatements(selector1);
		while (stmtIterator1.hasNext()) {
			Statement statement = stmtIterator1.next();
			removeStatements.add(statement);
			Selector selector2 = new SimpleSelector(statement.getSubject(), null, null, null);
			StmtIterator stmtIterator2 = tdbModel.listStatements(selector2);
			while (stmtIterator2.hasNext()) {
				removeStatements.add(stmtIterator2.next());
			}
		}
		ActiveTDB.tdbDataset.end();
		// --- END SAFE -READ- TRANSACTION ---

		// --- BEGIN SAFE -WRITE- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		tdbModel = ActiveTDB.getModel(graph);
		try {
			for (Statement statement : removeStatements) {
				tdbModel.remove(statement);
			}
			ActiveTDB.tdbDataset.commit();
		} catch (Exception e) {
			ActiveTDB.tdbDataset.abort();
		} finally {
			ActiveTDB.tdbDataset.end();
		}
		// ---- END SAFE -WRITE- TRANSACTION ---
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public String getDataSourceNameString() {
		if (dataSourceName == null) {
			return "<NO ASSIGNED NAME>";
		}
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
		if (tdbResource != null)
			ActiveTDB.tsReplaceLiteral(tdbResource, RDFS.label, dataSourceName);
	}

	public String getVersion() {
		return version;
	}

	public String getVersionString() {
		if (version == null) {
			return "";
		}
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
		if (tdbResource != null)
			ActiveTDB.tsReplaceLiteral(tdbResource, DCTerms.hasVersion, version);
	}

	public String getComments() {
		return comments;
	}

	public String getCommentsString() {
		if (comments == null) {
			return "";
		}
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
		if (tdbResource != null)
			ActiveTDB.tsReplaceLiteral(tdbResource, RDFS.comment, comments);
	}

	/**
	 * Items that need to be synced to the Java object from the TDB (using Resource tdbResource) include
	 * String dataSourceName <= RDFS.label
	 * String version <= DCTerms.hasVersion
	 * String comments <= RDFS.comment
	 * Person contactPerson <= FedLCA.hasContactPerson
	 * List<FileMD> fileMDList <= LCAHT.containsFile
	 * Integer referenceDataStatus <= [if belongs to class LCAHT.MasterDataset or LCAHT.SupplementaryReferenceDataset]

	 * @param String indicating which graph is to be used to sync from
	 * @return True if succeeded, False if failed.
	 */
	public boolean syncFromTDB(String graphName) {
		if (tdbResource == null) {
			return false;
		}
		Resource personResource = null;
		List<Resource> fileMDResources = new ArrayList<Resource>();

		// --- BEGIN SAFE -READ- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		Model tdbModel = ActiveTDB.getModel(graphName);
		if (!tdbModel.containsResource(tdbResource)) {
			return false;
		}
		Selector selector = new SimpleSelector(tdbResource, null, null, null);
		StmtIterator stmtIterator = tdbModel.listStatements(selector);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			Property property = statement.getPredicate();
			if (property.equals(RDFS.label)) {
				dataSourceName = statement.getObject().asLiteral().getString();
			} else if (property.equals(RDFS.comment)) {
				comments = statement.getObject().asLiteral().getString();
			} else if (property.equals(DCTerms.hasVersion)) {
				version = statement.getObject().asLiteral().getString();
			} else if (property.equals(FedLCA.hasContactPerson)) {
				personResource = statement.getObject().asResource();
			} else if (property.equals(LCAHT.containsFile)) {
				fileMDResources.add(statement.getObject().asResource());
			} else if (property.equals(RDF.type)) {
				Resource type = statement.getObject().asResource();
				if (type.equals(LCAHT.MasterDataset) || type.equals(LCAHT.SupplementaryReferenceDataset)) {
					referenceDataStatus = 1;
				}
			}
		}
		ActiveTDB.tdbDataset.end();

		// ---- END SAFE -READ- TRANSACTION ---

		// FIXME - TONY HOWARD - PUT A BREAK POINT JUST BELOW HERE, THEN RUN THE HT
		contactPerson = new Person(personResource);
		for (Resource fileMDResource : fileMDResources) {
			FileMD fileMD = new FileMD();
			fileMD.setTdbResource(fileMDResource);
			fileMD.syncDataFromTDB();
			FileMDKeeper.add(fileMD);
			fileMDList.add(fileMD);
		}
		return true;
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}

	public Integer getReferenceDataStatus() {
		return referenceDataStatus;
	}

	public void setReferenceDataStatus(Integer referenceDataStatus) {
		this.referenceDataStatus = referenceDataStatus;
		if (tdbResource == null)
			return;
		if (referenceDataStatus == null) {
			ActiveTDB.tsRemoveStatement(tdbResource, RDF.type, LCAHT.MasterDataset);
			ActiveTDB.tsRemoveStatement(tdbResource, RDF.type, LCAHT.SupplementaryReferenceDataset);
			return;
		}
		if (referenceDataStatus == 1) {
			ActiveTDB.tsRemoveStatement(tdbResource, RDF.type, LCAHT.SupplementaryReferenceDataset);
			ActiveTDB.tsAddGeneralTriple(tdbResource, RDF.type, LCAHT.MasterDataset, null);
			return;
		}
		if (referenceDataStatus == 1) {
			ActiveTDB.tsRemoveStatement(tdbResource, RDF.type, LCAHT.MasterDataset);
			ActiveTDB.tsAddGeneralTriple(tdbResource, RDF.type, LCAHT.SupplementaryReferenceDataset, null);
		}
	}
}
