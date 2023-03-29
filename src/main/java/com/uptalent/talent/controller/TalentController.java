package com.uptalent.talent.controller;

import com.uptalent.filestore.FileStoreOperation;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.talent.TalentService;
import com.uptalent.talent.model.request.TalentEdit;
import com.uptalent.talent.model.request.TalentLogin;
import com.uptalent.talent.model.request.TalentRegistration;
import com.uptalent.talent.model.response.TalentGeneralInfo;
import com.uptalent.talent.model.response.TalentOwnProfile;
import com.uptalent.talent.model.response.TalentProfile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/talents")
public class TalentController {
    private final TalentService talentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<TalentGeneralInfo> getAllTalents(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "9") int size){
        return talentService.getAllTalents(page, size);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TalentProfile getTalentProfile(@PathVariable Long id){
        return talentService.getTalentProfileById(id);
    }

    @PostMapping
    public ResponseEntity<?> registerTalent(@Valid @RequestBody TalentRegistration talent){
        var response = talentService.addTalent(talent);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody TalentLogin loginRequest){
        var response = talentService.login(loginRequest);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TalentOwnProfile updateTalent(@PathVariable Long id,
                                         @Valid @RequestBody TalentEdit updatedTalent){
        return talentService.updateTalent(id, updatedTalent);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTalent(@PathVariable Long id) {
        talentService.deleteTalent(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(
            path = "/{id}/image/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public void uploadImage(@PathVariable Long id,
                            @RequestParam MultipartFile image,
                            @RequestParam FileStoreOperation operation){
        talentService.uploadImage(id, image, operation);
    }


}
