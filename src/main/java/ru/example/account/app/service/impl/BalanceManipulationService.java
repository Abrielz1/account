package ru.example.account.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.example.account.app.entity.Account;
import ru.example.account.app.repository.AccountRepository;
import ru.example.account.app.service.AccountBatchProcessor;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceManipulationService {

    private final AccountRepository accountRepository;

    private final AccountBatchProcessor batchProcessor;

    private final StringRedisTemplate redisTemplate;

    private final RedissonClient redissonClient;

    private static final String JOB_LOCK_KEY = "balance-increase-job:lock";

    private static final String CURSOR_KEY = "balance-increase:cursor:last-processed-id";

    private static final BigDecimal MAX_PERCENT = new BigDecimal("2.07");

    private static final int BATCH_SIZE = 50;

    @Scheduled(fixedRateString = "${app.scheduling.moneyRiser.rate}")
    @CacheEvict(cacheNames = {"users", "accounts"}, allEntries = true, beforeInvocation = true)
    public void increaseBalances() {

        final RLock lock = redissonClient.getLock(JOB_LOCK_KEY);

        boolean lockAcquired = false;

        try {

            lockAcquired = this.tryToAcquireLock(lock);

            if (!lockAcquired) {
                log.info("Could not acquire lock for balance increase job. It might be running on another instance.");
                return;
            }

            log.info("Lock acquired. Starting balance processing cycle.");
            this.runJobCycle();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Balance increase job was interrupted while waiting for a lock.", e);
        } finally {
            if (lockAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released.");
            }
        }
    }

    /**
     * Основной цикл работы, который итерируется по всем пачкам данных.
     */
    private void runJobCycle() {

        long lastProcessedId = readLastProcessedIdFromRedis();

        while (true) {
            log.debug("Fetching next batch of accounts after ID {}.", lastProcessedId);
            List<Account> batch = getNextBatch(lastProcessedId);

            if (batch.isEmpty()) {
                log.info("No more accounts to process. Job cycle finished. Resetting cursor.");
                redisTemplate.delete(CURSOR_KEY);
                break;
            }

            try {
                // Пытаемся обработать пачку. Вся магия с транзакцией и ретраями - внутри.
                batchProcessor.processAccounts(batch);

                // Если успешно - обновляем курсор для СЛЕДУЮЩЕЙ итерации.
                lastProcessedId = batch.get(batch.size() - 1).getId();
                this.updateLastProcessedIdInRedis(lastProcessedId);

                log.info("Batch processed successfully. Cursor advanced to ID {}.", lastProcessedId);

            } catch (Exception e) {
                // Если batchProcessor выбросил финальное исключение после всех ретраев,
                // мы просто логируем это и выходим из цикла.
                // Курсор в Redis НЕ БУДЕТ обновлен.
                log.error("Fatal error processing batch starting after ID {}. Stopping job. " +
                        "Next run will retry from this point. Error: {}", lastProcessedId, e.getMessage());
                break;
            }
        }
    }

    /**
     * Пытается захватить распределенную блокировку в цикле.
     */
    private boolean tryToAcquireLock(RLock lock) throws InterruptedException {
        int attempts = 0;
        final int maxLockAttempts = 3;
        while (attempts < maxLockAttempts) {
            // leaseTime 25 секунд - чтобы лок не "завис" навсегда, если инстанс упадет.
            // Ждем до 2 секунд.
            if (lock.tryLock(2, 25, TimeUnit.SECONDS)) {
                return true;
            }
            attempts++;
            log.warn("Could not acquire job lock, attempt {}/{}. Waiting...", attempts, maxLockAttempts);
        }
        return false;
    }

    private List<Account> getNextBatch(long lastProcessedId) {
        PageRequest pageRequest = PageRequest.of(0, BATCH_SIZE);
        Slice<Account> nextSlice = accountRepository.getNextBatch(lastProcessedId, MAX_PERCENT, pageRequest);
        return nextSlice.getContent();
    }

    private long readLastProcessedIdFromRedis() {
        String lastIdStr = redisTemplate.opsForValue().get(CURSOR_KEY);
        return (lastIdStr != null) ? Long.parseLong(lastIdStr) : 0L;
    }

    private void updateLastProcessedIdInRedis(long newId) {
        redisTemplate.opsForValue().set(CURSOR_KEY, String.valueOf(newId));
    }
}