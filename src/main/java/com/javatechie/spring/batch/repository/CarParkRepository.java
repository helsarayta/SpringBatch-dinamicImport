package com.javatechie.spring.batch.repository;

import com.javatechie.spring.batch.entity.CarPark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarParkRepository extends JpaRepository<CarPark,String> {
}
