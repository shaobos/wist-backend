package org.ssb.wist.impl;

import com.linkedin.restli.common.HttpStatus;
import com.linkedin.restli.common.PatchRequest;
import com.linkedin.restli.server.UpdateResponse;
import com.linkedin.restli.server.annotations.RestLiCollection;
import com.linkedin.restli.server.annotations.RestMethod;
import com.linkedin.restli.server.resources.CollectionResourceTemplate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.ssb.wist.Word;
import redis.clients.jedis.Jedis;

@RestLiCollection(name = "word", namespace = "org.ssb.wist")
public class WordResource extends CollectionResourceTemplate<String, Word> {

  private Jedis jedis = new Jedis("localhost");

  private String addDays(Date currentDate, int days) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(currentDate);
    calendar.setTimeZone(TimeZone.getTimeZone("PST"));
    calendar.add(Calendar.DATE, days);
    return dateFormat.format(calendar.getTime());
  }

  @RestMethod.Update
  public UpdateResponse update(String key, Word entity) {
    System.out.println(key);
    if (key != null) {
      int repetitionIndex = Integer.parseInt(jedis.hget(key, "repetition"));
      String reviewDateStr = jedis.hget(key, "review_date");
      Date todayDate = new Date();

      if (DateUtils.stringToDate(reviewDateStr).after(todayDate)) {
        // TODO:
        System.out.println("Review date of word " + key + " is in the future(" + reviewDateStr + ") but a request has been " +
          "received to update this word.");
        return new UpdateResponse(HttpStatus.S_400_BAD_REQUEST);
      }

      if (RepetitionInterval.intervals.size() == repetitionIndex) {
        System.out.println("Word " + key + " exceeded maximum review times. It graduated!!");
        return new UpdateResponse(HttpStatus.S_200_OK);
      }

      // return the next interval given the index
      int nextInterval = RepetitionInterval.intervals.get(repetitionIndex);
      String nextReviewDateStr = addDays(todayDate, nextInterval);

      jedis.hset(key, "repetition", Integer.toString(repetitionIndex+1));
      jedis.hset(key, "review_date", nextReviewDateStr);
    }
    return new UpdateResponse(HttpStatus.S_200_OK);
  }

  @RestMethod.PartialUpdate
  public UpdateResponse update(String key, PatchRequest<Word> patch) {
    return new UpdateResponse(HttpStatus.S_200_OK);
  }

  @Override
  public Word get(String word) {
    String note = jedis.hget(word, "note");
    String repetition = jedis.hget(word, "repetition");

    return new Word().setReviewCount(Integer.valueOf(repetition)).setWordName(word).setWordDef(note);
  }
}
