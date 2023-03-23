package com.uptalent.talent.controller;

import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.payload.HttpResponse;
import com.uptalent.talent.TalentService;
import com.uptalent.talent.exception.DeniedAccessException;
import com.uptalent.talent.exception.TalentExistsException;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.request.TalentEditRequest;
import com.uptalent.talent.model.request.TalentLoginRequest;
import com.uptalent.talent.model.request.TalentRegistrationRequest;
import com.uptalent.talent.model.response.TalentDTO;
import com.uptalent.talent.model.response.TalentProfileDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    public Talent updateTalent(@PathVariable Long id,
                               @Valid @RequestBody TalentEditRequest updatedTalent){
        return talentService.updateTalent(id, updatedTalent);
    }

    private HttpHeaders setJwtToHeader(String token) {
        HttpHeaders headers = new HttpHeaders();

        headers.add(JWT_TOKEN_HEADER_NAME, token);

        return headers;
    }

    //TODO: create a separate class for exception handling
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(TalentNotFoundException.class)
    public HttpResponse handlerNotFoundTalentException(TalentNotFoundException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(TalentExistsException.class)
    public HttpResponse handlerExistsTalentException(TalentExistsException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(DeniedAccessException.class)
    public HttpResponse handlerExistsTalentException(DeniedAccessException e) {
        return new HttpResponse(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleFieldsException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }
}
