package com.techprimers.reactive.reactivemongoexample1;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties="isTesting=true")
@EnableConfigurationProperties
public class PredictionBasedPrototypeApplicationTests {

	@Test
	public void contextLoads() {
	}

}
