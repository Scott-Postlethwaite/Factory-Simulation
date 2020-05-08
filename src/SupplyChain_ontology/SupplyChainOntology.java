package SupplyChain_ontology;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class SupplyChainOntology extends BeanOntology{
	
	private static Ontology theInstance = new SupplyChainOntology("my_ontology");
	
	public static Ontology getInstance(){
		return theInstance;
	}

	private SupplyChainOntology(String name) {
		super(name);
		try {
			add("SupplyChain_ontology.elements");
		} catch (BeanOntologyException e) {
			e.printStackTrace();
		}
	}
}