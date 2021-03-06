package com.codahale.metrics;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

public class SharedMetricRegistriesTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        SharedMetricRegistries.setDefaultRegistryName(new AtomicReference<>());
        SharedMetricRegistries.clear();
    }

    @Test
    public void memorizesRegistriesByName() throws Exception {
        final MetricRegistry one = SharedMetricRegistries.getOrCreate("one");
        final MetricRegistry two = SharedMetricRegistries.getOrCreate("one");

        assertThat(one)
                .isSameAs(two);
    }

    @Test
    public void hasASetOfNames() throws Exception {
        SharedMetricRegistries.getOrCreate("one");

        assertThat(SharedMetricRegistries.names())
                .containsOnly("one");
    }

    @Test
    public void removesRegistries() throws Exception {
        final MetricRegistry one = SharedMetricRegistries.getOrCreate("one");
        SharedMetricRegistries.remove("one");

        assertThat(SharedMetricRegistries.names())
                .isEmpty();

        final MetricRegistry two = SharedMetricRegistries.getOrCreate("one");
        assertThat(two)
                .isNotSameAs(one);
    }

    @Test
    public void clearsRegistries() throws Exception {
        SharedMetricRegistries.getOrCreate("one");
        SharedMetricRegistries.getOrCreate("two");

        SharedMetricRegistries.clear();

        assertThat(SharedMetricRegistries.names())
                .isEmpty();
    }

    @Test
    public void errorsWhenDefaultUnset() throws Exception {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Default registry name has not been set.");
        SharedMetricRegistries.getDefault();
    }

    @Test
    public void createsDefaultRegistries() throws Exception {
        final String defaultName = "default";
        final MetricRegistry registry = SharedMetricRegistries.setDefault(defaultName);
        assertThat(registry).isNotNull();
        assertThat(SharedMetricRegistries.getDefault()).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getOrCreate(defaultName)).isEqualTo(registry);
    }

    @Test
    public void errorsWhenDefaultAlreadySet() throws Exception {
        SharedMetricRegistries.setDefault("foobah");
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Default metric registry name is already set.");
        SharedMetricRegistries.setDefault("borg");
    }

    @Test
    public void setsDefaultExistingRegistries() throws Exception {
        final String defaultName = "default";
        final MetricRegistry registry = new MetricRegistry();
        assertThat(SharedMetricRegistries.setDefault(defaultName, registry)).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getDefault()).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getOrCreate(defaultName)).isEqualTo(registry);
    }
}
