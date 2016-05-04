package org.ssb.wist.impl;

import com.linkedin.data.template.StringMap;
import com.linkedin.restli.common.HttpStatus;
import com.linkedin.restli.common.PatchRequest;
import com.linkedin.restli.server.UpdateResponse;
import com.linkedin.restli.server.annotations.RestLiCollection;
import com.linkedin.restli.server.resources.CollectionResourceTemplate;
import org.ssb.wist.WordsOfToday;
import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestLiCollection(name = "wordsOfToday", namespace = "org.ssb.wist")
public class WordsOfTodayResource extends CollectionResourceTemplate<Long, WordsOfToday> {
  // TODO: convert this to a simple resource
  boolean shouldWordBeReviewed(String reviewDateStr, Date today, int repetition) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date reviewDate;

    if (RepetitionInterval.intervals.size() == repetition) {
      System.out.println("Word " + key + " exceeded maximum review times. No need to review again");
      return false;
    }

    try {
      reviewDate = simpleDateFormat.parse(reviewDateStr);
      if (today.after(reviewDate)) {
        return true;
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return false;
  }

  public WordsOfToday get(Long key) {
    Jedis jedis = new Jedis("localhost");
    Set<String> allKeys = jedis.keys("*");
    Map<String, String> response = new HashMap();

    for (String word : allKeys) {
      System.out.println("Word is " + word);

      String reviewDateStr = jedis.hget(word, "review_date");
      int repetition = Integer.parseInt(jedis.hget(word, "repetition"));
      Date today = new Date();
      if (shouldWordBeReviewed(reviewDateStr, today, repetition)) {
        System.out.println("Word " + " is considered today of word");
        String values = jedis.hget(word, "note");
        response.put(word, values);
      }
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
