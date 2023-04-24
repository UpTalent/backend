package com.uptalent.sponsor.model.request;


import jakarta.validation.constraints.Positive;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IncreaseKudos {
    @Positive
    private int balance;
}
