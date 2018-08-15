package ua.demo.receiver.listener;

import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Log4j
@Component
public class Receiver {
    private AtomicInteger count = new AtomicInteger(0);

    @RabbitListener(queues = "report_queue")
    public void worker(String message) {
        log.info("Received on worker : " + message + ", count: " + count.incrementAndGet());
    }
}
