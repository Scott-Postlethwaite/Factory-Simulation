package SupplyChain_ontology.elements;
import jade.content.Concept;
import jade.content.onto.annotations.Slot;

public class Device implements Concept {
	int serialNumber; 
	Storage storage;
	RAM ram; 
	
	
	@Slot(mandatory = true)
	public Storage getStorage() {
		return storage;
	}
	public void setStorage(Storage storage) {
		this.storage = storage;
	} 
	
	@Slot(mandatory = true)
	public RAM getRAM() {
		return ram;
	}
	public void setRAM(RAM ram) {
		this.ram = ram;
	} 
	
}
