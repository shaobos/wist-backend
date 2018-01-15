package org.ssb.wist.impl;

import com.linkedin.data.template.StringMap;
import com.linkedin.restli.server.annotations.RestLiSimpleResource;
import com.linkedin.restli.server.resources.SimpleResourceTemplate;
import org.ssb.wist.AllWords;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestLiSimpleResource(name = "allWords", namespace = "org.ssb.wist")
@Deprecated
public class AllWordsResource extends SimpleResourceTemplate<AllWords> {

  @Override
  public AllWords get() {
    Jedis jedis = new Jedis("localhost");
    Set<String> allKeys = jedis.keys("*");
    Map<String, String> response = new HashMap();

    for (String word : allKeys) {
      System.out.println("Word is " + word);

      String repetition = jedis.hget(word, "repetition");
      response.put(word, repetition);
    }

    return new AllWords().setAllWords(new StringMap(response));
  }
}
