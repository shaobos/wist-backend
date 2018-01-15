package org.ssb.wist.impl;

import redis.clients.jedis.Jedis;

public class RedisStorage {

  public static Jedis createInstance() {
    return new Jedis("localhost");
  }
}
