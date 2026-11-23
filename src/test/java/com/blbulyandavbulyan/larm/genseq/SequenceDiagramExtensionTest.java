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

    @Test
    void failedTestIsNotWritten() {
        assertThat(SequenceDiagramExtension.shouldWrite(true, true, 3)).isFalse();
    }

    @Test
    void zeroRequestsAreNotWritten() {
        assertThat(SequenceDiagramExtension.shouldWrite(true, false, 0)).isFalse();
    }

    @Test
    void selectedPassedWithRequestsIsWritten() {
        assertThat(SequenceDiagramExtension.shouldWrite(true, false, 1)).isTrue();
    }

    @Test
    void pitestMinionIsAMutationRun() {
        assertThat(SequenceDiagramExtension.isMutationRun("org.pitest.mutationtest.execute.MutationTestMinion 42")).isTrue();
    }

    @Test
    void regularTestRunIsNotAMutationRun() {
        assertThat(SequenceDiagramExtension.isMutationRun(
                "/usr/share/maven/boot/surefire/surefirebooter.jar /tmp/surefire 2026 surefire.tmp")).isFalse();
        assertThat(SequenceDiagramExtension.isMutationRun(null)).isFalse();
    }

    @Test
    void notSelectedIsNotWritten() {
        assertThat(SequenceDiagramExtension.shouldWrite(false, false, 2)).isFalse();
    }
}
