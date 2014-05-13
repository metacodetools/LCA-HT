package harmonizationtool.query.unused;

import java.util.Map;

import gov.epa.nrmrl.std.lca.ht.views.QueryView;
import harmonizationtool.comands.SelectTDB;
import harmonizationtool.handler.ImportTDBHandler;
import harmonizationtool.model.DataRow;
import harmonizationtool.model.ModelProvider;
import harmonizationtool.utils.Util;

//import com.hp.hpl.jena.query.Dataset;
//import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
//import com.hp.hpl.jena.update.GraphStore;
//import com.hp.hpl.jena.update.UpdateExecutionFactory;
//import com.hp.hpl.jena.update.UpdateFactory;
//import com.hp.hpl.jena.update.UpdateProcessor;
//import com.hp.hpl.jena.update.UpdateRequest;

//public class xNumberDataSets extends HarmonyBaseInsert implements IIntQuery {
public class UDelDataSet2 {

	public void execute() {
		// Query query = QueryFactory.create(queryStr);
		Model model = SelectTDB.model;
		if (model == null) {
			String msg = "ERROR no TDB open";
			Util.findView(QueryView.ID).getViewSite().getActionBars()
					.getStatusLineManager().setMessage(msg);
			return;
		}
		System.out.println("Running XNumberDataSets");
		// Dataset dataset = SelectTDB.dataset;
		// GraphStore graphStore = SelectTDB.graphStore;
		// DataRow columnHeaders = new DataRow();
		// queryResults.setColumnHeaders(columnHeaders);

		long change = model.size();

		// columnHeaders.add("Model");
		// columnHeaders.add("Size");

		System.err.printf("Before Update: %s\n", model.size());
		// data.add("Before Update");
		// data.add(""+model.size());

		ModelProvider modelProvider = new ModelProvider();
		// queryResults.setModelProvider(modelProvider);
		DataRow dataRow = new DataRow();
		modelProvider.addDataRow(dataRow);
		dataRow.add("Before Update");
		dataRow.add("" + model.size());

		String eco_p = "http://ontology.earthster.org/eco/core#";
		String ethold_p = "http://epa.gov/nrmrl/std/lca/ethold#";
		String afn_p = "http://jena.hpl.hp.com/ARQ/function#";
		String fn_p = "http://www.w3.org/2005/xpath-functions#";
		String owl_p = "http://www.w3.org/2002/07/owl#";
		String skos_p = "http://www.w3.org/2004/02/skos/core#";
		String sumo_p = "http://www.ontologyportal.org/SUMO.owl.rdf#";
		String rdf_p = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		String rdfs_p = "http://www.w3.org/2000/01/rdf-schema#";
		String xml_p = "http://www.w3.org/XML/1998/namespace";
		String xsd_p = "http://www.w3.org/2001/XMLSchema#";

//		Resource ds = model.getResource(eco_p + "DataSource");
//		Property a = model.getProperty(rdf_p + "type");
		Property lid = model.getProperty(ethold_p + "localSerialNumber");
		Property hasDataSource = model.getProperty(eco_p+"hasDataSource");


		Literal lid_2del = model.createTypedLiteral(12);

		ResIterator subsOfDS = model.listSubjectsWithProperty(hasDataSource, lid_2del);
		while (subsOfDS.hasNext()) {
			Resource sub = subsOfDS.nextResource();
		}


		// model.getResource(arg0);
		long startTime = System.currentTimeMillis();
		float elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000F;
		System.out.println("Time elapsed: " + elapsedTimeSec);
		System.err.printf("After Update: %s\n", model.size());
		System.out.println("done");
	}

}
