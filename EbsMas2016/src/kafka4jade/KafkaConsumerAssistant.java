package kafka4jade;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import jade.core.behaviours.Behaviour;

public class KafkaConsumerAssistant {
	
	private Behaviour behav;
	
	private KafkaConsumer<String, String> consumer;
	private ArrayList<String> topics = new ArrayList<>();
	
	private ConsumerRecords<String, String> result = null;
	private long timeout;
	private boolean consumer_is_alive = true;
	private boolean can_consume = false;
	
	private PollThread pollThread = new PollThread();
	
	public KafkaConsumerAssistant(Behaviour b, String consumer_group){
		
		this.behav = b;
		
		Properties consumer_props = new Properties();
    	consumer_props.put("bootstrap.servers", "localhost:9092");
    	consumer_props.put("group.id", consumer_group);
    	consumer_props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    	consumer_props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    	
    	this.consumer = new KafkaConsumer<String, String>(consumer_props);
    	
	}
	
	public void subscribe(String topic) {
		topics.add(topic);
		consumer.subscribe(topics);
	}
	
	public void unsubscribe(String topic) {
		if (topics.contains(topic)){
			topics.remove(topic);
			consumer.subscribe(topics);
		}
	}
	
	public ConsumerRecords<String, String> consume(long timeout){
		
		if (timeout == 0) return consumer.poll(0);
		
		if (result == null){
			behav.block();
			
			this.timeout = timeout;
			this.can_consume = true;
			
			if (!pollThread.isAlive()) pollThread.start();
			
			return ConsumerRecords.empty();
		} else {
			ConsumerRecords<String, String> current_results = result;
			result = null;
			return current_results;
		}
	}
	
	public void close(){
		this.consumer_is_alive = false;
	}
	
	private class PollThread extends Thread {
		public void run() {
			while (consumer_is_alive) {
				if (can_consume) {
					result = consumer.poll(timeout);
					behav.restart();
					can_consume = false;
				}
			}
			consumer.close();
		}
	}
}
