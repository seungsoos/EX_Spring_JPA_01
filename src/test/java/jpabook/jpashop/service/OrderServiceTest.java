package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.exception.NotEnoughStockException;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    EntityManager em;
    @Autowired
    OrderRepository orderRepository;
    @Test
    public void 상품주문() {
        //given
        Member member = createMember();
        Book book = createBook("JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order order = orderRepository.findOne(orderId);

        Assertions.assertEquals(OrderStatus.ORDER, order.getStatus(), "상품 주문시 상태는 상태는 Order");
        Assertions.assertEquals(1, order.getOrderItems().size(), "주문한 상품 종류 수");
        Assertions.assertEquals(10000 * 2, order.getTotalPrice(), "주문 가격은 가격 * 수량");
        Assertions.assertEquals(8, book.getStockQuantity(), "주문 수량 만큼 재고가 줄어듬");
    }

    @Test
    public void 주문취소() {
        //given
        Member member = createMember();
        Book book = createBook("JPA", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        //when
        orderService.cancelOrder(orderId);

        //then
        Order order = orderRepository.findOne(orderId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(book.getStockQuantity()).isEqualTo(10);
    }

    @Test
    public void 상품주문_재고수량초과() {
        //given
        Member member = createMember();
        Item item = createBook("JPA", 10000, 10);

        int orderCount = 11;
        //when
        //then
        assertThrows(NotEnoughStockException.class, () -> orderService.order(member.getId(), item.getId(), orderCount));
    }
    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("MemberA");
        member.setAddress(new Address("서울", "관악", "대학동"));
        em.persist(member);
        return member;
    }

}