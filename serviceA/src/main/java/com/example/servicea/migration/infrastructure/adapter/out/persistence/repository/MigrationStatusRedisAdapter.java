package com.example.servicea.migration.infrastructure.adapter.out.persistence.repository;

import com.example.servicea.migration.application.port.out.MigrationStatusPersistencePort;
import com.example.servicea.migration.domain.model.BackFillStatus;
import com.example.servicea.migration.domain.model.ValidationStatus;
import com.example.servicea.migration.infrastructure.adapter.out.persistence.mapper.BackFillStatusRedisMapper;
import com.example.servicea.migration.infrastructure.adapter.out.persistence.mapper.ValidationStatusRedisMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class MigrationStatusRedisAdapter implements MigrationStatusPersistencePort {
    private static final String BACKFILL_KEY = "service-a:backfill:instance";
    private static final String VALIDATION_KEY = "service-a:validation:statuses";
    private static final String VALIDATION_JOB_LOCK_KEY = "service-a:validation-job:lock";

    private static final int MAX_VALIDATION_STATUSES = 10;
    private static final Duration BACK_FILL_TTL = Duration.ofMinutes(5);
    private static final Duration VALIDATION_JOB_LOCK_TTL = Duration.ofSeconds(10);

    private static final DefaultRedisScript<Long> RELEASE_VALIDATION_JOB_LOCK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private static final DefaultRedisScript<Long> SAVE_VALIDATION_STATUS_SCRIPT = new DefaultRedisScript<>("""
            local count = redis.call('SCARD', KEYS[1])
            local maxStatuses = tonumber(ARGV[1])
            local spaceLeft = maxStatuses - count
            
            if spaceLeft <= 0 then
                return 0
            end
            
            local lastIndex = math.min(#ARGV, 2 + spaceLeft)
            local added = redis.call('SADD', KEYS[1], unpack(ARGV, 3, lastIndex))
            
            if added > 0 then
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))
            end
            
            return added
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final BackFillStatusRedisMapper backFillStatusMapper;
    private final ValidationStatusRedisMapper validationStatusMapper;

    @Override
    public List<BackFillStatus> findAllBackFillStatuses() {
        return redisTemplate.opsForHash().values(BACKFILL_KEY).stream()
                .map(String.class::cast)
                .map(backFillStatusMapper::deserialize)
                .toList();
    }

    @Override
    public void save(String instanceId, BackFillStatus status) {
        redisTemplate.opsForHash().put(BACKFILL_KEY, instanceId, backFillStatusMapper.serialize(status));
        redisTemplate.expire(BACKFILL_KEY, BACK_FILL_TTL);
    }

    @Override
    public List<ValidationStatus> findAllValidationStatuses() {
        Set<String> statuses = redisTemplate.opsForSet().members(VALIDATION_KEY);
        if (statuses == null) {
            return List.of();
        }
        return statuses.stream().map(validationStatusMapper::deserialize).toList();
    }

    @Override
    public void saveAll(List<ValidationStatus> statuses) {
        if (statuses.isEmpty()) {
            return;
        }

        Object[] scriptArguments = Stream.concat(
                Stream.of(
                        String.valueOf(MAX_VALIDATION_STATUSES),
                        String.valueOf(BACK_FILL_TTL.toSeconds())
                ),
                statuses.stream().map(validationStatusMapper::serialize)
        ).toArray();

        redisTemplate.execute(
                SAVE_VALIDATION_STATUS_SCRIPT,
                List.of(VALIDATION_KEY),
                scriptArguments);
    }

    @Override
    public void deleteAllValidationStatuses() {
        redisTemplate.delete(VALIDATION_KEY);
    }

    @Override
    public boolean lock(UUID id) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                VALIDATION_JOB_LOCK_KEY,
                id.toString(),
                VALIDATION_JOB_LOCK_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public void unlock(UUID id) {
        redisTemplate.execute(
                RELEASE_VALIDATION_JOB_LOCK_SCRIPT,
                List.of(VALIDATION_JOB_LOCK_KEY),
                id.toString());
    }
}
