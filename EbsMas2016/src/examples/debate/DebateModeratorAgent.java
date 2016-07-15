package examples.debate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;

public class DebateModeratorAgent extends Agent {

	private class ChooseArgumentBehaviour extends TickerBehaviour {
		
		private static final long serialVersionUID = 1L;
		
		public ChooseArgumentBehaviour(Agent a, long timeout) {
	    	super(a, timeout);
	    }
		
	    @Override
		public void onTick() {
	    	
	    	String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	    	String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
            StringBuilder salt = new StringBuilder();
            Random rnd = new Random();
            while (salt.length() < 5) {
                int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                salt.append(SALTCHARS.charAt(index));
            }
            String rndString = salt.toString();
	    	String msg = "{\"type\":\"new-argument\", \"argument\":\"" + rndString + "\", \"time\":\"" + timeStamp + "\"}";
	    	DebateModeratorAgent.this.producer.send(new ProducerRecord<String, String>(topicName, msg));
	    	DebateModeratorAgent.this.log("A man propose an argument: " + rndString);
		}
		
	}
	
	private static final long serialVersionUID = 1L;

	private final String zkHosts = "localhost:2181";
	private final int zkSessionTimeoutMs = 15 * 1000;
    private final int zkConnectionTimeoutMs = 10 * 1000;
    private final ZkClient zkClient = new ZkClient(zkHosts, zkSessionTimeoutMs, zkConnectionTimeoutMs, ZKStringSerializer$.MODULE$);
    private final ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(zkHosts), false);
    
    String topicName = "debateTopic";
    
    private KafkaProducer<String, String> producer;
	
	private void log(final String msg) {
		String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        System.out.println("-----------------------\n" + timeStamp + " [" + this.getAID().getLocalName() + ", The Moderator]: " + msg + "\n-----------------------");
    }
	
	@Override
    protected void setup() {
        this.log("Moderator Started.");

        int partitions = 1;
        int replication = 1;
        Properties topicConfiguration = new Properties();
        
        if (!AdminUtils.topicExists(zkUtils, topicName)){
        	AdminUtils.createTopic(zkUtils, topicName, partitions, replication, topicConfiguration, null);
        }
        
        Properties properties = new Properties();
    	properties.put("bootstrap.servers", "localhost:9092");
    	properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    	properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    	
    	this.producer = new KafkaProducer<>(properties);
        
        this.addBehaviour(new ChooseArgumentBehaviour(this, 3*1000));
        
    }
	
	@Override
    protected void takeDown() {
		this.log("Moderator out! <MIC DROP>");
		
		this.producer.close();
		AdminUtils.deleteTopic(zkUtils, topicName);
		
    }
	
}
