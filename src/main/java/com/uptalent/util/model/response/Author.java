package com.uptalent.util.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Author {
    private Long id;
    private String name;
    private String avatar;
}