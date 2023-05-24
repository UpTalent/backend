package com.uptalent.talent.repository;

import com.uptalent.talent.model.entity.Talent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface TalentRepository extends JpaRepository<Talent, Long> {
    @Query("SELECT t from talent t where t.credentials.verified = TRUE ORDER BY t.id DESC")
    Page<Talent> findAllByOrderByIdDesc(Pageable pageable);

    @Query("SELECT t.avatar FROM talent t WHERE t.id = :talentId")
    Optional<String> findAvatarByTalentId(Long talentId);

    @Query("SELECT t.banner FROM talent t WHERE t.id = :talentId")
    Optional<String> findBannerByTalentId(Long talentId);

    @Query("select t from talent t where " +
            "coalesce((SELECT count(sk) FROM t.skills sk WHERE sk.name IN :skills GROUP BY t.id), 0) = :skillsSize ")
    Page<Talent> filterAllBySkills(String [] skills, int skillsSize, PageRequest of);

    @Query("select sum(p.kudos) from proof p join talent t on t.id = p.talent.id where t.id = :talentId")
    Long getTotalCountKudosByTalentId(Long talentId);

    @Query("SELECT t FROM talent t WHERE t.id = :id AND t.credentials.verified = TRUE")
    Optional<Talent> findByIdAndCredentialsVerified(Long id);

    @Modifying(clearAutomatically=true, flushAutomatically=true)
    @Query(value = "UPDATE talent t " +
            "SET t.avatar=NULL," +
            "t.firstname='Deleted', t.lastname='Talent', t.banner=NULL " +
            "WHERE t.id IN :ids")
    void updateTalentDeleteData(List<Long> ids);

}
