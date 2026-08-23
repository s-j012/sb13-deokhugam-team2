package com.deokhugam.book.external.ocr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OcrSpaceResponse(
    @JsonProperty("ParsedResults")
    List<ParsedResult> parsedResults,

    @JsonProperty("IsErroredOnProcessing")
    boolean erroredOnProcessing,

    @JsonProperty("ErrorMessage")
    Object errorMessage
) {

  public record ParsedResult(
      @JsonProperty("ParsedText")
      String parsedText
  ) {
  }
}
