package ru.example.account.security.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.example.account.security.entity.ActiveSessionCache;

@Repository
public interface ActiveSessionCacheRepository extends CrudRepository<ActiveSessionCache, String> {

}
