package io.camunda.tasklist.dto;

import java.time.LocalDateTime;

public class DateFilter {
    private LocalDateTime from;

    private LocalDateTime to;
    
    public DateFilter(LocalDateTime from, LocalDateTime to) {
      this.from = from;
      this.to = to;
    }
    
    public LocalDateTime getFrom() {
      return from;
    }

    public void setFrom(LocalDateTime from) {
      this.from = from;
    }

    public LocalDateTime getTo() {
      return to;
    }

    public void setTo(LocalDateTime to) {
      this.to = to;
    }
}
