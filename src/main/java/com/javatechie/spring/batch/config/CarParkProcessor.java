package com.javatechie.spring.batch.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.spring.batch.entity.CarPark;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
@Slf4j
public class CarParkProcessor implements ItemProcessor<CarPark, CarPark> {
    @Autowired
    private WebClient.Builder webClient;

    @Value("${url.base.convert}")
    String urlBaseConvert;
    @Override
    public CarPark process(CarPark carPark) throws JsonProcessingException {
        log.info("CAR PARK 1 => {}", carPark);
        String convertGeo = webClient.build()
                .get()
                .uri(urlBaseConvert + "/commonapi/convert/3857to4326?Y=" + carPark.getYCoord() + "&X=" + carPark.getXCoord())
                .retrieve()
                .bodyToMono(String.class).block();
        Map<String, Object> req = new ObjectMapper().readValue(convertGeo, Map.class);
        carPark.setXCoord(Float.valueOf(req.get("latitude").toString()));
        carPark.setYCoord(Float.valueOf(req.get("longitude").toString()));
        log.info("CAR PARK 2 => {}", carPark);

        return carPark;
    }
}
