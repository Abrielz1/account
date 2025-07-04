package ru.example.account.user.entity;

public enum RoleType {
    // Клиентские роли
    ROLE_CLIENT,

    // Административно-технические роли
    ROLE_ADMIN,
    ROLE_TECH_SUPPORT,

    // --- НОВАЯ БИЗНЕС-ИЕРАРХИЯ ---
    ROLE_MANAGER,
    ROLE_SENIOR_MANAGER,
    ROLE_TOP_MANAGEMENT,

    // --- ИЕРАРХИЯ БЕЗОПАСНОСТИ ---
    ROLE_SECURITY_OFFICER,
    ROLE_SECURITY_SUPERVISOR,
    ROLE_SECURITY_TOP_SUPERVISOR;
}
