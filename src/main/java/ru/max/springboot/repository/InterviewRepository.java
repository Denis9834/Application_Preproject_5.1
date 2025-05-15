package ru.max.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.max.springboot.model.Interview;
import ru.max.springboot.model.User;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUser(User user);

    //fuzzy поиск у Admin
    @Query(value = """
            SELECT * FROM interviews
             WHERE organization LIKE '%' || :term || '%'
                OR organization_latin LIKE '%' || :term || '%'
                OR organization % :term
                OR organization_latin % :term
                OR organization LIKE '%' || :termLatin || '%'
                OR organization_latin LIKE '%' || :termLatin || '%'
                OR organization % :termLatin
                OR organization_latin % :termLatin
             ORDER BY
               (organization LIKE '%' || :term || '%'
                OR organization_latin LIKE '%' || :term || '%'
                OR organization LIKE '%' || :termLatin || '%'
                OR organization_latin LIKE '%' || :termLatin || '%') DESC,
               similarity(organization, :term) DESC,
               similarity(organization_latin, :term) DESC,
               similarity(organization, :termLatin) DESC,
               similarity(organization_latin, :termLatin) DESC
            """, nativeQuery = true)
    List<Interview> searchFuzzyAllByOrganization(@Param("term") String term,
                                                 @Param("termLatin") String termLatin);

    //fuzzy поиск у User
    @Query(value = """
        SELECT * FROM interviews
         WHERE user_id = :userId
           AND (
            organization LIKE '%' || :term || '%'
            OR organization_latin LIKE '%' || :term || '%'
            OR organization % :term
            OR organization_latin % :term
            OR organization LIKE '%' || :termLatin || '%'
            OR organization_latin LIKE '%' || :termLatin || '%'
            OR organization % :termLatin
            OR organization_latin % :termLatin
         ORDER BY
           (organization LIKE '%' || :term || '%'
            OR organization_latin LIKE '%' || :term || '%'
            OR organization LIKE '%' || :termLatin || '%'
            OR organization_latin LIKE '%' || :termLatin || '%') DESC,
           similarity(organization, :term) DESC,
           similarity(organization_latin, :term) DESC,
           similarity(organization, :termLatin) DESC,
           similarity(organization_latin, :termLatin) DESC
         """, nativeQuery = true)
    List<Interview> searchFuzzyByUserAllByOrganization(@Param("userId") Long userId,
                                                       @Param("term") String term,
                                                       @Param("termLatin") String termLatin);
}
