package gov.epa.nrmrl.std.lca.ht.output;

import gov.epa.nrmrl.std.lca.ht.csvFiles.CSVTableView;
import gov.epa.nrmrl.std.lca.ht.dialog.ChooseDataSetDialog;
import gov.epa.nrmrl.std.lca.ht.flowContext.mgr.MatchContexts;
import gov.epa.nrmrl.std.lca.ht.flowProperty.mgr.MatchProperties;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.Util;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class SaveHarmonizedDataForOLCAJsonld implements IHandler {
	public static final String ID = "gov.epa.nrmrl.std.lca.ht.output.SaveHarmonizedDataForOLCAJsonld";

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		Shell shell = HandlerUtil.getActiveShell(event);
		ChooseDataSetDialog dlg = new ChooseDataSetDialog(shell);
		dlg.open();
		String currentName = dlg.getSelection();

		Util.findView(MatchContexts.ID);
		Util.findView(MatchProperties.ID);

		Logger runLogger = Logger.getLogger("run");

		System.out.println("Saving Harmonized Data to .jsonld file");

		String saveTo = event.getParameter("LCA-HT.outputFilename");

		if (saveTo == null) {
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			FileDialog dialog = new FileDialog(shell, SWT.SAVE);
			String[] filterNames = new String[] { "Json Files", "Jsonld Files", "Turtle Files" };
			String[] filterExtensions = new String[] { "*.json", "*.jsonld", "*.ttl" };

			String outputDirectory = Util.getPreferenceStore().getString("outputDirectory");
			if (outputDirectory.startsWith("(same as") || outputDirectory.length() == 0) {
				outputDirectory = Util.getPreferenceStore().getString("workingDirectory");
			}
			if (outputDirectory.length() > 0) {
				dialog.setFilterPath(outputDirectory);
			} else {
				String homeDir = System.getProperty("user.home");
				dialog.setFilterPath(homeDir);
			}

			dialog.setFilterNames(filterNames);
			dialog.setFilterExtensions(filterExtensions);
			Util.findView(CSVTableView.ID);
			dialog.setFileName(currentName + "_harmonized");

			saveTo = dialog.open();
		}
		System.out.println("Save to: " + saveTo);
		if (saveTo == null) {
			// "dialog was cancelled or an error occurred"
			return null;
		}

		runLogger.info("  # Writing RDF triples to " + saveTo.toString());
		// ActiveTDB.copyDatasetContentsToExportGraph(currentName);
		// List<Statement> statements = ActiveTDB.collectAllStatementsForDataset(currentName, null);
		Set<Resource> datasetMembers = ActiveTDB.getDatasetMemberSubjects(currentName, null);

		/*
		 * TODO FIXME TODO FIXME TODO ALERT! DANGER! SEE HERE!
		 * ATTENTION TONY HOWARD : 
		 * We need to make a zip file with each of the following folders.
		 * Then, I'll work some magic to create <uuid.json> "files" to put in each folder one by one
		 * Then we need to write the zip file.
		 * So: do some magic near the beginning to set up the streams or whatever they're called.  I guess I'd
		 * be specifying "paths" for each file?!?
		 * Then, at the end, set up something to output (or zip then output).
		 * 
		 * Thanks!
		 * 
		 */
		Set<Resource> actorResources = new HashSet<Resource>();
		Set<Resource> categoryResources = new HashSet<Resource>();
		Set<Resource> flowPropertyResources = new HashSet<Resource>();
		Set<Resource> flowResources = new HashSet<Resource>();
		Set<Resource> lciaCategoryResources = new HashSet<Resource>();
		Set<Resource> lciaMethodResources = new HashSet<Resource>();
		Set<Resource> locationResources = new HashSet<Resource>();
		Set<Resource> processResources = new HashSet<Resource>();
		Set<Resource> sourceResources = new HashSet<Resource>();
		Set<Resource> unitGroupResources = new HashSet<Resource>();
		Set<Resource> unMatchedResource = new HashSet<Resource>();

		// First, sort members into batches
		for (Resource itemResource : datasetMembers) {
			Set<RDFNode> singleSet = new HashSet<RDFNode>();
			singleSet.add(itemResource);
			List<Statement> statements = ActiveTDB.collectStatementsTraversingNodeSet(singleSet, null);
			ActiveTDB.clearExportGraphContents();
			ActiveTDB.copyStatementsToGraph(statements, ActiveTDB.exportGraphName);
			if (itemResource.hasProperty(RDF.type, OpenLCA.Actor)) {
				actorResources.add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.Category)) {
				categoryResources.add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.FlowProperty)) {
				flowPropertyResources.add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.Flow)) {
				flowResources.add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.ImpactCategory)) {
				lciaCategoryResources.add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.ImpactMethod)) {
				lciaMethodResources.add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.Location)) {
				locationResources.add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.Process)) {
				processResources.add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.Source)) {
				sourceResources.add(itemResource);
			} else if (itemResource.hasProperty(RDF.type, OpenLCA.UnitGroup)) {
				unitGroupResources.add(itemResource);
			} else {
				unMatchedResource.add(itemResource);
			}
		}

		/*
		 * =================================================================================
		 * ============================= OLD CODE BELOW =====================================
		 * =================================================================================
		 */
		// /**
		// * Once data are copied into the export graph, data can be prepared for openLCA
		// * 1) Select Flows using Comparisons
		// * 2) Create new UUIDs for those
		// * 3) Move old info to an appropriate field name
		// * 4) Place new info in the appropriate place
		// * 5) Append to description info about what happened
		// */
		//
		// // if (false){
		// Date modifiedDate = AnnotationProvider.getCurrentAnnotation().getModifiedDate();
		// String modString = Temporal.getLocalDateFmt(modifiedDate);
		// /* TODO - WORK HERE to collect the ComparisonProviders associated with the dataset, then go through each
		// * and make appropriate changes to the export dataset
		// */
		//
		// // List<Statement> statementsToFix = new ArrayList<Statement>();
		// ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		// Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// Selector selector0 = new SimpleSelector(null, FedLCA.comparedSource, null, null);
		// StmtIterator stmtIterator0 = tdbModel.listStatements(selector0);
		// while (stmtIterator0.hasNext()) {
		// Statement statement = stmtIterator0.next();
		// statementsToFix.add(statement);
		// }
		// ActiveTDB.tdbDataset.end();
		//
		// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		// tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// for (Statement statement : statementsToFix) {
		// String unTypedDateTime = statement.getObject().toString();
		// Literal typedDateTime = tdbModel.createTypedLiteral(unTypedDateTime, XSDDatatype.XSDdateTime);
		// tdbModel.add(statement.getSubject(), OpenLCA.lastChange, typedDateTime);
		// tdbModel.remove(statement);
		// // System.out.println("statement = "+statement);
		// }
		// // nothing
		// // RDFNode modNode = CurationMethods.getCurrentAnnotation().getProperty(DCTerms.modified).getObject();
		// // Literal modLiteral = modNode.asLiteral();
		// // Calendar cal = ((XSDDateTime) literalDate).;
		// // Object thing = literalDate.getValue();
		// // System.out.println("it is a :"+modString.getClass());
		// // Date annotatationDate = new Date(CurationMethods.getCurrentAnnotation().getProperty(DCTerms.modified)
		// // .getObject().asLiteral().getLong());
		// // String dateString = Util.getLocalDateFmt(annotatationDate);
		// try {
		//
		// StringBuilder b = new StringBuilder();
		// b.append(Prefixes.getPrefixesForQuery());
		// b.append("  delete {graph <" + ActiveTDB.exportGraphName + ">{  \n");
		// b.append("    ?of olca:description ?oDescription ; \n");
		// b.append("        olca:lastChange ?oLastChange ; \n");
		// b.append("        olca:cas ?oCas ; \n");
		// b.append("        olca:name ?oName ; \n");
		// b.append("        fedlca:hasOpenLCAUUID ?oUUID . \n");
		// b.append("  }} \n");
		// b.append("   \n");
		// b.append("  insert {graph <" + ActiveTDB.exportGraphName + ">{  \n");
		// b.append("    ?of olca:description ?newDescription ; \n");
		// b.append("        olca:lastChange ?newLastChange ; \n");
		// b.append("        olca:cas ?newCas ; \n");
		// b.append("        olca:name ?mName ; \n");
		// b.append("        fedlca:hasOpenLCAUUID ?newUUID . \n");
		// b.append("  }} \n");
		// b.append("   \n");
		// b.append("  where { \n");
		// b.append("    #-- olca:Flow \n");
		// b.append("    ?of a olca:Flow . \n");
		// b.append("    ?of fedlca:hasOpenLCAUUID ?oUUID . \n");
		// b.append("    ?pf a fedlca:Flow . \n");
		// b.append("   \n");
		// b.append("    #-- fedlca:Flow (parsed) \n");
		// b.append("    ?pf fedlca:hasOpenLCAUUID ?oUUID . \n");
		// b.append("    ?pf fedlca:sourceTableRowNumber ?row . \n");
		// b.append("   \n");
		// b.append("    ?pf owl:sameAs ?mf . \n");
		// // b.append("    #-- fedlca:Comparison (created) \n");
		// // b.append("    ?comp fedlca:comparedSource ?pf . \n");
		// // b.append("    ?comp fedlca:comparedMaster ?mf . \n");
		// // b.append("    ?comp fedlca:comparedEquivalence fedlca:Equivalent . \n");
		// // // TODO: Handle cases other than "Equivalent"
		// b.append("   \n");
		// b.append("    #-- fedlca:Flow (master) \n");
		// b.append("    ?mf a fedlca:Flow . \n");
		// b.append("    ?mf eco:hasFlowable ?mflowable . \n");
		// b.append("    ?mds a lcaht:MasterDataset . \n");
		// b.append("    ?mf eco:hasDataSource ?mds . \n");
		// b.append("   \n");
		// b.append("    #-- olca:name == rdfs:label -- 1 CONDITION NEEDING ACTION \n");
		// b.append("    ?of olca:name ?oName . \n");
		// b.append("    ?mflowable rdfs:label ?mName . \n");
		// b.append("    bind (IF ((str(?oName) != str(?mName) ) , concat(\"; name: original = \", ?oName) , \"\") as ?cName) \n");
		// b.append("   \n");
		// b.append("    #-- olca:cas == fedlca:hasFormattedCAS -- 3 CONDITIONS NEEDING ACTION \n");
		// b.append("    optional { ?of olca:cas ?oCas . } \n");
		// b.append("    optional { ?mflowable fedlca:hasFormattedCAS ?mCas . } \n");
		// b.append("    bind (IF (( bound(?oCas) &&  bound(?mCas) && str(?oCas) != str(?mCas)) , concat(\"; cas: original = \",?oCas),\"\") as ?c1Cas) \n");
		// b.append("    bind (IF ((!bound(?oCas) &&  bound(?mCas)) , \"; cas: original not defined\",\"\") as ?c2Cas) \n");
		// b.append("    bind (IF (( bound(?oCas) && !bound(?mCas)) , \"; cas: master not defined\",\"\") as ?c3Cas) \n");
		// b.append("    bind (concat(?c1Cas,?c2Cas,?c3Cas) as ?cCas) \n");
		// b.append("    bind (IF ((?c1Cas != \"\" || ?c2Cas != \"\"),?mCas, ?oCas) as ?newCas) \n");
		// b.append("    #-- ABOVE, THE USE OF ?oCas IS TO ENSURE THAT IT GETS PUT BACK SINCE IT WILL BE DELETED \n");
		// b.append("   \n");
		// b.append("    #-- olca:formula == eco:chemicalFormula -- 3 CONDITIONS NEEDING ACTION \n");
		// b.append("    optional { ?of olca:formula ?oFormula . } \n");
		// b.append("    optional { ?mflowable eco:chemicalFormula ?mFormula . } \n");
		// b.append("    bind (IF (( bound(?oFormula) &&  bound(?mFormula) && str(?oFormula) != str(?mFormula)) , concat(\"; formula: original = \",?oFormula),\"\") as ?c1Formula) \n");
		// b.append("    bind (IF ((!bound(?oFormula) &&  bound(?mFormula)) , \"; formula: original not defined\",\"\") as ?c2Formula) \n");
		// b.append("    bind (IF (( bound(?oFormula) && !bound(?mFormula)) , \"; formula: master not defined\",\"\") as ?c3Formula) \n");
		// b.append("    bind (concat(?c1Formula,?c2Formula,?c3Formula) as ?cFormula) \n");
		// b.append("   \n");
		// b.append("    #-- fedlca:hasOpenLCAUUID (for both) -- 4 CONDITIONS NEEDING ACTION \n");
		// b.append("    optional { ?mf fedlca:hasOpenLCAUUID ?mUUID . } \n");
		// b.append("    bind (IF (( bound(?oUUID) &&  bound(?mUUID) && str(?oUUID) != str(?mUUID)) , concat(\"; UUID: original = \",?oUUID),\"\") as ?c1UUID) \n");
		// b.append("    bind (IF ((!bound(?oUUID) &&  bound(?mUUID)) , \"; UUID: original not defined\",\"\") as ?c2UUID) \n");
		// b.append("    #-- ABOVE NOT NEEDED UNLESS WE MAKE THE oUUID OPTIONAL \n");
		// b.append("    bind (IF (( bound(?oUUID) && !bound(?mUUID)) , \"; UUID: master not defined\",\"\") as ?c3UUID) \n");
		// b.append("    bind (IF ((!bound(?oUUID) && !bound(?mUUID)) , \"; UUID: new value created\",\"\") as ?c4UUID) \n");
		// b.append("    bind (concat(?c1UUID,?c2UUID,?c3UUID,?c4UUID) as ?cUUID) \n");
		// b.append("    bind (IF (( bound(?oUUID) &&  bound(?mUUID)) , ?mUUID,\"\") as ?new1UUID) \n");
		// b.append("    bind (IF (( bound(?oUUID) && !bound(?mUUID)) , ?oUUID,\"\") as ?new2UUID) \n");
		// b.append("    bind (concat(?new1UUID,?new2UUID) as ?newUUID) \n");
		// b.append("   \n");
		// b.append("    #-- olca:lastChange -- 1 CONDITION NEEDING ACTION \n");
		// b.append("    optional {?of olca:lastChange ?oLastChange } \n");
		// b.append("    bind (IF (bound(?oLastChange) , concat(\"; previous lastChange: \",str(?oLastChange)),\"\") as ?cLastChange)  \n");
		// b.append("    bind (\"" + modString + "\"^^xsd:dateTime as ?newLastChange) \n");
		// // b.append("    bind (\"" + modString + "\" as ?newLastChange) \n");
		// b.append("    #--    ^^^^^^^^^^^^^^^^^^^^^^^^^ PLACE ACTUAL VALUE FROM AnnotationProvider ABOVE \n");
		// b.append("   \n");
		// b.append("    #-- olca:description -- 1 CONDITION PLUS CONCATINATION NEEDED \n");
		// b.append("    optional {?of olca:description ?oDescription } \n");
		// b.append("    bind (IF (!bound(?oDescription) , concat(\"Description created:\",str(now())),?oDescription) as ?cDescription)  \n");
		// b.append("    bind (concat(?cDescription , ?cUUID, ?cName, ?cCas, ?cFormula , ?cLastChange) as ?newDescription) \n");
		// b.append("  } \n");
		// b.append("   \n");
		// String query = b.toString();
		// System.out.println("Big query = \n" + query + "\n");
		// UpdateRequest request = UpdateFactory.create(query);
		// UpdateProcessor proc = UpdateExecutionFactory.create(request, ActiveTDB.graphStore);
		// proc.execute();
		// ActiveTDB.tdbDataset.commit();
		// } catch (Exception e) {
		// System.out.println("01 TDB transaction failed; see Exception: " + e);
		// ActiveTDB.tdbDataset.abort();
		// } finally {
		// ActiveTDB.tdbDataset.end();
		// }
		// // ---- END SAFE -WRITE- TRANSACTION ---
		//
		// String olcaNS = OpenLCA.NS;
		// if (!olcaNS.equals(Prefixes.getNSForPrefix("olca"))) {
		// System.out.println("Aaack!  OpenLCA namespace has changed!");
		// // TODO: Determine a good place to keep track of this since openLCA namespace may change in or out of LCA HT
		// }
		// // int olcaNSLength = olcaNS.length() + 1;
		//
		// List<Resource> originalIRI = new ArrayList<Resource>();
		// List<String> newUUID = new ArrayList<String>();
		//
		// // COLLECT THE OLD IRIs AND THE NEW UUID TO SET UP THE REPLACEMENT
		// // ---- BEGIN SAFE -READ- TRANSACTION ---
		// ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		// tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// try {
		// StringBuilder b = new StringBuilder();
		// b.append(Prefixes.getPrefixesForQuery());
		// b.append("select distinct ?of ?uuid \n");
		// b.append("  where { \n");
		// b.append("    ?mf a fedlca:Flow . \n");
		// b.append("    ?mf fedlca:hasOpenLCAUUID ?muuid . \n");
		// b.append("    ?mf eco:hasDataSource ?mds . \n");
		// b.append("    ?mds a lcaht:MasterDataset . \n");
		// b.append("    bind (str(?muuid) as ?uuid) \n");
		// b.append("    ?of fedlca:hasOpenLCAUUID ?uuid . \n");
		// b.append("    ?of a olca:Flow . \n");
		// b.append("} \n");
		// String query = b.toString();
		// HarmonyQuery2Impl harmonyQuery2Impl = new HarmonyQuery2Impl();
		// harmonyQuery2Impl.setQuery(query);
		// System.out.println("query = " + query);
		// harmonyQuery2Impl.setGraphName(ActiveTDB.exportGraphName);
		//
		// ResultSet resultSet = harmonyQuery2Impl.getResultSet();
		// while (resultSet.hasNext()) {
		// QuerySolution querySolution = resultSet.next();
		// originalIRI.add(querySolution.get("of").asResource());
		// newUUID.add(querySolution.get("uuid").asLiteral().getString());
		// }
		// } catch (Exception e) {
		// System.out.println("Read from TDB failed; see Exception: " + e);
		// ActiveTDB.tdbDataset.abort();
		// } finally {
		// ActiveTDB.tdbDataset.end();
		// }
		// // // ---- END SAFE -READ- TRANSACTION ---
		// //
		// // // ---- BEGIN SAFE -WRITE- TRANSACTION ---
		// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		// tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// try {
		// List<Statement> statementsToAdd = new ArrayList<Statement>();
		// List<Statement> statementsToRemove = new ArrayList<Statement>();
		// for (int i = 0; i < originalIRI.size(); i++) {
		// Resource newIRI = tdbModel.createResource(OpenLCA.NS + newUUID.get(i));
		// Resource oldIRI = originalIRI.get(i);
		// StmtIterator stmtIterator = oldIRI.listProperties();
		// while (stmtIterator.hasNext()) {
		// Statement statement = stmtIterator.next();
		// statementsToAdd.add(tdbModel.createStatement(newIRI, statement.getPredicate(),
		// statement.getObject()));
		// statementsToRemove.add(statement);
		// }
		// Selector selector = new SimpleSelector(null, null, oldIRI);
		// StmtIterator stmtIterator2 = tdbModel.listStatements(selector);
		// while (stmtIterator2.hasNext()) {
		// Statement statement = stmtIterator2.next();
		// statementsToAdd.add(tdbModel.createStatement(statement.getSubject(), statement.getPredicate(),
		// newIRI));
		// statementsToRemove.add(statement);
		// }
		// }
		// for (Statement statement : statementsToAdd) {
		// tdbModel.add(statement);
		// }
		// for (Statement statement : statementsToRemove) {
		// tdbModel.remove(statement);
		// }
		// ActiveTDB.tdbDataset.commit();
		// } catch (Exception e) {
		// System.out.println("replace URI failed; see Exception: " + e);
		// ActiveTDB.tdbDataset.abort();
		// } finally {
		// ActiveTDB.tdbDataset.end();
		// }
		// // ---- END SAFE -WRITE- TRANSACTION ---
		//
		// // ---- BEGIN SAFE -WRITE- TRANSACTION ---
		// ActiveTDB.tdbDataset.begin(ReadWrite.WRITE);
		// tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
		// try {
		// // NOW NEED TO REMOVE ALL EXTRANEOUS STUFF
		// StringBuilder b = new StringBuilder();
		// b.append(Prefixes.getPrefixesForQuery());
		// b.append("  delete {graph <" + ActiveTDB.exportGraphName + ">{  \n");
		// b.append("    ?s ?p ?o . \n");
		// b.append("  }} \n");
		// b.append("   \n");
		// b.append("  where { \n");
		// b.append("    ?ds a eco:DataSource . \n");
		// b.append("    ?ds rdfs:label ?dsName . \n");
		// b.append("    filter(str(?dsName) = \"" + currentName + "\" ) . \n");
		// b.append("    ?s ?p ?o . \n");
		// b.append("    ?s eco:hasDataSource ?ads . \n");
		// b.append("    filter (?ds != ?ads) \n");
		// b.append("    ?s ?p ?o . \n");
		// b.append("} \n");
		// String query = b.toString();
		// System.out.println("Replace UUIDs query = \n" + query + "\n");
		// UpdateRequest request = UpdateFactory.create(query);
		// UpdateProcessor proc = UpdateExecutionFactory.create(request, ActiveTDB.graphStore);
		// proc.execute();
		// ActiveTDB.tdbDataset.commit();
		// } catch (Exception e) {
		// System.out.println("Remove non-dataset stuff failed; see Exception: " + e);
		// ActiveTDB.tdbDataset.abort();
		// } finally {
		// ActiveTDB.tdbDataset.end();
		// }
		// // ---- END SAFE -WRITE- TRANSACTION ---

		try {
			FileOutputStream fout = new FileOutputStream(saveTo);
			String outType = ActiveTDB.getRDFTypeFromSuffix(saveTo);

			// --- BEGIN SAFE -READ- TRANSACTION ---
			ActiveTDB.tdbDataset.begin(ReadWrite.READ);
			Model tdbModel = ActiveTDB.getModel(ActiveTDB.exportGraphName);
			tdbModel.write(fout, outType);
			ActiveTDB.tdbDataset.end();
			fout.close();
			removePrefix(saveTo);
			// ---- END SAFE -WRITE- TRANSACTION ---

		} catch (FileNotFoundException e1) {
			ActiveTDB.tdbDataset.abort();
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ActiveTDB.clearExportGraphContents();

		/* To copy a dataset to the export graph */
		// ActiveTDB.copyDatasetContentsToExportGraph(olca);
		return null;
	}

	// OpenLCA does not handle the olca: prefix correctly. Resolve by removing and letting the @context declaration
	// handle
	private void removePrefix(String path) {
		String tempPath = path + ".lcaht.tmp";
		File oldFile = new File(path);
		File newFile = new File(tempPath);
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(oldFile));
			String line = bufferedReader.readLine();
			PrintWriter printWriter = new PrintWriter(new PrintWriter(newFile));
			while (line != null) {
				line = line.replaceAll("olca:", "");
				printWriter.println(line);
				line = bufferedReader.readLine();
			}
			printWriter.close();
			bufferedReader.close();
			oldFile.delete();
			newFile.renameTo(oldFile);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
