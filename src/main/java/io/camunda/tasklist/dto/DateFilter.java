package io.camunda.tasklist.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateFilter {
  private OffsetDateTime from;

  private OffsetDateTime to;

  public DateFilter(OffsetDateTime from, OffsetDateTime to) {
    this.from = from;
    this.to = to;
  }
  public DateFilter(LocalDateTime from, LocalDateTime to) {
      this.from = from.atZone(ZoneId.systemDefault()).toOffsetDateTime();
      this.to = to.atZone(ZoneId.systemDefault()).toOffsetDateTime();
  }

  public OffsetDateTime getFrom() {
    return from;
  }

  public void setFrom(OffsetDateTime from) {
    this.from = from;
  }

  public OffsetDateTime getTo() {
    return to;
  }

  public void setTo(OffsetDateTime to) {
    this.to = to;
  }
}
