package ua.demo.receiver.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j
@Component
public class Receiver {
    private AtomicInteger count = new AtomicInteger(0);

    @RabbitListener(queues = "report_queue")
    public void worker(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws InterruptedException, IOException {
        log.info("Received on worker : " + message + ", count: " + count.incrementAndGet());
        Thread.sleep(2000L);
        channel.basicReject(tag, true);
    }
}
