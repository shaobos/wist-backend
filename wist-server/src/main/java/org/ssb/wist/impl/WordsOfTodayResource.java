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
@Deprecated
public class WordsOfTodayResource extends CollectionResourceTemplate<Long, WordsOfToday> {

  private Jedis jedis = new Jedis("localhost");

  // TODO: convert this to a simple resource
  boolean shouldWordBeReviewed(String reviewDateStr, Date today, int repetition) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date reviewDate;

    if (RepetitionInterval.intervals.size() == repetition) {
      System.out.println("Word exceeded maximum review times. No need to review again");
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

  /**
   *
   * @param key
   * @return
   */
  public WordsOfToday get(Long key) {
    Set<String> allKeys = jedis.keys("*");
    Map<String, String> wordsOfTodayMap = new HashMap();

    for (String word : allKeys) {
      String reviewDateStr = jedis.hget(word, "review_date");
      int repetition = Integer.parseInt(jedis.hget(word, "repetition"));
      Date today = new Date();
      if (shouldWordBeReviewed(reviewDateStr, today, repetition)) {
        String values = jedis.hget(word, "note");
        wordsOfTodayMap.put(word, values);
      }
    }

    return new WordsOfToday().setWordsOfToday(new StringMap(wordsOfTodayMap));
  }

  @Override
  public UpdateResponse update(Long key, WordsOfToday entity) {
    return new UpdateResponse(HttpStatus.S_202_ACCEPTED);
  }

  @Override
  public UpdateResponse update(Long key, PatchRequest<WordsOfToday> patch) {
    return super.update(key, patch);
  }
}
