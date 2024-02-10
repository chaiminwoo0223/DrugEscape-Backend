package gdsc.skhu.drugescape.jwt;

import gdsc.skhu.drugescape.domain.dto.TokenDTO;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStorage {
    private final ConcurrentHashMap<String, TokenDTO> tokenStore = new ConcurrentHashMap<>();

    public void save(String sessionToken, TokenDTO tokenDTO) {
        tokenStore.put(sessionToken, tokenDTO);
    }

    public TokenDTO retrieve(String sessionToken) {
        return tokenStore.remove(sessionToken); // 일회용으로 처리하기 위해 조회 후 삭제
    }
}