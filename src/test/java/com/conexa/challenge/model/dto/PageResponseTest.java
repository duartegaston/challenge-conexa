package com.conexa.challenge.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResponseTest {

    private final List<Integer> numbers = List.of(1, 2, 3, 4, 5);

    @Test
    void firstPageHasNext() {
        PageResponse<Integer> page = PageResponse.slice(numbers, 1, 2);

        assertThat(page.content()).containsExactly(1, 2);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.limit()).isEqualTo(2);
        assertThat(page.total()).isEqualTo(5);
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void lastPartialPageHasNoNext() {
        PageResponse<Integer> page = PageResponse.slice(numbers, 3, 2);

        assertThat(page.content()).containsExactly(5);
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void exactMultipleDoesNotInventAnExtraPage() {
        assertThat(PageResponse.slice(List.of(1, 2, 3, 4), 2, 2).hasNext()).isFalse();
    }

    @Test
    void pageBeyondTheEndIsEmpty() {
        PageResponse<Integer> page = PageResponse.slice(numbers, 10, 2);

        assertThat(page.content()).isEmpty();
        assertThat(page.total()).isEqualTo(5);
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void hugePageNumberDoesNotOverflow() {
        assertThat(PageResponse.slice(numbers, Integer.MAX_VALUE, 100).content()).isEmpty();
    }

    @Test
    void emptyList() {
        PageResponse<Integer> page = PageResponse.slice(List.of(), 1, 10);

        assertThat(page.content()).isEmpty();
        assertThat(page.total()).isZero();
        assertThat(page.hasNext()).isFalse();
    }
}
