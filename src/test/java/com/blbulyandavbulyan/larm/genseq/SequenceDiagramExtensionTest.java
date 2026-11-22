package com.blbulyandavbulyan.larm.genseq;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SequenceDiagramExtensionTest {
    private static final Class<?> TEST_CLASS = SequenceDiagramExtensionTest.class;

    @Test
    void annotatedIsSelected() {
        assertThat(SequenceDiagramExtension.isSelected(TEST_CLASS, "foo", true, null)).isTrue();
    }

    @Test
    void listedSimpleNameIsSelected() {
        assertThat(SequenceDiagramExtension.isSelected(TEST_CLASS, "foo",
                false, "Other#bar, SequenceDiagramExtensionTest#foo")).isTrue();
    }

    @Test
    void listedFqcnIsSelected() {
        assertThat(SequenceDiagramExtension.isSelected(TEST_CLASS, "foo",
                false, TEST_CLASS.getName() + "#foo")).isTrue();
    }

    @Test
    void notListedNotAnnotatedIsNotSelected() {
        assertThat(SequenceDiagramExtension.isSelected(TEST_CLASS, "foo",
                false, "SequenceDiagramExtensionTest#bar,Other#foo")).isFalse();
    }

    @Test
    void nullSelectSelectsOnlyAnnotated() {
        assertThat(SequenceDiagramExtension.isSelected(TEST_CLASS, "foo", false, null)).isFalse();
        assertThat(SequenceDiagramExtension.isSelected(TEST_CLASS, "foo", false, "")).isFalse();
    }
}
