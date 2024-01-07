package gdsc.skhu.drugescape.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlackListService {
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public TokenBlackListService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToBlackList(String token) {
        // 토큰과 함께 "blacklist"를 키로 사용하여 Redis에 저장합니다.
        redisTemplate.opsForValue().set("blacklist:" + token, "true", 7, TimeUnit.DAYS); // 만료 기간(7일)
    }

    public boolean isBlackListed(String token) {
        // 토큰이 블랙리스트에 있는지 Redis에서 확인합니다.
        String result = redisTemplate.opsForValue().get("blacklist:" + token);
        return result != null;
    }
}