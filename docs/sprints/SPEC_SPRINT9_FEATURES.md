# Sprint 9 Feature Specifications

This document defines the feature specifications for Sprint 9 of VERDANDI LOOM.
All behaviors, dimensions, keys, and values described here are normative.

---

## 1. Profile Modal

### Component

`ProfileModal`

### Props

| Prop      | Type           | Description                        |
|-----------|----------------|------------------------------------|
| `onClose` | `() => void`   | Callback invoked when the modal is dismissed. |

### Tabs

The modal contains 10 tabs organized into 3 sections:

| Section   | Tabs                                                          |
|-----------|---------------------------------------------------------------|
| Account   | Profile, Security, Access                                     |
| Interface | Appearance, Graph                                             |
| System    | Activity, Notifications, Favorites, Shortcuts, Tokens         |

### Dimensions

| Property       | Value    |
|----------------|----------|
| Max width      | 820px    |
| Max height     | 580px    |
| Sidebar width  | 192px    |

### Animation

Open animation transitions from the initial state to the final state:

| Property     | Initial                        | Final                    |
|--------------|--------------------------------|--------------------------|
| transform    | translateY(12px) scale(0.98)   | translateY(0) scale(1)   |
| easing       | cubic-bezier(0.16, 1, 0.3, 1) |                          |

### Close Behavior

The modal can be closed via any of the following triggers:

- Pressing the `Escape` key
- Clicking the backdrop overlay
- Clicking the X button

All close triggers apply a 200ms delay before invoking `onClose()`.

---

## 2. Appearance Settings

### Color Palettes

| Key           | Description      |
|---------------|------------------|
| amber-forest  | Default palette  |
| slate         | Neutral gray     |
| lichen        | Muted green      |
| juniper       | Deep teal        |
| warm-dark     | Warm dark tones  |

### UI Fonts

| Font            |
|-----------------|
| DM Sans         |
| Inter            |
| IBM Plex Sans   |
| Geist           |
| Oxanium         |
| Exo 2           |

### Monospace Fonts

| Font             |
|------------------|
| Fira Code        |
| JetBrains Mono   |
| IBM Plex Mono    |
| Geist Mono       |
| Source Code Pro   |

### Font Size

Range: 11px to 16px (integer steps).

### Density

Two modes: `compact` and `normal`.

### CSS Variables

| Variable          | Purpose                                    |
|-------------------|--------------------------------------------|
| `--font`          | Active UI font family                      |
| `--mono`          | Active monospace font family                |
| `fontSize`        | Base font size in px                        |
| `data-density`    | HTML attribute on root element (`compact` or `normal`) |

---

## 3. Graph Preferences

### GraphPrefs Keys

| Key               | Type      | Description                                      |
|--------------------|-----------|--------------------------------------------------|
| `autoLayout`       | boolean   | Automatically re-layout the graph on data change |
| `drillAnimation`   | boolean   | Animate transitions when drilling into nodes     |
| `hoverHighlight`   | boolean   | Highlight connected edges/nodes on hover         |
| `showEdgeLabels`   | boolean   | Display labels on edges                          |
| `colLevelDefault`  | boolean   | Show column-level detail by default              |
| `startLevel`       | number    | Initial zoom/drill level on load                 |
| `nodeLimit`        | number    | Maximum number of nodes rendered at once         |

### Storage

Persisted to `localStorage` under the key `seer-graph-prefs` as a JSON object.

---

## 4. Command Palette

### Trigger

- Windows/Linux: `Ctrl+K`
- macOS: `Cmd+K`

Registered as a global keyboard shortcut.

### Commands

| Command ID       | Shortcut | Description                         |
|------------------|----------|-------------------------------------|
| `fit-view`       | F        | Fit the canvas to the viewport      |
| `toggle-theme`   | T        | Toggle between light and dark theme |
| `deselect`       | Escape   | Clear all node/edge selections      |
| `nav-l1`         | --       | Navigate to L1 overview             |
| `nav-knot`       | --       | Navigate to KNOT view               |

### Search Integration

The command palette includes an inline search field. A maximum of 8 matching results are displayed at any time.

### Keyboard Navigation

| Key        | Behavior                              |
|------------|---------------------------------------|
| ArrowUp    | Move selection to previous item       |
| ArrowDown  | Move selection to next item           |
| Enter      | Execute the selected command          |
| Escape     | Close the command palette             |

---

## 5. Search Palette

### Trigger

Press the `/` key to open the search palette (global scope).

### Type Filters

7 filter categories are available:

| Filter        | Description                  |
|---------------|------------------------------|
| all           | No filtering (show all)      |
| tables        | Table nodes only             |
| columns       | Column nodes only            |
| routines      | Routine/procedure nodes only |
| statements    | Statement nodes only         |
| databases     | Database nodes only          |
| applications  | Application nodes only       |

### Routing by DaliNodeType

Search results navigate to the appropriate canvas level based on node type:

| DaliNodeType                  | Target Level |
|-------------------------------|--------------|
| application, database, schema | L1           |
| table, column, package        | L2           |
| subquery                      | L3           |

### Recent Items

| Storage Key            | Max Entries | Content              |
|------------------------|-------------|----------------------|
| `seer-search-history`  | 10          | Recent search queries |
| `seer-recent-nodes`    | 10          | Recently visited nodes|

Both stored in `localStorage`.

### Search Behavior

| Parameter     | Value   |
|---------------|---------|
| Debounce      | 300ms   |
| Min characters| 2       |

---

## 6. Inspector Components

### InspectorColumn

Displays metadata for a selected column node.

| Field       | Description                                 |
|-------------|---------------------------------------------|
| label       | Column name                                 |
| type badge  | Visual badge indicating the column category |
| dataType    | SQL/source data type                        |
| operation   | Transformation or operation applied         |

### InspectorJoin

Displays metadata for a join relationship.

| Field        | Description                                |
|--------------|--------------------------------------------|
| label        | Join display name                          |
| join type badge | Visual badge for join type (INNER, LEFT, etc.) |
| left table   | Left-side table of the join                |
| right table  | Right-side table of the join               |
| condition    | Join condition expression                  |

### InspectorParameter

Displays metadata for a routine parameter.

| Field        | Description                                |
|--------------|--------------------------------------------|
| label        | Parameter name                             |
| param badge  | Visual badge for parameter classification  |
| direction    | IN, OUT, or INOUT                          |
| routine name | Name of the parent routine                 |

---

## 7. Keyboard Shortcuts

### Global Shortcuts

| Shortcut         | Action                  |
|------------------|-------------------------|
| `Ctrl+K` / `Cmd+K` | Open command palette |
| `/`              | Open search palette      |

### Canvas Shortcuts

| Shortcut | Action                         |
|----------|--------------------------------|
| Escape   | Deselect all nodes and edges   |
| F        | Fit view to canvas content     |

### Store Shortcuts

| Shortcut                       | Action |
|--------------------------------|--------|
| `Ctrl+Z` / `Cmd+Z`            | Undo   |
| `Ctrl+Shift+Z` / `Cmd+Shift+Z`| Redo   |

---

## 8. Persistence

All persistence uses `localStorage`. Keys and their purposes are listed below.

| localStorage Key        | Content                        | Notes                  |
|-------------------------|--------------------------------|------------------------|
| `seer-loom-filters`     | Serialized filter state        | Debounced at 500ms     |
| `seer-theme`            | Active theme identifier        |                        |
| `seer-palette`          | Active color palette           |                        |
| `seer-search-history`   | Recent search query strings    | Max 10 entries         |
| `seer-recent-nodes`     | Recently visited node IDs      | Max 10 entries         |
| `seer-ui-font`          | Selected UI font family        |                        |
| `seer-mono-font`        | Selected monospace font family |                        |
| `seer-font-size`        | Font size in px                |                        |
| `seer-density`          | Density mode (compact/normal)  |                        |
| `seer-graph-prefs`      | Graph preferences JSON object  |                        |

---

## 9. Undo/Redo

### Entry Types

Each undo/redo entry records one of the following action types:

| Entry Type  | Description                                   |
|-------------|-----------------------------------------------|
| `hide`      | A node was hidden from the canvas              |
| `restore`   | A previously hidden node was restored          |
| `showAll`   | All hidden nodes were made visible             |
| `expand`    | A node was expanded to reveal child nodes      |

### Max Depth

The undo/redo stack holds a maximum of 50 entries. When the limit is exceeded, the oldest entry is discarded.

### Shortcuts

| Shortcut                       | Action |
|--------------------------------|--------|
| `Ctrl+Z` / `Cmd+Z`            | Undo   |
| `Ctrl+Shift+Z` / `Cmd+Shift+Z`| Redo   |
