package com.lange.common.rest;

import com.lange.common.functional.Or;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by lange on 21/1/16.
 */
public class AsynchResponseContainerTest {

    @Test
    public void testInstantiation() {
        AsynchResponseContainer<String, Integer> container = AsynchResponseContainer.create();
        assertNotNull(container);

        Optional<Or<String, Integer>> contained = container.getContained();
        assertNotNull(contained);
        assertFalse(contained.isPresent());
    }

    @Test
    public void testReset() {
        AsynchResponseContainer<String, Integer> container = AsynchResponseContainer.create();
        assertNotNull(container);

        Optional<Or<String, Integer>> contained = container.getContained();
        assertNotNull(contained);
        assertFalse(contained.isPresent());

        container.reset();
        Optional<Or<String, Integer>> contained2 = container.getContained();
        assertNotNull(contained2);
        assertFalse(contained2.isPresent());

        container.setSuccessResult("ABC");
        Optional<Or<String, Integer>> contained3 = container.getContained();
        assertNotNull(contained3);
        assertTrue(contained3.isPresent());

        container.reset();
        Optional<Or<String, Integer>> contained4 = container.getContained();
        assertNotNull(contained4);
        assertFalse(contained4.isPresent());

    }
}
