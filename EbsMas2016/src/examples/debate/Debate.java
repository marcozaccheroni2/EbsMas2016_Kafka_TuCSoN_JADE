package examples.debate;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import examples.playground.KeeperAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;

public class Debate {

	public static void main(String[] args) {
		// This is the important method. This launches the jade platform.
		Runtime rt = Runtime.instance();
		
		Profile profile = new ProfileImpl();
		
		// With the Profile you can set some options for the container
		profile.setParameter(Profile.PLATFORM_ID, "Platform Name");
		profile.setParameter(Profile.CONTAINER_NAME, "Container Name");
		
		// Create the Main Container
		AgentContainer mainContainer = rt.createMainContainer(profile);
		
		try {
			DebateModeratorAgent jaquen = new DebateModeratorAgent();
			mainContainer.acceptNewAgent("Jaqen", jaquen).start();
			
			wait(1.0);
			
			DebateParticipantAgent ned = new DebateParticipantAgent();
			mainContainer.acceptNewAgent("Ned", ned).start();
			
			wait(1.5);
			
			DebateParticipantAgent jon = new DebateParticipantAgent();
			mainContainer.acceptNewAgent("Jon", jon).start();
			
		} catch(StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	private static void wait(double seconds){
		try {
			double millis = seconds * 1000;
		    Thread.sleep((int) millis);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}

}