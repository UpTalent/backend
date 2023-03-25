package com.uptalent.talent.controller;

import com.uptalent.filestore.FileStoreOperation;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.talent.TalentService;
import com.uptalent.talent.model.request.TalentEditRequest;
import com.uptalent.talent.model.request.TalentLoginRequest;
import com.uptalent.talent.model.request.TalentRegistrationRequest;
import com.uptalent.talent.model.response.TalentDTO;
import com.uptalent.talent.model.response.TalentOwnProfileDTO;
import com.uptalent.talent.model.response.TalentProfileDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.uptalent.jwt.JwtConstant.JWT_TOKEN_HEADER_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/talents")
public class TalentController {
    private final TalentService talentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<TalentDTO> getAllTalents(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "9") int size){
        return talentService.getAllTalents(page, size);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TalentProfileDTO getTalentProfile(@PathVariable Long id){
        return talentService.getTalentProfileById(id);
    }

    @PostMapping
    public ResponseEntity<?> registerTalent(@Valid @RequestBody TalentRegistrationRequest talent){
        var response = talentService.addTalent(talent);
        var headers = setJwtToHeader(response.jwtToken());

        return new ResponseEntity<>(response, headers, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody TalentLoginRequest loginRequest){
        var response = talentService.login(loginRequest);
        var headers = setJwtToHeader(response.jwtToken());

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TalentOwnProfileDTO updateTalent(@PathVariable Long id,
                                            @Valid @RequestBody TalentEditRequest updatedTalent){
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

    private HttpHeaders setJwtToHeader(String token) {
        HttpHeaders headers = new HttpHeaders();

        headers.add(JWT_TOKEN_HEADER_NAME, token);

        return headers;
    }

}
