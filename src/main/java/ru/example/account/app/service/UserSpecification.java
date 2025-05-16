package ru.example.account.app.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.example.account.app.entity.EmailData;
import ru.example.account.app.entity.PhoneData;
import ru.example.account.app.entity.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class UserSpecification implements Specification<User> {

    private final LocalDate dateOfBirth;
    private final String phone;
    private final String name;
    private final String email;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();


        if (dateOfBirth != null) {
            predicates.add(cb.greaterThan(root.get("dateOfBirth"), dateOfBirth));
        }

        if (StringUtils.hasText(name)) {
            predicates.add(cb.like(root.get("username"), name + "%"));
        }

        if (StringUtils.hasText(phone)) {
            Join<User, PhoneData> phoneData = root.join("userPhones", JoinType.INNER);
            predicates.add(cb.equal(phoneData.get("phone"), phone));
        }

        if (StringUtils.hasText(email)) {
            Join<User, EmailData> userEmailData = root.join("userEmails", JoinType.INNER);
            predicates.add(cb.equal(userEmailData.get("email"), email));
        }

        query.distinct(true);
        return cb.and(predicates.toArray(predicates.toArray(new Predicate[0])));
    }
}
