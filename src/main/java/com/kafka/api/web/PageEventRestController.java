package com.kafka.api.web;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kafka.api.entities.PageEvent;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class PageEventRestController {
	
	private final StreamBridge streamBridge;
	private final InteractiveQueryService interactiveQueryService;
	
	PageEventRestController(StreamBridge streamBridge, InteractiveQueryService interactiveQueryService){
		this.streamBridge = streamBridge;
		this.interactiveQueryService = interactiveQueryService;
	}
	
	@GetMapping("/publish/{topic}/{name}")
	public PageEvent publish(@PathVariable String topic, @PathVariable String name) {
		PageEvent pageEvent = new PageEvent(name, Math.random()>0.5?"U1":"U2", new Date(), new Random().nextInt(9000));
		streamBridge.send(topic, pageEvent);
		return pageEvent;
	}

	@GetMapping(path="/analytics", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Map<String, Long>> analytics() {
		return Flux.interval(Duration.ofSeconds(1))
				.map(sequence -> {
					Map<String, Long> stringLongMap = new HashMap<>();
					//le stream sur le bean utilise windowstore c pour ça que ici aussi
					ReadOnlyWindowStore<String, Long> windowStore = interactiveQueryService.getQueryableStore("page-count", QueryableStoreTypes.windowStore());
					Instant now = Instant.now();
					Instant from = now.minusMillis(5000);
					KeyValueIterator<Windowed<String>, Long> fetchAll = windowStore.fetchAll(from, now);
					while(fetchAll.hasNext()) {
						KeyValue<Windowed<String>, Long> next = fetchAll.next();
						stringLongMap.put(next.key.key(), next.value);
					}
					return stringLongMap;
				}).share();
	}
}
