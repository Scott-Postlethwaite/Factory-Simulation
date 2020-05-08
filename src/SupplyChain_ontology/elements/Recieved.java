package SupplyChain_ontology.elements;

import jade.content.Predicate;

public class Recieved implements Predicate{
	private Manufacturer_Order order; 
	
	public Manufacturer_Order getOrder() {
		return order;
	}
	public void setOrder(Manufacturer_Order order) {
		this.order = order;
	}
	
	
}
