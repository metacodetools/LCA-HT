//package gov.epa.nrmrl.std.lca.ht.vocabulary;
//
//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
//import com.hp.hpl.jena.rdf.model.Property;
//import com.hp.hpl.jena.rdf.model.Resource;
//
//
///**
// * Vocabulary definitions taken from the Earthster Core Ontologhy (ECO) by the Environmental Modeling and Visualization Lab (EMVL)
// * Supporting work for PIs Troy Hawkins and Wes Ingwersen, US EPA 
// * @author Tom Transue 03 Jan 2014 09:46 
// */
//
///**
// *  Effect Aggregation Categories ontology.
//
//This is an eco extension ontology that defines the class FlowAggregationCategory to represent the concept of a flow as a pair of substance and compartment.
//
//It is an extension point for defining other kinds of common aggregation categories.
//
//To the extent possible under law, New Earth  has waived all copyright and related or neighboring rights to http://ontology.earthster.org/eco/fasc. This work is published from United States.
// * @author Tom
// *
// */
//
//public class FASC {
//    /** <p>The RDF tdbModel that holds the vocabulary terms</p> */
//    private static Model m_model = ModelFactory.createDefaultModel();
//
//    /** <p>The namespace of the vocabulary as a string</p> */
//    public static final String NS = NS+"";
//
//    /** <p>The namespace of the vocabulary as a string</p>
//     *  @see #NS */
//    public static String getURI() {return NS;}
//
//    /** <p>The namespace of the vocabulary as a resource</p> */
//    public static final Resource NAMESPACE = m_model.createResource( NS );
//
//    /** <p>Compartments are used for classifying effects.  Effects have a hasCompartment property and the type of the value of that property may be used to classify the effect.  Examples of compartments include emissions to urban air and resource consumption from water.</p> */
//    public static final Resource Compartment = m_model.createResource( NS+"Compartment" );
//
//    /** <p>hasCompartment indicates a compartment related to a resource.  It may be used to relate a flow aggregation category to a compartment.</p> */
//    public static final Property hasCompartment = m_model.createProperty( NS+"hasCompartment" );
//
//}

package gov.epa.nrmrl.std.lca.ht.vocabulary;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Vocabulary definitions taken from the Earthster Core Ontologhy (ECO) by the Environmental Modeling and Visualization Lab (EMVL)
 * Supporting work for PIs Troy Hawkins and Wes Ingwersen, US EPA 
 * @author Tom Transue 03 Jan 2014 09:46 
 */

public class FASC {
    /** <p>The RDF tdbModel that holds the vocabulary terms</p> */
//    private static Model m_model = ModelFactory.createDefaultModel();
	private static Model m_model = ActiveTDB.getModel(null);


    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://ontology.earthster.org/eco/fasc#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p>A compartment involving emissions.</p> */
    public static final Resource EmissionCompartment = m_model.createResource( NS+"EmissionCompartment" );

    /** <p>Compartments are used for classifying effects.  Effects have a hasCompartment property and the type of the value of that property may be used to classify the effect.Examples of compartments include emissions to urban air and resource consumption from water.</p> */
    public static final Resource Compartment = m_model.createResource( NS+"Compartment" );

    /** <p></p> */
    public static final Resource SocialCompartment = m_model.createResource( NS+"SocialCompartment" );

    /** <p>A compartment involving consumption of renable resources.</p> */
    public static final Resource RenewableResourceConsumptionCompartment = m_model.createResource( NS+"RenewableResourceConsumptionCompartment" );

    /** <p>A compartment involving resource consumption.</p> */
    public static final Resource ResourceConsumptionCompartment = m_model.createResource( NS+"ResourceConsumptionCompartment" );

    /** <p>A compartment involving land use.</p> */
    public static final Resource LandUseCompartment = m_model.createResource( NS+"LandUseCompartment" );

    /** <p></p> */
    public static final Resource NonAgriculturalSoilCompartment = m_model.createResource( NS+"NonAgriculturalSoilCompartment" );

    /** <p>A compartment whose medium is soil.</p> */
    public static final Resource SoilCompartment = m_model.createResource( NS+"SoilCompartment" );

    /** <p>High air (typically emissions from high stacks) or non-urban air.</p> */
    public static final Resource HiOrNonUrbanAirCompartment = m_model.createResource( NS+"HiOrNonUrbanAirCompartment" );

    /** <p>A compartment who medium is air.</p> */
    public static final Resource AirCompartment = m_model.createResource( NS+"AirCompartment" );

    /** <p>A compartment whose medium is forrest soil.</p> */
    public static final Resource ForestrySoilCompartment = m_model.createResource( NS+"ForestrySoilCompartment" );

    /** <p></p> */
    public static final Resource EconomicCompartment = m_model.createResource( NS+"EconomicCompartment" );

    /** <p>A compartment whose effects are not only long term.</p> */
    public static final Resource NotLongTermCompartment = m_model.createResource( NS+"NotLongTermCompartment" );

    /** <p>A compartment involving land transformation.</p> */
    public static final Resource LandTransformationCompartment = m_model.createResource( NS+"LandTransformationCompartment" );

    /** <p>A compartments whose effects are long term.</p> */
    public static final Resource LongTermCompartment = m_model.createResource( NS+"LongTermCompartment" );

    /** <p>A compartment whose medium is the lower stratosphere and upper troposphere</p> */
    public static final Resource LowerStratoUpperTropoCompartment = m_model.createResource( NS+"LowerStratoUpperTropoCompartment" );

    /** <p>A compartment involving consumption of genetic resources.</p> */
    public static final Resource GeneticResourceConsumptionCompartment = m_model.createResource( NS+"GeneticResourceConsumptionCompartment" );

    /** <p>A flow aggregation category is pair consisting of a flowable and a compartment.Examples of flow aggregation categories include carbon dioxide to urban air.A necessary and sufficient condition for two flow aggregation categories to be identical is that they have the same flowable and the same compartment.</p> */
    public static final Resource FlowAggregationCategory = m_model.createResource( NS+"FlowAggregationCategory" );

    /** <p>A compartment whose medium is river water.</p> */
    public static final Resource RiverWaterCompartment = m_model.createResource( NS+"RiverWaterCompartment" );

    /** <p>Water that does not have a high salt content.  Also known as sweet water.</p> */
    public static final Resource FreshWaterCompartment = m_model.createResource( NS+"FreshWaterCompartment" );

    /** <p>A  substance source is classification of the source of a substance.Examples of substance sources are fossil and biotic.No identity criteria are defined for substance sources.</p> */
    public static final Resource SubstanceSource = m_model.createResource( NS+"SubstanceSource" );

    /** <p>A compartment involving land occupation.</p> */
    public static final Resource LandOccuptationCompartment = m_model.createResource( NS+"LandOccuptationCompartment" );

    /** <p></p> */
    public static final Resource LowAirCompartment = m_model.createResource( NS+"LowAirCompartment" );

    /** <p>A compartment whose medium is lake water.</p> */
    public static final Resource LakeWaterCompartment = m_model.createResource( NS+"LakeWaterCompartment" );

    /** <p>A compartment whose medium has a high population density.</p> */
    public static final Resource HighPopulationDensityCompartment = m_model.createResource( NS+"HighPopulationDensityCompartment" );

    /** <p>A compartment whose medium is water.</p> */
    public static final Resource WaterCompartment = m_model.createResource( NS+"WaterCompartment" );

    /** <p>A compartment whose medium is biotic.</p> */
    public static final Resource BioticCompartment = m_model.createResource( NS+"BioticCompartment" );

    /** <p></p> */
    public static final Resource BioticSubstanceSource = m_model.createResource( NS+"BioticSubstanceSource" );

    /** <p></p> */
    public static final Resource FossilSubstanceSource = m_model.createResource( NS+"FossilSubstanceSource" );

    /** <p>A compartment involving consumption of element resources.</p> */
    public static final Resource ElementResourceConsumptionCompartment = m_model.createResource( NS+"ElementResourceConsumptionCompartment" );

    /** <p>A compartment whose medium is salt water.</p> */
    public static final Resource SaltWaterCompartment = m_model.createResource( NS+"SaltWaterCompartment" );

    /** <p>A compartment whose medium is ground water.</p> */
    public static final Resource GroundWaterCompartment = m_model.createResource( NS+"GroundWaterCompartment" );

    /** <p>A compartment whose medium has a low population density.</p> */
    public static final Resource LowPopulationDensityCompartment = m_model.createResource( NS+"LowPopulationDensityCompartment" );

    /** <p>A compartment involving consumption of material resources.</p> */
    public static final Resource MaterialResourceConsumptionCompartment = m_model.createResource( NS+"MaterialResourceConsumptionCompartment" );

    /** <p>A compartment whose medium is the ground.</p> */
    public static final Resource GroundCompartment = m_model.createResource( NS+"GroundCompartment" );

    /** <p>A compartment involving consumption of energy resources.</p> */
    public static final Resource EnergyResourceConsumptionCompartment = m_model.createResource( NS+"EnergyResourceConsumptionCompartment" );

    /** <p>A compartment whose medium is fossil water.</p> */
    public static final Resource FossilWaterCompartment = m_model.createResource( NS+"FossilWaterCompartment" );

    /** <p>A compartment whose medium is agricultural soil.</p> */
    public static final Resource AgriculturalSoilCompartment = m_model.createResource( NS+"AgriculturalSoilCompartment" );

    /** <p>A compartment involving consumption of non-renable resources.</p> */
    public static final Resource NonRenewableResourceConsumptionCompartment = m_model.createResource( NS+"NonRenewableResourceConsumptionCompartment" );

    /** <p>A compartment whose medium is industrial soil.</p> */
    public static final Resource IndustrialSoilCompartment = m_model.createResource( NS+"IndustrialSoilCompartment" );

    /** <p>relates an effect aggregation category to the effect it aggregates.</p> */
    public static final Property hasEffect = m_model.createProperty( NS+"hasEffect" );

    /** <p>hasCompartment indicates a compartment related to a resource.  It may be used to relate a flow aggregation category to a compartment.</p> */
    public static final Property hasCompartment = m_model.createProperty( NS+"hasCompartment" );

    /** <p>hasFlowable relates a flow aggregation category to a flowable.</p> */
    public static final Property hasFlowable = m_model.createProperty( NS+"hasFlowable" );

    /** <p>relates an effect aggregation category to the land transformation category the transfromation is to.</p> */
    public static final Property hasTransformationToCategory = m_model.createProperty( NS+"hasTransformationToCategory" );

    /** <p>relates an effect aggregation category to the land transformation category the transfromation is from.</p> */
    public static final Property hasTransformationFromCategory = m_model.createProperty( NS+"hasTransformationFromCategory" );

    /** <p>relates an EffectAggregationCategory to a land category category</p> */
    public static final Property hasOccupationCategory = m_model.createProperty( NS+"hasOccupationCategory" );

    /** <p>relates a volume occupation effect to a category of volume occupation.</p> */
    public static final Property hasVolumeOccupationCategory = m_model.createProperty( NS+"hasVolumeOccupationCategory" );

}
