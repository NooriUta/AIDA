# AIDA Architecture Diagrams

10 Mermaid diagrams from `ARCH_DEEPDIVE_AIDA.md` v1.0, saved as individual `.mermaid` files для переиспользования и редактирования.

## Index

| № | File | Part | Section | Description |
|---|---|---|---|---|
| 01 | `01_c4_level1_context.mermaid` | Part I | §3 | C4 Level 1 Context — AIDA как black box + actors + external systems |
| 02 | `02_c4_level2_container.mermaid` | Part I | §4 | C4 Level 2 Container — все компоненты AIDA с ключевыми связями |
| 03 | `03_data_layer_storage.mermaid` | Part II | §5 | Data Layer — YGG (HoundArcade) + FRIGG (unified state) topology |
| 04 | `04_hound_pipeline.mermaid` | Part II | §6 | Hound 4-stage pipeline с SemanticEngine components |
| 05 | `05_dali_core_internal.mermaid` | Part II | §7 | Dali Core internal — JobRunr + REST + worker pool + FRIGG persistence |
| 06 | `06_api_gateway_flow.mermaid` | Part II | §9 | API Gateway sequence — Browser → Chur → SHUTTLE → backends |
| 07 | `07_verdandi_composition.mermaid` | Part II | §10 | VERDANDI composition — LOOM + KNOT + ANVIL UI + MIMIR Chat |
| 08 | `08_heimdall_event_bus.mermaid` | Part II | §11 | HEIMDALL event bus + metrics + control plane |
| 09 | `09_failure_modes_recovery.mermaid` | Part III | §14 | Failure taxonomy + recovery strategies |
| 10 | `10_deployment_topology.mermaid` | Part III | §15 | Docker Compose deployment topology (tiers, ports, volumes) |

## How to use

**Preview online:** copy content of any `.mermaid` file to https://mermaid.live

**Render locally:**
```bash
# Install mermaid-cli
npm install -g @mermaid-js/mermaid-cli

# Render to SVG
mmdc -i 01_c4_level1_context.mermaid -o 01_c4_level1_context.svg

# Render all to SVG batch
for f in *.mermaid; do mmdc -i "$f" -o "${f%.mermaid}.svg"; done

# Render to PNG с dark background
mmdc -i 01_c4_level1_context.mermaid -o 01.png -b transparent -t dark
```

**Embed в Markdown:**
```markdown
\`\`\`mermaid
[paste contents of .mermaid file, excluding YAML front matter]
\`\`\`
```

## Color coding (consistent across diagrams)

- 🟢 **Green** (`#4ade80`) — working / primary / recovery
- 🟡 **Yellow** (`#fbbf24`) — new / in-progress / graceful
- 🟠 **Orange** (`#fb923c`) — severe
- 🔴 **Red** (`#f87171`) — catastrophic / critical
- 🔵 **Blue** (`#60a5fa`) — external / transient
- 🟣 **Purple** (`#a78bfa`) — storage / worker
- ⚫ **Gray** (`#94a3b8`) — planned / deferred

## Source

All diagrams originated from working sessions on 11-12.04.2026 during AIDA architecture planning. Source document: `ARCH_DEEPDIVE_AIDA.md` v1.0.
