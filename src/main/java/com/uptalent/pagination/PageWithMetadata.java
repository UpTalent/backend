package com.uptalent.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
public class PageWithMetadata<T> {
    private List<T> content;
    private int totalPages;
}
