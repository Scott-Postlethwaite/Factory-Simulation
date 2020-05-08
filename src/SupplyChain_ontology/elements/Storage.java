package SupplyChain_ontology.elements;

import jade.content.onto.annotations.Slot;

public class Storage extends Component {
	private String space;
	
	@Slot(mandatory = true)
	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}
}
	
