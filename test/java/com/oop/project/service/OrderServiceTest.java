package com.oop.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.oop.project.model.CustomizationOption;
import com.oop.project.model.MenuItem;
import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;

class OrderServiceTest {

    @Test
    void createOrder_setsStaffId() {
        OrderService service = new OrderService();
        OrderDraft draft = service.createOrder(77);

        assertEquals(77, draft.getStaffId());
        assertEquals(0, draft.getItems().size());
    }

    @Test
    void parseQuantity_validatesInput() {
        assertEquals(5, OrderService.parseQuantity("5"));
        assertThrows(IllegalArgumentException.class, () -> OrderService.parseQuantity("abc"));
        assertThrows(IllegalArgumentException.class, () -> OrderService.parseQuantity(""));
    }

    @Test
    void addReplaceRemove_followQuantityRules() {
        OrderService service = new OrderService();
        OrderDraft draft = service.createOrder(1);
        MenuItem tea = new MenuItem(1, "Tea", "", new BigDecimal("10000"), 1, LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () ->
            service.addItem(draft, tea, List.of(), 0)
        );

        service.addItem(draft, tea, List.of(), 2);
        assertEquals(1, draft.getItems().size());
        assertEquals(2, draft.getItems().get(0).getQuantity());

        service.replaceItem(draft, 0, tea, List.of(), 3);
        assertEquals(3, draft.getItems().get(0).getQuantity());

        service.removeItem(draft, 0);
        assertEquals(0, draft.getItems().size());
    }

    @Test
    void copyOfSelected_createsIndependentList() {
        OrderService service = new OrderService();
        List<CustomizationOption> selected = new ArrayList<>();
        selected.add(new CustomizationOption(1, "Extra", BigDecimal.ONE, 1));

        List<CustomizationOption> copied = service.copyOfSelected(selected);
        selected.clear();

        assertEquals(1, copied.size());
    }

    @Test
    void calculateTotals_applyExpectedRates() {
        OrderService service = new OrderService();
        BigDecimal subtotal = new BigDecimal("100000");

        assertEquals(0, service.calculateTax(subtotal).compareTo(new BigDecimal("10000.00")));
        assertEquals(0, service.calculateServiceFee(subtotal).compareTo(new BigDecimal("5000.00")));
        assertEquals(0, service.calculateTotal(subtotal).compareTo(new BigDecimal("115000.00")));
    }

    @Test
    void lineTotalFromDraft_matchesServiceTotalExpectation() {
        OrderService service = new OrderService();
        OrderDraft draft = service.createOrder(12);

        MenuItem coffee = new MenuItem(2, "Coffee", "", new BigDecimal("25000"), 1, LocalDateTime.now());
        CustomizationOption milk = new CustomizationOption(4, "Extra milk", new BigDecimal("5000"), 2);

        service.addItem(draft, coffee, List.of(milk), 2);

        OrderItem line = draft.getItems().get(0);
        assertEquals(0, line.getUnitPrice().compareTo(new BigDecimal("30000")));
        assertEquals(0, draft.getSubtotal().compareTo(new BigDecimal("60000")));
    }
}
