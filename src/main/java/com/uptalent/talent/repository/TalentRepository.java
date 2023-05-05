package com.uptalent.talent.repository;

import com.uptalent.talent.model.entity.Talent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Repository
public interface TalentRepository extends JpaRepository<Talent, Long> {
    Page<Talent> findAllByOrderByIdDesc(Pageable pageable);

    @Query("SELECT t.avatar FROM talent t WHERE t.id = :talentId")
    Optional<String> findAvatarByTalentId(Long talentId);

    @Query("SELECT t.banner FROM talent t WHERE t.id = :talentId")
    Optional<String> findBannerByTalentId(Long talentId);

    @Query("select t from talent t where " +
            "coalesce((SELECT count(sk) FROM t.skills sk WHERE sk.name IN :skills GROUP BY t.id), 0) = :skillsSize ")
    Page<Talent> filterAllBySkills(String [] skills, int skillsSize, PageRequest of);

}
