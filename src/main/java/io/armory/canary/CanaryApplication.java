package io.armory.canary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Random;

@SpringBootApplication
public class CanaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(CanaryApplication.class, args);
	}

	private BeerService beerService;

	public CanaryApplication(BeerService beerService) {
		this.beerService = beerService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void orderBeers() {
		Flux.interval(Duration.ofSeconds(10))
				.map(CanaryApplication::toOrder)
				.doOnEach(o -> beerService.orderBeer(o.get()))
				.subscribe();
	}

	private static Order toOrder(Long l) {
		System.out.println("Order input = " + l);
		double amount = l % 5;
		System.out.println("Order amount = " + amount);
		String[] types = {"hazyIPA", "IPA", "doubleIPA"};
		String type = types[new Random().nextInt(types.length)];
		System.out.println("Order type = " + type);
		return new Order(amount, type);
	}

}
