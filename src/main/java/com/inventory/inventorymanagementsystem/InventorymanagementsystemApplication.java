package com.inventory.inventorymanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InventorymanagementsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventorymanagementsystemApplication.class, args);
	}

}
