package com.jkc.microservices.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ReactorTest {

    @Test
    void testFlux() {
        List<Integer> integerList = new ArrayList<>();
        Flux.just(1, 2, 4, 5, 6, 7).filter(n -> n % 2 == 0).map(n -> n * 2).log().subscribe(integerList::add);
        assertThat(integerList).containsExactly(4, 8, 12);
    }

    @Test
    void testFluxBlocking() {
        List<Integer> integerList = Flux.just(1, 2, 4, 5, 6, 7).filter(n -> n % 2 == 0).map(n -> n * 2).log().collectList().block();
        assertThat(integerList).containsExactly(4, 8, 12);
    }
}
