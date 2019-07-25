package com.liewmanchoi.service.impl;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@EmbeddedKafka
public class ProducerServiceTest {
  @ClassRule public static EmbeddedKafkaRule rule = new EmbeddedKafkaRule(1, true, "PigeonPush");

  @Test
  public void push() {}
}
