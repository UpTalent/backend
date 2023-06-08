package com.uptalent.proof.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorProof {
    private Long id;
    private String lastname;
    private String firstname;
    private String avatar;
}
