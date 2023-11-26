package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Address {

    private String city;

    private String street;

    private String zipcode;

    protected Address() {

    }

    /**
     * 값 타입은 변경 불가능하게 설계해야한다.
     * 생성자에서 초기화하고, 변경 불가능한 클래스로 제공
     * JPA 스펙상 엔티티나 임베디드 타입은 자바 기본 생성자 또는 protected로 설정해야한다.
     * public < protected 로 설정하여 개발자가 인지 할 수 있도록 하는게 안전하다.
     */

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
