package io.camunda.tasklist.util;

import io.camunda.tasklist.dto.Pagination;

public class PaginationUtil {
  public static Pagination createPagination(Integer pageSize) {
    return new Pagination().setPageSize(pageSize);
  }
}
