# Refactoring experiment — reference app + architecture gate

This repository is the test harness for a bachelor thesis experiment: it checks
whether an AI coding model, when asked to refactor a messy Java file into a clean
4-tier architecture, introduces forbidden dependencies between layers — and
whether an ArchUnit "architecture gate" reliably catches those violations.

This README only covers what exists after **Phase 1**.

## Target architecture (all under `com.example.shop`)

| Layer            | Holds                                              | May depend on        |
|------------------|----------------------------------------------------|----------------------|
| `presentation`   | REST controllers + request/response DTOs           | `application`        |
| `application`    | service / use-case classes (orchestrate via ports) | `domain` (ports)     |
| `domain`         | entities, business logic, repository PORT interfaces | nothing            |
| `infrastructure` | JPA entities + adapters that implement the ports   | `domain`             |

Everything else is a violation.

## The architecture gate (ArchUnit)

Four rules, each written as a normal test that fails the build when broken
(`src/test/java/com/example/shop/arch/`):

1. **Layered architecture** — a layer may only be used by the layers allowed to use it.
2. **No cycles** — no cyclic dependencies between the top-level packages.
3. **Domain purity** — nothing in `..domain..` may import `org.springframework..`,
   `jakarta.persistence..`, or `..infrastructure..`.
4. **Package naming** — `*Controller` in `presentation`, `*Service` / `*UseCase` in
   `application`, `*Adapter` and Spring Data repositories in `infrastructure`.

### JSON report

When the gate test runs it always writes `target/arch-report.json` (whether the
build passed or failed). The Python runner depends on this format, so it is kept
simple and stable:

```json
{
  "violationCount": 1,
  "violations": [
    {
      "rule": "Package naming",
      "type": "NAMING",
      "sourceClass": "com.example.shop.ShopController",
      "targetClass": "",
      "message": "Class <com.example.shop.ShopController> ... should reside in '..presentation..'"
    }
  ]
}
```

- `type` is one of `LAYERED`, `CYCLE`, `DOMAIN_PURITY`, `NAMING`.
- `sourceClass` is the offending class; `targetClass` is what it depends on
  (empty for naming/cycle violations). `message` is always the full ArchUnit text.

## The simple baseline ("god file")

`src/main/java/com/example/shop/ShopController.java` mixes HTTP handling,
business rules (bulk discount + stock management) and raw JPA into one class in
the root package. It **passes** the HTTP tests but **fails** the gate.

## How to build / verify

Java 17 source level, built/run here on Corretto 23 (no JDK 17 is installed on
this machine). Maven is not on PATH; use a full path to `mvn`.

```powershell
$env:JAVA_HOME = "C:\Users\osami\.jdks\corretto-23.0.2"
$mvn = "C:\Users\osami\AppData\Local\thesis-build-tools\apache-maven-3.9.9\bin\mvn.cmd"

# behaviour tests (expected: PASS)
& $mvn -Dtest=ShopBehaviourTest test

# architecture gate (expected: FAIL, and writes target/arch-report.json)
& $mvn -Dtest=ArchitectureGateTest test
```
