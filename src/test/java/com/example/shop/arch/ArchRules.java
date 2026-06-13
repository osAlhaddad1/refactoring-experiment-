package com.example.shop.arch;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import org.springframework.data.repository.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * The four architecture rules of the gate, kept in one place so both the gate
 * test (which fails the build) and the JSON report writer can use them.
 *
 * Target architecture, all under com.example.shop:
 *   presentation  -> application
 *   application   -> domain   (through ports)
 *   infrastructure-> domain   (implements ports)
 *   domain        -> nothing
 *
 * allowEmptyShould(true) means a rule does not error just because there are no
 * classes to check yet (for example a baseline that has no layers at all).
 */
public final class ArchRules {

    private ArchRules() {
    }

    /** Rule 1: each layer may only be used by the layers that are allowed to. */
    public static final ArchRule LAYERED = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Presentation").definedBy("..presentation..")
            .layer("Application").definedBy("..application..")
            .layer("Domain").definedBy("..domain..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
            .allowEmptyShould(true);

    /** Rule 2: no cyclic dependencies between the top-level packages. */
    public static final ArchRule NO_CYCLES = slices()
            .matching("com.example.shop.(*)..")
            .should().beFreeOfCycles()
            .allowEmptyShould(true);

    /** Rule 3: the domain stays pure (no Spring, no JPA, no infrastructure). */
    public static final ArchRule DOMAIN_PURITY = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "..infrastructure..")
            .allowEmptyShould(true);

    /** Rule 4: classes live in the package their name implies. */
    public static final ArchRule PACKAGE_NAMING = CompositeArchRule
            .of(classes().that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..presentation..").allowEmptyShould(true))
            .and(classes().that().haveSimpleNameEndingWith("Service")
                    .should().resideInAPackage("..application..").allowEmptyShould(true))
            .and(classes().that().haveSimpleNameEndingWith("UseCase")
                    .should().resideInAPackage("..application..").allowEmptyShould(true))
            .and(classes().that().haveSimpleNameEndingWith("Adapter")
                    .should().resideInAPackage("..infrastructure..").allowEmptyShould(true))
            .and(classes().that().areAssignableTo(Repository.class)
                    .should().resideInAPackage("..infrastructure..").allowEmptyShould(true));
}
