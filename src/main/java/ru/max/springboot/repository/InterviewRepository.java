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
         WHERE lower(organization) LIKE '%' || lower(:term) || '%'
            OR lower(organization_latin) LIKE '%' || lower(:term) || '%'
            OR lower(organization) % lower(:term)
            OR lower(organization_latin) % lower(:term)
            OR lower(organization) LIKE '%' || lower(:termLatin) || '%'
            OR lower(organization_latin) LIKE '%' || lower(:termLatin) || '%'
            OR lower(organization) % lower(:termLatin)
            OR lower(organization_latin) % lower(:termLatin)
         ORDER BY
           (lower(organization) LIKE '%' || lower(:term) || '%'
            OR lower(organization_latin) LIKE '%' || lower(:term) || '%'
            OR lower(organization) LIKE '%' || lower(:termLatin) || '%'
            OR lower(organization_latin) LIKE '%' || lower(:termLatin) || '%') DESC,
           similarity(lower(organization), lower(:term)) DESC,
           similarity(lower(organization_latin), lower(:term)) DESC,
           similarity(lower(organization), lower(:termLatin)) DESC,
           similarity(lower(organization_latin), lower(:termLatin)) DESC
         """, nativeQuery = true)
    List<Interview> searchFuzzyAllByOrganization(@Param("term") String term,
                                                 @Param("termLatin") String termLatin);

    //fuzzy поиск у User
    @Query(value = """
        SELECT * FROM interviews
         WHERE user_id = :userId
           AND (
             lower(organization) LIKE '%' || lower(:term) || '%'
             OR lower(organization_latin) LIKE '%' || lower(:term) || '%'
             OR lower(organization) % lower(:term)
             OR lower(organization_latin) % lower(:term)
             OR lower(organization) LIKE '%' || lower(:termLatin) || '%'
             OR lower(organization_latin) LIKE '%' || lower(:termLatin) || '%'
             OR lower(organization) % lower(:termLatin)
             OR lower(organization_latin) % lower(:termLatin)
           )
         ORDER BY
           (lower(organization) LIKE '%' || lower(:term) || '%'
            OR lower(organization_latin) LIKE '%' || lower(:term) || '%'
            OR lower(organization) LIKE '%' || lower(:termLatin) || '%'
            OR lower(organization_latin) LIKE '%' || lower(:termLatin) || '%') DESC,
           similarity(lower(organization), lower(:term)) DESC,
           similarity(lower(organization_latin), lower(:term)) DESC,
           similarity(lower(organization), lower(:termLatin)) DESC,
           similarity(lower(organization_latin), lower(:termLatin)) DESC
         """, nativeQuery = true)
    List<Interview> searchFuzzyByUserAllByOrganization(@Param("userId") Long userId,
                                                       @Param("term") String term,
                                                       @Param("termLatin") String termLatin);
}
