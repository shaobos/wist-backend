package org.ssb.wist.impl;

import com.linkedin.restli.common.HttpStatus;
import com.linkedin.restli.common.PatchRequest;
import com.linkedin.restli.server.BasicCollectionResult;
import com.linkedin.restli.server.UpdateResponse;
import com.linkedin.restli.server.annotations.Finder;
import com.linkedin.restli.server.annotations.RestLiCollection;
import com.linkedin.restli.server.annotations.RestMethod;
import com.linkedin.restli.server.resources.CollectionResourceTemplate;
import org.ssb.wist.Word;
import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

@RestLiCollection(name = "words", namespace = "org.ssb.wist")
public class WordsResource extends CollectionResourceTemplate<String, Word> {

  private Jedis jedis = RedisStorage.createInstance();

  @RestMethod.Get
  public Word get(String word) {
    String note = jedis.hget(word, RedisMapper.NOTE);
    String repetition = jedis.hget(word, RedisMapper.REPETITION);

    return new Word().setReviewCount(Integer.valueOf(repetition)).setWordName(word).setWordDef(note);
  }

  @RestMethod.Update
  public UpdateResponse update(String key, Word entity) {
    System.out.println(key);
    if (key != null) {
      int repetitionIndex = Integer.parseInt(jedis.hget(key, RedisMapper.REPETITION));
      String reviewDateStr = jedis.hget(key, RedisMapper.REVIEW_DATE);
      Date todayDate = new Date();

      if (DateUtils.stringToDate(reviewDateStr).after(todayDate)) {
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

      jedis.hset(key, RedisMapper.REPETITION, Integer.toString(repetitionIndex+1));
      jedis.hset(key, RedisMapper.REVIEW_DATE, nextReviewDateStr);
    }
    return new UpdateResponse(HttpStatus.S_200_OK);
  }

  @Finder("wordsOfTheDay")
  public BasicCollectionResult<Word> findWordsOfTheDay() {
    Set<String> allKeys = jedis.keys("*");
    List<Word> response = new ArrayList<Word>();

    for (String wordKey : allKeys) {

      String reviewDateStr = jedis.hget(wordKey, RedisMapper.REVIEW_DATE);
      int repetition = Integer.parseInt(jedis.hget(wordKey, RedisMapper.REPETITION));
      Date today = new Date();

      if (shouldWordBeReviewed(reviewDateStr, today, repetition)) {
        Word word = new Word().setWordName(wordKey);
        String note = jedis.hget(wordKey, RedisMapper.NOTE);
        word.setWordDef(note);
        response.add(word);
      }
    }

    BasicCollectionResult<Word> basicCollectionResult = new BasicCollectionResult<Word>(response, response.size());
    return basicCollectionResult;
  }

  @RestMethod.GetAll
  public BasicCollectionResult findAllWords() {
    Set<String> allKeys = jedis.keys("*");
    List<Word> response = new ArrayList<Word>();

    for (String wordKey : allKeys) {
      Word word = new Word().setWordName(wordKey);
      String repetition = jedis.hget(wordKey, RedisMapper.REPETITION);
      word.setReviewCount(Integer.valueOf(repetition));
      response.add(word);
    }

    return new BasicCollectionResult(response, response.size());
  }


  @RestMethod.PartialUpdate
  public UpdateResponse update(String key, PatchRequest<Word> patch) {
    return new UpdateResponse(HttpStatus.S_200_OK);
  }


  private String addDays(Date currentDate, int days) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(currentDate);
    calendar.setTimeZone(TimeZone.getTimeZone("PST"));
    calendar.add(Calendar.DATE, days);
    return dateFormat.format(calendar.getTime());
  }

  // TODO: convert this to a simple resource
  private boolean shouldWordBeReviewed(String reviewDateStr, Date today, int repetition) {
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
}
