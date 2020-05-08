package SupplyChain_ontology.elements;

import jade.content.onto.annotations.Slot;

public class RAM extends Component {
	private String ammount; 
	
	@Slot(mandatory = true)
	public String getAmmount() {
		return ammount; 
	}
	
	public void setAmmount(String ammount) {
		this.ammount = ammount; 
	}
}
