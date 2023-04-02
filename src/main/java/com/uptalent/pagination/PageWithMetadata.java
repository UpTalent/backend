package com.uptalent.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
public class PageWithMetadata<T> {
    private List<T> content;
    private int totalPages;
}
