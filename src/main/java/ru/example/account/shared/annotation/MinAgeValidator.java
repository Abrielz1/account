package ru.example.account.shared.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.Period;

@Slf4j
public class MinAgeValidator implements ConstraintValidator<MinAge, LocalDate> {

    private int minAge;

    @Override
    public void initialize(MinAge constraintAnnotation) {
        this.minAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {

        if (birthDate == null) {
            log.debug("[DEBUG] supplied userBirthDate must not be null!");
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Date of birth is required"
            ).addConstraintViolation();

            return false;
        }

        int age = Period.between(birthDate, LocalDate.now()).getYears();

        if (age < minAge) {

            StringBuilder message = new StringBuilder();

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message.append("Client must be at least ")
                    .append(minAge)
                    .append(" years old")
                            .toString())
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}
