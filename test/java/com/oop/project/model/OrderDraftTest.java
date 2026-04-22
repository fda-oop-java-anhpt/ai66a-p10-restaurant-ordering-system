package com.oop.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class OrderDraftTest {

    @Test
    void subtotal_reflectsAddReplaceAndRemove() {
        MenuItem pho = new MenuItem(1, "Pho", "", new BigDecimal("40000"), 1, LocalDateTime.now());
        MenuItem bun = new MenuItem(2, "Bun", "", new BigDecimal("30000"), 1, LocalDateTime.now());

        OrderDraft draft = new OrderDraft(99);
        draft.addItem(new OrderItem(pho, List.of(), 1));
        draft.addItem(new OrderItem(bun, List.of(), 2));

        assertEquals(0, draft.getSubtotal().compareTo(new BigDecimal("100000")));

        draft.replaceItem(1, new OrderItem(bun, List.of(), 1));
        assertEquals(0, draft.getSubtotal().compareTo(new BigDecimal("70000")));

        draft.removeItem(0);
        assertEquals(0, draft.getSubtotal().compareTo(new BigDecimal("30000")));

        draft.clearItems();
        assertEquals(0, draft.getSubtotal().compareTo(BigDecimal.ZERO));
    }
}
