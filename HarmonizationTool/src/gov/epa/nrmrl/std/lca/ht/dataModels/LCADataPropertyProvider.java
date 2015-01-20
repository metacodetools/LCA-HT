package gov.epa.nrmrl.std.lca.ht.dataModels;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class LCADataPropertyProvider {
	private String propertyClass; // e.g. "Flowable"
	private String propertyName; // e.g. "Name"
	private Resource rdfClass; // e.g. ECO.Flowable;
	private RDFDatatype rdfDatatype; // e.g. XSDDatatype.XSDfloat
	private boolean isRequired = false; // e.g. true
	private boolean isUnique = true; // e.g. false
	private boolean leftJustified = true; // e.g. true
	private List<QACheck> qaChecks; // A LIST OF WAYS OF CHECKING THIS COLUMN FOR VALIDITY
	private Property tdbProperty;

	// private List<Issue> issues = new ArrayList<Issue>();
	// private List<String> values = new ArrayList<String>();

	public LCADataPropertyProvider() {

	}

	public LCADataPropertyProvider(String propertyName) {
		this.propertyName = propertyName;
	}

	public LCADataPropertyProvider copyLCADataProperty(LCADataPropertyProvider lcaDataProperty) {
		LCADataPropertyProvider result = new LCADataPropertyProvider(lcaDataProperty.getPropertyName());
		// CSVColumnInfo newCSVColumnInfo = new CSVColumnInfo();
		result.propertyName = lcaDataProperty.getPropertyName();
		result.rdfDatatype = lcaDataProperty.getRdfDatatype();

		result.isRequired = lcaDataProperty.isRequired();
		result.isUnique = lcaDataProperty.isUnique();
		result.leftJustified = lcaDataProperty.isLeftJustified();
		// result.tdbProperty = lcaDataProperty.getTDBProperty();
		result.qaChecks = lcaDataProperty.copyCheckLists();
		// BUT DON'T COPY ISSUES
		// this.issues = menuCSVColumnInfo.copyIssues();
		// INITIALIZE INSTEAD
		// result.issues = new ArrayList<Issue>();
		// propertyValueJavaClass = Integer.getClass();
		return result;
	}

	private static boolean compareLCADataPropertyProviders(LCADataPropertyProvider lcaDataProperty1, LCADataPropertyProvider lcaDataProperty2){
		if (lcaDataProperty1 == null){
			return false;
		}
		if (lcaDataProperty2 == null){
			return false;
		}
		if (!lcaDataProperty1.getPropertyClass().equals(lcaDataProperty2.getPropertyClass())){
			return false;
		}
		if (!lcaDataProperty1.getPropertyName().equals(lcaDataProperty2.getPropertyName())){
			return false;
		}
		return true;
	}
	
	public boolean sameAs(LCADataPropertyProvider lcaDataPropertyProvider){
		return compareLCADataPropertyProviders(this, lcaDataPropertyProvider);
	}
//	public String getDataClassName() {
//		if (rdfClass.hasProperty(RDFS.label)) {
//			return rdfClass.getPropertyResourceValue(RDFS.label).asLiteral().getString();
//		}
//		return null;
//	}

	// public LCADataPropertyProvider(LCADataPropertyProvider menuCSVColumnInfo) {
	// // CSVColumnInfo newCSVColumnInfo = new CSVColumnInfo();
	// this.propertyName = menuCSVColumnInfo.getPropertyName();
	// this.isRequired = menuCSVColumnInfo.isRequired();
	// this.isUnique = menuCSVColumnInfo.isUnique();
	// this.leftJustified = menuCSVColumnInfo.isLeftJustified();
	// this.rdfClass = menuCSVColumnInfo.getRDFClass();
	// this.tdbProperty = menuCSVColumnInfo.getTdbProperty();
	// this.rdfDatatype = menuCSVColumnInfo.getRdfDatatype();
	// this.checkLists = menuCSVColumnInfo.copyCheckLists();
	// // BUT DON'T COPY ISSUES
	// // this.issues = menuCSVColumnInfo.copyIssues();
	// // INITIALIZE INSTEAD
	// this.issues = new ArrayList<Issue>();
	// }

	public String getPropertyClass() {
		return propertyClass;
	}

	public void setPropertyClass(String propertyClass) {
		this.propertyClass = propertyClass;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	// public Object getPropertyValueJavaType() {
	// return propertyValueJavaType;
	// }
	//
	// public void setPropertyValueJavaType(Object propertyValueJavaType) {
	// this.propertyValueJavaType = propertyValueJavaType;
	// }

	public Resource getRDFClass() {
		return rdfClass;
	}

	public void setRDFClass(Resource rdfClass) {
		this.rdfClass = rdfClass;
	}

	public RDFDatatype getRdfDatatype() {
		return rdfDatatype;
	}

	public void setRDFDatatype(RDFDatatype rdfDatatype) {
		this.rdfDatatype = rdfDatatype;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public boolean isLeftJustified() {
		return leftJustified;
	}

	public void setLeftJustified(boolean leftJustified) {
		this.leftJustified = leftJustified;
	}

	public List<QACheck> getCheckLists() {
		return qaChecks;
	}

	public void setCheckLists(List<QACheck> checkLists) {
		this.qaChecks = checkLists;
	}

	public List<QACheck> copyCheckLists() {
		List<QACheck> results = new ArrayList<QACheck>();
		for (QACheck qaCheck : qaChecks) {
			QACheck newQACheck = new QACheck(qaCheck);
			results.add(newQACheck);
		}
		return results;
	}

	public void addQACheck(QACheck qaCheck) {
		this.qaChecks.add(qaCheck);
	}

	// public List<Issue> getIssues() {
	// return issues;
	// }
	//
	// public void setIssues(List<Issue> issues) {
	// this.issues = issues;
	// }
	//
	// public void addIssue(Issue issue) {
	// this.issues.add(issue);
	// }
	//
	// public int getIssueCount() {
	// return this.issues.size();
	// }
	//
	// public void clearIssues() {
	// this.issues.clear();
	// }

	public Property getTDBProperty() {
		return tdbProperty;
	}

	public void setTDBProperty(Property tdbProperty) {
		this.tdbProperty = tdbProperty;
	}
}