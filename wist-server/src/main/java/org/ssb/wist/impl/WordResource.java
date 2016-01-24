package org.ssb.wist.impl;

import com.linkedin.restli.common.HttpStatus;
import com.linkedin.restli.common.PatchRequest;
import com.linkedin.restli.server.UpdateResponse;
import com.linkedin.restli.server.annotations.RestLiCollection;
import com.linkedin.restli.server.annotations.RestMethod;
import com.linkedin.restli.server.resources.CollectionResourceTemplate;
import org.ssb.wist.Word;

@RestLiCollection(name = "word", namespace = "org.example.fortunes")
public class WordResource extends CollectionResourceTemplate<String, Word> {

  @RestMethod.Update
  public UpdateResponse update(String key, Word entity) {

    System.out.println(key);
    return new UpdateResponse(HttpStatus.S_200_OK);
  }

  @RestMethod.PartialUpdate
  public UpdateResponse update(String key, PatchRequest<Word> patch) {
    System.out.println("niubi");
    return new UpdateResponse(HttpStatus.S_200_OK);
  }

  @Override
  public Word get(String key) {
    System.out.println("Get me. Read me. Analyze me");
    return new Word();
  }
}
