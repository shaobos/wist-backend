package org.ssb.wist.impl;

import com.linkedin.data.template.StringMap;
import com.linkedin.restli.common.HttpStatus;
import com.linkedin.restli.common.PatchRequest;
import com.linkedin.restli.server.UpdateResponse;
import com.linkedin.restli.server.annotations.RestLiCollection;
import com.linkedin.restli.server.resources.CollectionResourceTemplate;
import org.example.fortunes.WordsOfToday;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestLiCollection(name = "wordsOfToday", namespace = "org.example.fortunes")
public class WordsOfTodayResource extends CollectionResourceTemplate<Long, WordsOfToday> {


  public WordsOfToday get(Long key){
    Jedis jedis = new Jedis("localhost");
    Set<String> allKeys = jedis.keys("*");
    Map<String, String> response = new HashMap();

    for (String word : allKeys) {
      System.out.println("Word is " + word);
      String values = jedis.hget(word, "note");
      response.put(word, values);
    }

    return new WordsOfToday().setWordsOfToday(new StringMap(response));
  }

  @Override
  public UpdateResponse update(Long key, WordsOfToday entity) {
    System.out.println("update, key, entity");
    return new UpdateResponse(HttpStatus.S_202_ACCEPTED);
  }

  @Override
  public UpdateResponse update(Long key, PatchRequest<WordsOfToday> patch) {
    return super.update(key, patch);
  }
}
