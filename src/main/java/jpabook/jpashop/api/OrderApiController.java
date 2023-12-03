package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * oneToMany List 컬렉션 조회시 발생하는 문제점
 * 1. @JsonIgnore 사용
 * 2. Entity를 직접 반환 하지 말고, DTO로 반환하여 반환
 * 3. Fetch Join시 ToMany의 경우 페이징 불가 (데이터가 많을경우 out of memory)
 *     => firstResult/maxResults specified with collection fetch; applying in memory
 *
 * Batch Size 적용
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(orderItem -> orderItem.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream().map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        return collect;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> list =orderRepository.findAllWithItem();
        List<OrderDto> collect = list.stream().map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        return collect;
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page() {
        List<Order> list =orderRepository.findAllWithMemberDelivery();
        List<OrderDto> collect = list.stream().map(order -> new OrderDto(order))
                .collect(Collectors.toList());

        return collect;
    }
    @GetMapping("/api/v3.1/page/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset",defaultValue = "0") int offset,
            @RequestParam(value = "limit",defaultValue = "100") int limit
                                        ) {
        // ToOne 관계는 페이징 처리가 가능하다.
        List<Order> list =orderRepository.findAllWithMemberDelivery(offset,limit);
        List<OrderDto> collect = list.stream().map(order -> new OrderDto(order))
                .collect(Collectors.toList());

        return collect;
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            log.info("order 생성자 시작 ={}",order);
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                        .map(orderItem -> new OrderItemDto(orderItem))
                        .collect(Collectors.toList());
            log.info("order 생성자 끝 ={}",order);
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;


        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
