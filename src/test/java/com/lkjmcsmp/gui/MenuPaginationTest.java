package com.lkjmcsmp.gui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuPaginationTest {

    @Test
    void maxPageForZeroItems() {
        assertEquals(0, MenuPagination.maxPage(0));
    }

    @Test
    void maxPageForOnePage() {
        assertEquals(0, MenuPagination.maxPage(1));
        assertEquals(0, MenuPagination.maxPage(28));
    }

    @Test
    void maxPageForMultiplePages() {
        assertEquals(1, MenuPagination.maxPage(29));
        assertEquals(1, MenuPagination.maxPage(56));
        assertEquals(2, MenuPagination.maxPage(57));
    }

    @Test
    void clampPageBounds() {
        assertEquals(0, MenuPagination.clampPage(-1, 10));
        assertEquals(0, MenuPagination.clampPage(0, 10));
        assertEquals(1, MenuPagination.clampPage(5, 30));
    }

    @Test
    void pageSliceReturnsCorrectRange() {
        List<Integer> items = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        List<Integer> slice = MenuPagination.pageSlice(items, 0);
        assertEquals(10, slice.size());

        java.util.ArrayList<Integer> items2 = new java.util.ArrayList<>();
        for (int i = 0; i < 29; i++) items2.add(i);
        List<Integer> slice2 = MenuPagination.pageSlice(items2, 1);
        assertEquals(1, slice2.size());
        assertEquals(28, slice2.get(0));
    }
}
