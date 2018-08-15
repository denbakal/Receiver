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

	public static void main(String[] args) {
		SpringApplication.run(ReceiverApplication.class, args);
	}

	@Override
	public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
		registrar.setContainerFactory(myRabbitListenerContainerFactory());
	}

	@Bean
	public DirectExchange reportExchange() {
		return new DirectExchange("report_exchange");
	}

	@Bean
	public Queue reportQueue() {
		return new Queue("report_queue", true);
	}

	@Bean
	public Binding bindingReport() {
		return BindingBuilder.bind(reportQueue())
				.to(reportExchange())
				.with("report_queue_key");
	}
}
