package Smartphone_Shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import SupplyChain_ontology.SupplyChainOntology;
import SupplyChain_ontology.elements.Battery;
import SupplyChain_ontology.elements.Customer_Order;
import SupplyChain_ontology.elements.Phablet;
import SupplyChain_ontology.elements.RAM;
import SupplyChain_ontology.elements.Screen;
import SupplyChain_ontology.elements.Smartphone;
import SupplyChain_ontology.elements.Storage;

import java.util.ArrayList;
import java.util.HashMap;

import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.Agent;

public class BuyerAgent extends Agent{
	//private HashMap<String,ArrayList<Customer_Offer>> currentOffers = new HashMap<>();
	private AID tickerAgent;
	private int numQueriesSent;
	private Codec codec = new SLCodec();
	private Ontology ontology = SupplyChainOntology.getInstance();
	private AID sellerAID;
	@Override
	protected void setup() {
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("buyer");
		sd.setName(getLocalName() + "-buyer-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		String[] args = (String[])this.getArguments();
		sellerAID = new AID("manufacturer",AID.ISLOCALNAME);
		
		addBehaviour(new TickerWaiter(this));
	}


	@Override
	protected void takeDown() {
		//Deregister from the yellow pages
		try{
			DFService.deregister(this);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
	}

	public class TickerWaiter extends CyclicBehaviour {

		//behaviour to wait for a new day
		public TickerWaiter(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new day"),
					MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt); 
			if(msg != null) {
				if(tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if(msg.getContent().equals("new day")) {
					//spawn new sequential behaviour for day's activities
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					//sub-behaviours will execute in the order they are added
					dailyActivity.addSubBehaviour(new SendOffers(myAgent));
					dailyActivity.addSubBehaviour(new EndDay(myAgent));
					myAgent.addBehaviour(dailyActivity);
				}
				else {
					//termination message to end simulation
					myAgent.doDelete();
				}
			}
			else{
				block();
			}
		}

	}



	public class SendOffers extends OneShotBehaviour {

		public SendOffers(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(sellerAID); // sellerAID is the AID of the Seller agent
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName()); 
			double rand = Math.random();
			if(rand < 0.5) {
				double rand2 = Math.random();
				double rand3 = Math.random();


				Smartphone ss = new Smartphone();
				Screen screen = new Screen();
				screen.setInch("5inch");
				screen.setSerialNumber(101);
				ss.setScreen(screen);
				Battery battery = new Battery();
				battery.setCapacity("2000mAh");
				battery.setSerialNumber(201);
				ss.setBattery(battery);
				RAM ram = new RAM();
				if(rand2 < 0.5) {
					ram.setAmmount("4Gb");
					ram.setSerialNumber(301);
					ss.setRAM(ram);
				}
				else {
					ram.setAmmount("8Gb");
					ram.setSerialNumber(302);
					ss.setRAM(ram);
				}
				if(rand3 < 0.5) {
	
					Storage storage = new Storage();
					storage.setSpace("64GB");
					storage.setSerialNumber(401);
					ss.setStorage(storage);
				}
				else {
					Storage storage = new Storage();
					storage.setSpace("256GB");
					storage.setSerialNumber(402);
					ss.setStorage(storage);
				}
				
				ACLMessage enquiry = new ACLMessage(ACLMessage.REQUEST);
				enquiry.addReceiver(sellerAID);
				enquiry.setLanguage(codec.getName());
				enquiry.setOntology(ontology.getName());
				
				Customer_Order order = new Customer_Order();
				order.setDevice(ss);
				double numberOfSmartphones = Math.floor(1 + 50*rand);
				order.setNumOfDevices(numberOfSmartphones);
				order.setPrice(Math.floor(100+500*rand));
				order.setDueDate(Math.floor(1 + 10*rand));
				order.setPenalty(numberOfSmartphones * Math.floor(1 + 50 * rand));
				order.setBuyer(getAID());
				
				Action request = new Action();
				request.setAction(order);
				request.setActor(sellerAID);
				// the agent that you request to perform the action
				try {
				// Let JADE convert from Java objects to string
					getContentManager().fillContent(enquiry, request); //send the wrapper object
					send(enquiry);
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				
			}
				catch (OntologyException oe) {
					oe.printStackTrace();
					 System.out.println(oe.toString());
				}
	

			}
			else {
				double rand2 = Math.random();
				double rand3 = Math.random();
				Phablet ps = new Phablet();
				Screen screen = new Screen();
				screen.setInch("7inch");
				screen.setSerialNumber(102);
				ps.setScreen(screen);
				Battery battery = new Battery();
				battery.setSerialNumber(202);
				battery.setCapacity("3000mAh");
				ps.setBattery(battery);
				RAM ram = new RAM();
				if(rand2 < 0.5) {
					ram.setAmmount("4Gb");
					ram.setSerialNumber(301);
					ps.setRAM(ram);
				}
				else {
					ram.setAmmount("8Gb");
					ram.setSerialNumber(302);
					ps.setRAM(ram);
				}
				if(rand3 < 0.5) {
	
					Storage storage = new Storage();
					storage.setSpace("64GB");
					storage.setSerialNumber(401);
					ps.setStorage(storage);
				}
				else {
					Storage storage = new Storage();
					storage.setSpace("256GB");
					storage.setSerialNumber(402);
					ps.setStorage(storage);
				}
				ACLMessage enquiry = new ACLMessage(ACLMessage.REQUEST);
				enquiry.addReceiver(sellerAID);
				enquiry.setLanguage(codec.getName());
				enquiry.setOntology(ontology.getName());
				Customer_Order order = new Customer_Order();
				order.setDevice(ps);
				double numberOfSmartphones = Math.floor(1 + 50*rand);
				order.setNumOfDevices(numberOfSmartphones);
				order.setPrice(Math.floor(100+500*rand));
				order.setDueDate(Math.floor(1 + 10*rand));
				order.setPenalty(numberOfSmartphones * Math.floor(1 + 50 * rand));
				order.setBuyer(getAID());

				Action request = new Action();
				request.setAction(order);
				request.setActor(sellerAID);
				// the agent that you request to perform the action
				try {
				// Let JADE convert from Java objects to string
					getContentManager().fillContent(enquiry, request); //send the wrapper object
					send(enquiry);
				}
				catch (CodecException ce) {
					ce.printStackTrace();
							}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}
				}
			}

		}
		

	
	public class EndDay extends OneShotBehaviour {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EndDay(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(tickerAgent);
			msg.setContent("done");
			myAgent.send(msg);
			//send a message to each seller that we have finished
			ACLMessage sellerDone = new ACLMessage(ACLMessage.INFORM);
			sellerDone.setContent("done");
			sellerDone.addReceiver(sellerAID);
			myAgent.send(sellerDone);
		}
		
	}
}
	
	
	