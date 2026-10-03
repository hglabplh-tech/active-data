# Active Data: features and proposed email / URL realms

Analysis of `/Users/hglabplh/IdeaProjects/active-data/` on 2026-09-27. This is a source review; the project was not modified or run.

## What the project provides

Active Data is a Clojure/ClojureScript (`.cljc`) library for data modelling. The Leiningen project identifies version **0.3.3** and depends on Clojure 1.11.1 and Prismatic Schema 1.4.1 (`project.clj`).

| Area | Current features | Main source |
| --- | --- | --- |
| Records | Tagged, fixed-field record values; `def-record`; field keys usable for access and update; inheritance via `:extends`; constructors, predicates, reflection, transient mutation; optional field realms and validators. | `src/active/data/record.cljc`, `src/active/data/raw_record.cljc` |
| Structs | Fixed-key struct maps; constructors, accessors, mutators, introspection, lock/unlock. | `src/active/data/struct.cljc` |
| Realms | Data descriptions with a display description, a predicate and metadata. Built-in scalar realms include number, char, keyword, symbol, string, boolean, UUID and any; integer and real ranges are also provided. | `src/active/data/realm.cljc` |
| Realm composition | Optional, union, intersection, enum, sequences, sets, maps, tagged maps, tuples, records, function signatures, named and delayed/recursive realms. `compile` accepts vector, set, map, record and struct shorthand. | `src/active/data/realm.cljc` |
| Realm inspection | Predicates and selectors for realm objects, including inspection realms that classify their kinds. | `src/active/data/realm/inspection.cljc` |
| Validation and integration | Realm-to-Schema translation; delayed validator creation; global function validation switch; realm annotations on functions and record fields. | `src/active/data/realm/schema.cljc`, `src/active/data/realm/validation.cljc`, `src/active/data/realm/attach.cljc`, `src/active/data/record.cljc` |
| Tooling | JVM tests, ClojureScript tests through shadow-cljs/Karma, clj-kondo hooks and CI configuration. | `test/`, `package.json`, `shadow-cljs.edn`, `resources/clj-kondo.exports/` |

`realm/contains?` invokes the realm's predicate and is intended for shallow dispatch, not full validation (`realm.cljc:23-31`). In a scalar realm, the predicate can fully check the scalar value. Nested realms such as `sequence-of` deliberately check only the outer shape with `contains?`; the Schema validator checks contents (`realm.cljc:343-353`, `realm/schema.cljc:129-130`).

**Scope note:** The README and `doc/realms.md` describe generator support as a goal or motivation. I found no generator API in `src/`, so I would not list generated values as an implemented feature. Some function-schema cases also explicitly fall back to weaker checks (`realm/schema.cljc:33-60`).

## Adding string realms for email and URL

### Decide the accepted syntax first

The public contract matters more than the predicate implementation:

- `email`: a single mailbox address, not a display name or comma-separated list. Decide whether internationalized Unicode addresses, quoted local parts and domain literals are accepted. A simple regular expression should be documented as a **practical subset**, not complete RFC email validation. Syntax checking cannot establish that an address receives mail.
- `url`: choose whether this means any URI, an absolute URL, or specifically absolute `http`/`https` URLs. For web links, I recommend an absolute `http` or `https` URL with a nonempty hostname. Decide whether user info, IP literals, Unicode hosts, query-only values and fragments are accepted. Parsing alone is insufficient unless the required scheme and host are checked.
- Preserve the input string. These realms should classify/validate strings, not silently normalize or convert them.
- Keep JVM and JavaScript behavior aligned; Java-only `java.net.URI` would break the shared `.cljc` implementation. If a parser is used, use equivalent implementations and test both platforms.

### Smallest implementation: compose existing realms

The existing `realm/restricted` function (`realm.cljc:656-666`) can define both without changing the realm hierarchy:

```clojure
(def email
  (realm/restricted realm/string valid-email-string? "email address"))

(def url
  (realm/restricted realm/string valid-web-url-string? "absolute HTTP(S) URL"))
```

This creates intersections of `realm/string` and a predicate realm. `contains?` checks both; Schema translation handles the intersection and predicate (`realm/schema.cljc:80-82,123-124`), so `realm.validation/validator`, annotated functions and record fields can use them. Define `valid-email-string?` and `valid-web-url-string?` as total, pure predicates that return false for malformed strings. This is the best low-cost option for application-specific definitions or a first iteration.

Tradeoff: a `from-predicate` component is opaque to structural inspection and future generator tooling (`realm.cljc:106-115,656-666`). If these are public, standard library realms, a distinct representation is preferable.

### First-class library realms

For a public `realm/email` and `realm/url` whose identities can be inspected, follow the existing built-in scalar pattern:

1. Add the two predicate functions and built-in realm values to `src/active/data/realm.cljc`, near `realm/string` (`:email`, `:url` IDs; descriptions; empty metadata). Keep the predicates platform-compatible. `realm/compile` already returns realm objects unchanged (`realm.cljc:683-685`), so it needs no new shorthand branch.
2. Add `email` and `url` inspection realms via `the-builtin-scalar` in `src/active/data/realm/inspection.cljc` (`inspection.cljc:194-213`). Add both to the `inspection/realm` union (`inspection.cljc:267-293`). Without this, the Schema dispatch cannot recognize them as covered realm kinds.
3. Add explicit `inspection/email` and `inspection/url` cases in `src/active/data/realm/schema.cljc`, using `schema/pred` with the same realm predicate and a clear description. The current `inspection/string` case maps to `schema/Str`, which would otherwise accept every string (`schema.cljc:62-82`).
4. Document the syntax policy and add JVM **and** ClojureScript tests: predicate/`contains?`, `compile`, inspection ID, Schema/validator acceptance and rejection, optional/composite use, annotated function and record-field validation where relevant. Include empty string, nil, non-string, whitespace, missing `@`, malformed domain, non-HTTP scheme, relative URL and missing host; add explicit Unicode/IDN cases according to the chosen policy.

No new record subtype is needed: `builtin-scalar-realm` already has an ID, description and predicate (`realm/internal/records.cljc:11-22`). A dedicated subtype would add complexity without clear benefit unless email/URL need inspectable components or configuration.

**Dispatch caveat:** `union` picks its first matching member during Schema translation (`realm/schema.cljc:114-121`), and the internal `union-case` uses ordered predicate dispatch (`realm/internal/dispatch.cljc:61-75`). Because email and URL are subsets of string, ordering matters in unions containing `realm/string`; put a specialized realm before general string where specialization should win. The string realms are also mutually nonexclusive in principle, so they should not be treated as tagged variants.

## Recommendation

Start by specifying the syntax policy and prototyping with `realm/restricted`. If these will be part of Active Data's public API, promote them to built-in scalar realms using the three integration points above. Keep the same predicates for `contains?` and Schema validation so classification and validation agree for these scalar values.

## Verification boundary

This report is based on reading source and tests. I did not run the test suites because their build tools may write generated files inside the project, contrary to the request to leave the project untouched.
