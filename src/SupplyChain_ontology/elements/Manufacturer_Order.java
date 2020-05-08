package SupplyChain_ontology.elements;

import jade.content.AgentAction;
import jade.core.AID;

public class Manufacturer_Order implements AgentAction {
	private Component component; 
	private AID seller;
	private int daysLeft;
	private int numOfComponents;
	public Component getComponent() {
		return component;
	}
	public void setComponent(Component component) {
		this.component = component;
	}
	public AID getSeller() {
		return seller;
	}
	public void setSeller(AID seller) {
		this.seller = seller;
	} 
	public int getDaysLeft() {
		return daysLeft;
	}
	public void setDaysLeft(int  daysLeft) {
		
		this.daysLeft = daysLeft;
	} 
	
	public int getNumOfComponents() {
		return numOfComponents;
	}
	public void setNumOfComponents(int  numOfComponents) {
		
		this.numOfComponents = numOfComponents;
	} 
}
