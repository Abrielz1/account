package ru.example.account.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.example.account.app.entity.Account;
import ru.example.account.app.repository.AccountRepository;
import ru.example.account.app.service.AccountBatchProcessor;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceManipulationService {

    private final AccountRepository accountRepository;
    private final AccountBatchProcessor batchProcessor;

    private static final BigDecimal MAX_PERCENT = new BigDecimal("2.07");
    private static final int BATCH_SIZE = 50;

    /**
     * Запускает фоновую задачу по начислению процентов на все подходящие счета.
     * Использует keyset-пагинацию внутри цикла 'for' для эффективной обработки.
     * Цикл имеет одну точку выхода, что соответствует требованиям чистого кода.
     */
    @Scheduled(fixedRateString = "${app.scheduling.moneyRiser.rate:30000}")
    @CacheEvict(cacheNames = {"users", "accounts"}, allEntries = true, beforeInvocation = true)
    public void increaseBalances() {
        log.info("Starting balance increase job (keyset pagination with for-loop).");

        long lastProcessedId = 0L;

        // Элегантный цикл for. Он будет продолжаться до тех пор, пока `getNextBatch` возвращает непустой список.
        for (
                List<Account> batch = getNextBatch(lastProcessedId);
                !batch.isEmpty(); // <-- Единственное условие выхода из цикла
                batch = getNextBatch(lastProcessedId)
        ) {
            log.debug("Processing batch of {} accounts, starting after ID {}.", batch.size(), lastProcessedId);

            try {
                // Обрабатываем текущую пачку
                batchProcessor.processAccounts(batch);

                // Обновляем курсор для СЛЕДУЮЩЕЙ итерации цикла for
                lastProcessedId = batch.get(batch.size() - 1).getId();

            } catch (Exception e) {
                log.error("Fatal error while processing batch after ID {}. Stopping job. Error: {}", lastProcessedId, e.getMessage(), e);
                break; // <--  это аварийный выход из-за исключения.

            }
        }

        log.info("Balance increase job run has been completed.");
    }

    /**
     * Вспомогательный приватный метод для получения следующей пачки данных.
     * Это делает основной цикл более читаемым.
     */
    private List<Account> getNextBatch(long lastProcessedId) {
        PageRequest pageRequest = PageRequest.of(0, BATCH_SIZE);
        Slice<Account> nextSlice = accountRepository.getNextBatch(lastProcessedId, MAX_PERCENT, pageRequest);
        return nextSlice.getContent();
    }
}