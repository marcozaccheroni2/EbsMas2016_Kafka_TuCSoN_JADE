package examples.playground.backup;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class Playground {

	public static void main(String[] args) {
		
		// This is the important method. This launches the jade platform.
		Runtime rt = Runtime.instance();
		
		Profile profile = new ProfileImpl();
		
		// With the Profile you can set some options for the container
		profile.setParameter(Profile.PLATFORM_ID, "Playground");
		profile.setParameter(Profile.CONTAINER_NAME, "K2");
		profile.setParameter(Profile.SERVICES, "it.unibo.tucson.jade.service.TucsonService");
		
		
		// Create the Main Container
		AgentContainer mainContainer = rt.createMainContainer(profile);
		
		DebugGUI debugGUI = new DebugGUI();
		debugGUI.setVisible(true);
		
		try {
			
			int players_per_team = 2;
			
			KeeperAgent keeper = new KeeperAgent(debugGUI, players_per_team);
			mainContainer.acceptNewAgent("keeper", keeper).start();
			
			wait(5);
			
			for (int i = 0; i < (players_per_team + 2); i++) {
				wait(1);
				mainContainer.acceptNewAgent("player_" + i, new PlayerAgent(debugGUI)).start();
			}
			
		} catch(StaleProxyException e) {
			e.printStackTrace();
		}

	}
	
	private static void wait(int seconds){
		try {
		    Thread.sleep(seconds * 1000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}

}
