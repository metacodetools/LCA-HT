package harmonizationtool.query;

import harmonizationtool.model.ITableProvider;

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

public class HSubsSameCas extends HarmonyBaseQuery implements IParamHarmonize {
	private String queryID;
	private String refID;

	{
		label = "Harmonize Subs Same CAS";
	}

	@Override
	public void setQueryDataSet(String queryDataSet) {
		queryID = queryDataSet;
	}

	@Override
	public void setReferenceDataSet(String refDataSet) {
		refID = refDataSet;
	}

	@Override
	public String getQuery() {
		{
			try {
				String subRowPrefix = ITableProvider.getSubRowPrefix();
				String subRowNameHeader = ITableProvider.getsubRowNameHeader();
				StringBuilder b = new StringBuilder();
				b.append("PREFIX  eco:    <http://ontology.earthster.org/eco/core#> \n");
				b.append("PREFIX  ethold: <http://epa.gov/nrmrl/std/lca/ethold#> \n");
				b.append("PREFIX  afn:    <http://jena.hpl.hp.com/ARQ/function#> \n");
				b.append("PREFIX  fn:     <http://www.w3.org/2005/xpath-functions#> \n");
				b.append("PREFIX  owl:    <http://www.w3.org/2002/07/owl#> \n");
				b.append("PREFIX  skos:   <http://www.w3.org/2004/02/skos/core#> \n");
				b.append("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
				b.append("PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n");
				b.append("PREFIX  xml:    <http://www.w3.org/XML/1998/namespace> \n");
				b.append("PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#> \n");
				b.append("PREFIX  dcterms: <http://purl.org/dc/terms/> \n");
				b.append(" \n");
				b.append("SELECT  \n");
				b.append("   (\""+queryID+"\" as ?"+subRowPrefix+"1_"+subRowNameHeader+") \n");
				b.append("   (\""+refID+"\" as ?"+subRowPrefix+"2_"+subRowNameHeader+") \n");
				b.append("   (str(?name1) as ?"+subRowPrefix+"1_substance_name) \n");
				b.append("   (str(?name2) as ?"+subRowPrefix+"2_substance_name) \n");
				b.append("   (str(?cas) as ?same_cas) \n");
				b.append(" \n");
				b.append("WHERE { \n");
				b.append("      ?sub1 eco:hasDataSource ?ds_query . \n");
				b.append("      ?ds_query rdfs:label ?ds_qname . \n");
				b.append("      filter regex(str(?ds_qname),\""+ queryID + "\") \n");

				b.append("      ?sub2 eco:hasDataSource ?ds_ref . \n");
				b.append("      ?ds_ref rdfs:label ?ds_rname . \n");
				b.append("      filter regex(str(?ds_rname),\""+ refID + "\") \n");

				b.append("      ?sub1 eco:casNumber ?cas .  \n");
				b.append("      ?sub2 eco:casNumber ?cas .   \n");
				b.append("      ?sub1 rdfs:label ?name1 . \n");
				b.append("      ?sub2 rdfs:label ?name2 .  \n");
				b.append("} \n");
				b.append("order by ?sub1 ?ds_match \n");
				queryStr = b.toString();
				System.out.println("queryStr = \n" + queryStr);
				return queryStr;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("queryStr = \n" + queryStr);

				queryStr = null;
				return queryStr;
			}

		}
	}
}
