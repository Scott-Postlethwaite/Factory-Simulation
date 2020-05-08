package SupplyChain_ontology.elements;

import jade.content.Predicate;

public class Delivered implements Predicate{
	private Customer_Order order; 
	
	public Customer_Order getOrder() {
		return order;
	}
	public void setOrder(Customer_Order order) {
		this.order = order;
	}
	
	
}
