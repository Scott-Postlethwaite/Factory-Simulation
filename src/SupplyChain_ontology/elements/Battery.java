package SupplyChain_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Battery extends Component {
	private String capacity; 
	
	@Slot(mandatory = true)
	public String getCapacity(){
		return capacity;
	}
	
	public void setCapacity(String capacity) {
		this.capacity = capacity; 
	}
}
