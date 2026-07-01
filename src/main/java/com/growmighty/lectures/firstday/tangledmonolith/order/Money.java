package com.growmighty.lectures.firstday.tangledmonolith.order;

// Money라는 값 객체(VO) 적용
// Value Object는 값 자체가 중요한 객체이기 때문이다.
// 이 역시 Entity이기 때문에 기본적으로 정적 팩토리 메서드로 구현하는 것을 지향한다.
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Embeddable // 이 선언으로 Money는 엔티티에 포함될 수 있는 값 객체가 되었다.
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {
    private BigDecimal value;

    // 생성자를 private로 잠근다.
    private Money(BigDecimal value) {
        this.value = value;
    }

    // from + static 메서드를 활용한다.
    // 생성 전에 예외 처리로 검증할 수 있다. 불변식을 보장하는 것이다. 생성되는 순간부터 항상 올바른 상태여야 하는 DDD의 철학.
    // 정적 팩토리 메서드는 이름을 붙일 수 있다. 고로, 목적이 드러난다.
    public static Money from(BigDecimal value) {
        Objects.requireNonNull(value, "금액은 null일 수 없습니다.");

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 0원 이상이어야 합니다. 입력값: " + value);
        }

        return new Money(value);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money plus(Money other) {
        return new Money(this.value.add(other.value));
    }

    // 뺄셈은 음수가 나올 수 있으므로 from으로 생성.
    public Money minus(Money other) {
        return Money.from(this.value.subtract(other.value));
    }

    public Money times(int quantity) {
        return new Money(this.value.multiply(BigDecimal.valueOf(quantity)));
    }

    public Money percentage(int percent) {
        BigDecimal amount = this.value
                .multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return new Money(amount);
    }

    public boolean isSameAmount(Money other) {
        return this.value.compareTo(other.value) == 0;
    }
}
