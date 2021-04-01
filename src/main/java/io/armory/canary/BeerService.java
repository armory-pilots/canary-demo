package io.armory.canary;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class BeerService {

    private final MeterRegistry meterRegistry;
    private Counter regularIPAOrderCounter;
    private Counter hazyIPAOrderCounter;
    private Counter doubleIPAOrderCounter;

    private List<Order> orders = new ArrayList<>();

    public BeerService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initOrderCounters();
        Gauge.builder("beer.ordersInQueue", orders, Collection::size)
                .description("Number of unserved orders")
                .register(meterRegistry);
    }

    private void initOrderCounters() {
        hazyIPAOrderCounter = Counter.builder("beer.orders")
                .tag("type", "hazyIPA")
                .description("The number of orders ever placed for Hazy IPA beers")
                .register(meterRegistry);
        regularIPAOrderCounter = Counter.builder("beer.orders")
                .tag("type", "IPA")
                .description("The number of orders ever placed for IPA beers")
                .register(meterRegistry);
        doubleIPAOrderCounter = Counter.builder("beer.orders")
                .tag("type", "doubleIPA")
                .description("The number of orders ever placed for IPA beers")
                .register(meterRegistry);
    }

    public void orderBeer(Order order) {
        orders.add(order);

        switch (order.type) {
            case "hazyIPA": hazyIPAOrderCounter.increment(1.0);
            case "IPA": regularIPAOrderCounter.increment(1.0);
            case "doubleIPA": doubleIPAOrderCounter.increment(1.0);
        }
    }

    @Scheduled(fixedRate = 5000)
    @Timed(description = "Time spent serving orders", longTask = true)
    public void serveFirstOrder() throws InterruptedException {
        if (!orders.isEmpty()) {
            Order order = orders.remove(0);
            Thread.sleep((long) (1000L * order.amount));
        }
    }
}
