package com.liewmanchoi.client;

import org.junit.Test;

public class ClientTest {

  @Test
  public void run() {
    Client client = new Client();
    client.run("http://127.0.0.1", System.out::println);
  }
}
