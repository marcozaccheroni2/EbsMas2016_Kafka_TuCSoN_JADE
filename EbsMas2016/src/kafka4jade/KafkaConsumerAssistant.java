package kafka4jade;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import jade.core.behaviours.Behaviour;

public class KafkaConsumerAssistant {
	
	private Behaviour behav;
	
	private KafkaConsumer<String, String> consumer;
	private ArrayList<String> topics = new ArrayList<>();
	
	private ConsumerRecords<String, String> result = null;
	private long timeout;
	
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
			pollThread.can_consume.set(true);
			
			if (!pollThread.isAlive()) pollThread.start();
			
			return ConsumerRecords.empty();
		} else {
			ConsumerRecords<String, String> current_results = result;
			result = null;
			return current_results;
		}
	}
	
	public void close(){
		pollThread.consumer_is_alive.set(false);;
	}
	
	private class PollThread extends Thread {
		
		private AtomicBoolean consumer_is_alive = new AtomicBoolean(true);
		private AtomicBoolean can_consume = new AtomicBoolean(false);
		
		public void run() {
			while (this.consumer_is_alive.get()) {
				if (this.can_consume.get()) {
					System.out.println("[Poll Request]");
					result = consumer.poll(timeout);
					System.out.println("[Result ready]");
					this.can_consume.set(false);;
					behav.restart();
				}
			}
			System.out.println("[Closing consumer]");
			consumer.close();
		}
	}
}
