package com.example.shop.arch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The architecture gate.
 *
 * Each of the four rules is a normal test that fails the build when broken.
 * After the rules run, writeReport() always writes a machine-readable JSON
 * report (target/arch-report.json) listing every violation. The Python runner
 * reads that report, so its format is kept simple and stable:
 *
 * {
 *   "violationCount": 1,
 *   "violations": [
 *     { "rule": "Package naming", "type": "NAMING",
 *       "sourceClass": "com.example.shop.ShopController",
 *       "targetClass": "",
 *       "message": "Class <...> ... should reside in '..presentation..'" }
 *   ]
 * }
 */
class ArchitectureGateTest {

    private static final String REPORT_PATH = "target/arch-report.json";

    private static JavaClasses classes;

    @BeforeAll
    static void importMainClasses() {
        // Only the application's own classes, not the test classes.
        classes = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.example.shop");
    }

    @Test
    void layeredArchitecture() {
        ArchRules.LAYERED.check(classes);
    }

    @Test
    void noCycles() {
        ArchRules.NO_CYCLES.check(classes);
    }

    @Test
    void domainPurity() {
        ArchRules.DOMAIN_PURITY.check(classes);
    }

    @Test
    void packageNaming() {
        ArchRules.PACKAGE_NAMING.check(classes);
    }

    /** Runs after every rule (pass or fail) and writes the JSON report. */
    @AfterAll
    static void writeReport() throws Exception {
        List<Map<String, Object>> violations = new ArrayList<>();
        collect(violations, "Layered architecture", "LAYERED", ArchRules.LAYERED);
        collect(violations, "No cycles", "CYCLE", ArchRules.NO_CYCLES);
        collect(violations, "Domain purity", "DOMAIN_PURITY", ArchRules.DOMAIN_PURITY);
        collect(violations, "Package naming", "NAMING", ArchRules.PACKAGE_NAMING);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("violationCount", violations.size());
        report.put("violations", violations);

        File out = new File(REPORT_PATH);
        if (out.getParentFile() != null) {
            out.getParentFile().mkdirs();
        }
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, report);
    }

    /** Evaluates one rule and turns each failure line into a violation entry. */
    private static void collect(List<Map<String, Object>> violations,
                                String ruleName, String type, ArchRule rule) {
        EvaluationResult result = rule.evaluate(classes);
        for (String message : result.getFailureReport().getDetails()) {
            String[] pair = extractClasses(message);
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("rule", ruleName);
            v.put("type", type);
            v.put("sourceClass", pair[0]);
            v.put("targetClass", pair[1]);
            v.put("message", message);
            violations.add(v);
        }
    }

    private static final Pattern ANGLE = Pattern.compile("<([^>]+)>");

    /**
     * Best-effort: ArchUnit puts the classes involved between angle brackets,
     * for example "Method <A.foo()> calls method <B.bar()> ...". We pull out the
     * first one as the source and the second (if any) as the target. The full
     * text is always kept in "message", so this is only for convenience.
     */
    private static String[] extractClasses(String message) {
        Matcher m = ANGLE.matcher(message);
        String source = "";
        String target = "";
        if (m.find()) {
            source = trimMember(m.group(1));
        }
        if (m.find()) {
            target = trimMember(m.group(1));
        }
        return new String[] {source, target};
    }

    /** Drops a trailing method/parameter part, e.g. "a.b.C.foo()" -> "a.b.C.foo". */
    private static String trimMember(String token) {
        int paren = token.indexOf('(');
        if (paren >= 0) {
            token = token.substring(0, paren);
        }
        return token.trim();
    }
}
