package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
 */
@RestController
@RequiredArgsConstructor
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
        System.out.println(" ================================= ");
        List<OrderDto> collect = orders.stream().map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        System.out.println(" ================================= ");
        return collect;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> list =orderRepository.findAllWithItem();
        System.out.println(" ================================= ");
        List<OrderDto> collect = list.stream().map(order -> new OrderDto(order))
                .collect(Collectors.toList());

        System.out.println(" ================================= ");
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
            System.out.println(" 생성자 시작================================= ");
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                        .map(orderItem -> new OrderItemDto(orderItem))
                        .collect(Collectors.toList());
            System.out.println(" 생성자 종료================================= ");
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
