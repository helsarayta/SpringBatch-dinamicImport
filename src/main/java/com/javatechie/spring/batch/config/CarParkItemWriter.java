package com.javatechie.spring.batch.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.spring.batch.entity.CarPark;
import com.javatechie.spring.batch.repository.CarParkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CarParkItemWriter implements ItemWriter<CarPark> {

    @Autowired
    private CarParkRepository repository;

    @Autowired
    private WebClient.Builder webClient;

    @Value("${url.base.convert}")
    String urlBaseConvert;

    @Override
    public void write(List<? extends CarPark> list) throws Exception {
        System.out.println("Writer Thread "+Thread.currentThread().getName());
        repository.saveAll(list);
    }
}
