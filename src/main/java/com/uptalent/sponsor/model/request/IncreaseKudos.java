package com.uptalent.sponsor.model.request;


import jakarta.validation.constraints.Min;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IncreaseKudos {
    @Min(value = 0, message = "Kudos should be greater or equals 0")
    private int balance;
}
