package com.uptalent.talent.controller;

import com.uptalent.talent.TalentService;
import com.uptalent.talent.model.request.TalentRegistrationRequest;
import com.uptalent.talent.model.res.TalentDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/talents")
public class TalentController {
    private final TalentService talentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<TalentDTO> getAllTalents(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "9") int size){
        return talentService.getAllTalents(page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void registerTalent(@Valid @RequestBody TalentRegistrationRequest talent){
        talentService.addTalent(talent);
    }

}
