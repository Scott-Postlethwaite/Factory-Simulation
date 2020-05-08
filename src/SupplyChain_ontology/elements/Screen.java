package SupplyChain_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Screen extends Component {
	private String inch; 
	
	@Slot(mandatory = true)
	String getInch() {
		return inch;
	}
	
	public void setInch(String inch) {
		this.inch = inch; 
	}
}
