"""The six prompting approaches and the text blocks used to build their prompts.

Each approach decides:
- guidance: what architecture knowledge we put in the instruction
- context:  what material we show the model
- loop:     whether we re-prompt with the violation report until the gate passes

"Whole module" = the god-file plus three read-only blocks appended after it:
the architecture description, the ArchUnit rule source that grades the run, and
the HTTP behaviour tests (marked "do not modify").
"""

# ---------------------------------------------------------------------------
# The six approaches, in order.
# ---------------------------------------------------------------------------

APPROACHES = [
    {"name": "naive-local",           "guidance": "none",          "context": "god_only",      "loop": False},
    {"name": "naive-module",          "guidance": "none",          "context": "whole_module",  "loop": False},
    {"name": "archaware-interfaces",  "guidance": "arch_desc",     "context": "skeleton_ports","loop": False},
    {"name": "ruleaware-module",      "guidance": "literal_rules", "context": "whole_module",  "loop": False},
    {"name": "ruleaware-module-loop", "guidance": "literal_rules", "context": "whole_module",  "loop": True},
    {"name": "recipe-loop",           "guidance": "rules_recipe",  "context": "whole_module",  "loop": True},
]

APPROACH_NAMES = [a["name"] for a in APPROACHES]


def approach_by_name(name):
    for approach in APPROACHES:
        if approach["name"] == name:
            return approach
    return None


# ---------------------------------------------------------------------------
# Text blocks.
# ---------------------------------------------------------------------------

BASIC_ASK = (
    "Refactor the following Java application into a clean, layered architecture.\n"
    "Keep the application's HTTP behaviour exactly the same."
)

ARCH_DESCRIPTION = """Target architecture. All code lives under com.example.shop, in four layers:

- presentation: REST controllers and request/response DTOs.
- application: service / use-case classes that orchestrate the domain through ports.
- domain: entities, business logic, and repository PORT interfaces. Pure: no Spring, no JPA, no other layer.
- infrastructure: JPA entities and adapter classes that implement the domain ports.

Allowed dependencies (everything else is forbidden):
  presentation   -> application
  application    -> domain (only through ports)
  infrastructure -> domain (implements ports)
  domain         -> nothing"""

LITERAL_RULES = """Your refactoring must obey these architecture rules. An automated ArchUnit gate checks them:

1. Layered architecture: presentation -> application -> domain, and infrastructure -> domain.
   presentation must NOT reach infrastructure or domain directly.
2. No cycles between the top-level packages (presentation, application, domain, infrastructure).
3. Domain purity: nothing in ..domain.. may import org.springframework.., jakarta.persistence.., or ..infrastructure..
4. Package naming: *Controller in presentation; *Service or *UseCase in application;
   *Adapter and Spring Data repositories in infrastructure."""

RECIPE = """Follow this recipe:
- The presentation layer returns its own DTOs and never imports domain or infrastructure types.
- JPA stays in infrastructure, hidden behind repository port interfaces declared in the domain.
- Any global/shared state also sits behind a port, with its implementation in infrastructure."""

TARGET_SKELETON = """Create your classes in these packages:
  com.example.shop.presentation    (REST controllers + DTOs)
  com.example.shop.application      (services / use-cases)
  com.example.shop.domain           (entities, business logic, repository port interfaces)
  com.example.shop.infrastructure   (JPA entities + adapters implementing the ports)"""

DOMAIN_PORT_INTERFACES = """Here is the kind of port interface the domain should declare
(design the ports your code actually needs):

  package com.example.shop.domain;
  import java.util.Optional;
  public interface ProductRepository {
      Product save(Product product);
      Optional<Product> findById(Long id);
  }

The matching adapter (which uses JPA) belongs in com.example.shop.infrastructure
and implements this interface."""

OUTPUT_CONTRACT = """Return EXACTLY ONE JSON object and nothing else. No markdown, no code fences,
no text outside the JSON. The JSON must have this shape:

{
  "files": [ { "path": "src/main/java/com/example/shop/.../X.java", "content": "<full file content>" } ],
  "deleted": [ "src/main/java/com/example/shop/OldClass.java" ],
  "notes": "short summary of what you changed"
}

- Put every file you create or change in "files", each with its full content and a
  path relative to the project root, using forward slashes.
- List files to remove in "deleted".
- Do NOT change the HTTP behaviour tests or the ArchUnit rule files; they grade your work."""


def _read_only(title, body):
    return "=== " + title + " ===\n" + body


def build_prompt(approach, context, report_text=None):
    """Builds the full prompt text for one approach.

    context is a dict with: god_file, arch_rules_source, behaviour_tests.
    report_text is the previous JSON violation report (loop approaches only).
    """
    parts = [BASIC_ASK]

    # guidance
    guidance = approach["guidance"]
    if guidance == "arch_desc":
        parts.append(ARCH_DESCRIPTION)
    elif guidance == "literal_rules":
        parts.append(LITERAL_RULES)
    elif guidance == "rules_recipe":
        parts.append(LITERAL_RULES)
        parts.append(RECIPE)

    # the code to refactor is always shown
    parts.append(_read_only("The code to refactor (the god-file)", context["god_file"]))

    # extra context
    kind = approach["context"]
    if kind == "whole_module":
        parts.append(_read_only("Architecture description (read-only reference)", ARCH_DESCRIPTION))
        parts.append(_read_only("ArchUnit rules that grade your output (read-only, do not modify)",
                                context["arch_rules_source"]))
        parts.append(_read_only("HTTP behaviour tests (read-only, do not modify, must keep passing)",
                                context["behaviour_tests"]))
    elif kind == "skeleton_ports":
        parts.append(_read_only("Target package skeleton", TARGET_SKELETON))
        parts.append(_read_only("Example domain port interface", DOMAIN_PORT_INTERFACES))

    parts.append(OUTPUT_CONTRACT)

    if report_text:
        parts.append(_read_only(
            "Your previous attempt FAILED the architecture gate. "
            "Fix these violations and resubmit the full refactoring",
            report_text))

    return "\n\n".join(parts)
