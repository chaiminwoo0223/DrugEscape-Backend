package gdsc.skhu.drugescape.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {
    USER, ADMIN // ADMIN과 관련된 기능은 나중에 추가하겠습니다.
}