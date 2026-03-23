# quantum-sdk-android (Kotlin)

## SDK Parity Check

This SDK must stay in sync with the Rust reference SDK. Use `sdk-graph` to check parity:

```bash
# Scan this SDK (run after making changes)
sdk-graph scan --sdk kotlin --dir ~/work/tauri_apps/qe-sdk-collection/android_projects/quantum-sdk/src

# Scan Rust reference (if not recently scanned)
sdk-graph scan --sdk rust --dir ~/work/tauri_apps/qe-sdk-collection/rust_projects/quantum-sdk/src

# Show what this SDK is missing vs Rust
sdk-graph diff --base rust --target kotlin

# Show overall stats
sdk-graph stats
```

Binary: `~/go/bin/sdk-graph` (in PATH)
Graph file: `~/work/go_programs/quantum-ai/sdk-graph.json` (shared across all SDKs)

## Workflow

1. Before starting work: run `sdk-graph diff --base rust --target kotlin` to see current gaps
2. After adding types/fields: rescan with `sdk-graph scan`
3. Verify gap reduced: run diff again
4. Goal: zero missing types and fields vs Rust

## Reference Implementation

The Rust SDK is the source of truth: `~/work/tauri_apps/qe-sdk-collection/rust_projects/quantum-sdk/src/`

When adding missing types, follow the Rust SDK's field names and JSON serialization. Map types idiomatically:
- Rust `Option<T>` → Kotlin `T?` with `= null` default
- Rust `Vec<T>` → Kotlin `List<T>`
- Rust `String` → Kotlin `String`
- Rust `serde(rename = "snake_case")` → Kotlin `@SerialName("snake_case")`
- All data types should be `@Serializable data class`

## API Server

Backend: https://api.quantumencoding.ai
Repo: ~/work/go_programs/quantum-ai
