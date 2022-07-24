package com.kakao.pretest.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

    @Override
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @CacheEvict(allEntries = true, cacheNames = { "QueryKeyword" })
    @Scheduled(fixedDelay = 60 * 1000)
    public void cacheEvict() { }
}
