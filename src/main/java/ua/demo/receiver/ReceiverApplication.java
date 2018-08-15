package ua.demo.receiver;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableRabbit
public class ReceiverApplication implements RabbitListenerConfigurer {
	@Bean
	public SimpleRabbitListenerContainerFactory myRabbitListenerContainerFactory() {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory());
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//		factory.setConcurrentConsumers(5);
//		factory.setMaxConcurrentConsumers(10);
//		factory.setPrefetchCount(5);
		return factory;
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		return new CachingConnectionFactory("localhost");
	}

	@Bean
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory());
	}

	/*@Bean
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
		rabbitTemplate.setQueue("report_queue");
//		rabbitTemplate.setReplyTimeout(60 * 100); //no reply to - we use direct-reply-to
		return rabbitTemplate;
	}*/

//	@Bean
//	public Queue reportQueue() {
//		return new Queue("report_queue");
//	}

	public static void main(String[] args) {
		SpringApplication.run(ReceiverApplication.class, args);
	}

	@Override
	public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
		registrar.setContainerFactory(myRabbitListenerContainerFactory());
	}

	/* DLX */
	/*@Bean
	public Queue retryQueue() {
		Map<String, Object> args = new HashMap<>();
		args.put("x-dead-letter-exchange", "dlx");
		args.put("x-message-ttl", 5000);
		return new Queue("report_queue", false, false, false, args);
	}*/

	@Bean
	public DirectExchange reportExchange() {
		return new DirectExchange("report_exchange");
	}

	@Bean
	public Queue reportQueue() {
		Map<String, Object> args = new HashMap<>();
		args.put("x-dead-letter-exchange", "retry_exchange");
		args.put("x-dead-letter-routing-key", "retry_queue_key");
//		args.put("x-message-ttl", 5000);
		return new Queue("report_queue", true, false, false, args);
	}

	@Bean
	public Binding bindingReport() {
		return BindingBuilder.bind(reportQueue())
				.to(reportExchange())
				.with("report_queue_key");
	}

	@Bean
	public DirectExchange retryExchange() {
		return new DirectExchange("retry_exchange");
	}

	@Bean
	public Queue retryQueue() {
		Map<String, Object> args = new HashMap<>();
		args.put("x-dead-letter-exchange", "report_exchange");
		// Route to the incoming queue when the TTL occurs
		args.put("x-dead-letter-routing-key", "report_queue_key");
		// TTL 15 seconds
		args.put("x-message-ttl", 15000);
		return new Queue("retry_queue", true, false, false, args);
	}

	@Bean
	public Binding bindingRetry() {
		return BindingBuilder.bind(retryQueue())
				.to(retryExchange())
				.with("retry_queue_key");
	}
}
