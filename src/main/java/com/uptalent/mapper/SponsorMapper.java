package com.uptalent.mapper;

import com.uptalent.sponsor.model.entity.Sponsor;

import com.uptalent.sponsor.model.response.SponsorProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SponsorMapper {
    static SponsorProfile toSponsorProfile(Sponsor sponsor) {
        return new SponsorProfile(
                sponsor.getId(),
                sponsor.getFullname(),
                sponsor.getAvatar(),
                sponsor.getCredentials().getEmail(),
                sponsor.getKudos()
        );
    }
}
