package com.uptalent.mapper;

import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.response.ProofDetailInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProofMapper {
    ProofDetailInfo toProofDetailInfo(Proof proof);
}
