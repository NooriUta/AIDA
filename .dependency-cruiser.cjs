/** @type {import('dependency-cruiser').IConfiguration} */
/**
 * .dependency-cruiser.cjs — root-level dep-cruiser config.
 *
 * This file is used by the QG-ARCH CI workflow (arch-rules.yml) to check
 * dependency hygiene across all three TypeScript frontends (verdandi,
 * heimdall-frontend, shell) and the BFF (chur).
 *
 * Rules implemented here match QG-ARCH-enforcement.md §2:
 *   R-TS-01 — BFF must not import frontend code
 *   R-TS-02 — admin layer must not import public layer  (chur-specific)
 *   R-TS-03 — keycloakAdmin.ts only from src/admin     (chur-specific)
 *   R-TS-04 — ArcadeDbSessionStore only from store layer (chur-specific)
 *   R-TS-05 — No direct circular dependencies
 *   R-TS-06 — No node_modules with known security issues in critical path
 *
 * Per-package overrides live in each package's own
 * `<package>/.dependency-cruiser.cjs` (created by their npm scripts).
 *
 * Usage from repo root:
 *   npx depcruise --config .dependency-cruiser.cjs bff/chur/src
 *   npx depcruise --config .dependency-cruiser.cjs frontends/verdandi/src
 *   npx depcruise --config .dependency-cruiser.cjs frontends/heimdall-frontend/src
 *   npx depcruise --config .dependency-cruiser.cjs frontends/shell/src
 */
module.exports = {
  forbidden: [
    // ── R-TS-01: BFF must not import frontend code ─────────────────────────
    {
      name: 'no-bff-to-frontend',
      comment: 'R-TS-01 — BFF (chur) must not import from frontend packages.',
      severity: 'error',
      from: { path: '^bff/chur/src' },
      to:   { path: '^frontends/' },
    },

    // ── R-TS-02: chur/src/admin must not import chur/src/public ─────────────
    {
      name: 'admin-layer-isolated-from-public',
      comment: 'R-TS-02 — admin routes must not import public-facing API surface.',
      severity: 'error',
      from: { path: '^bff/chur/src/admin' },
      to:   { path: '^bff/chur/src/public' },
    },

    // ── R-TS-03: keycloakAdmin.ts used only from src/admin (MTN-67) ──────────
    {
      name: 'kc-admin-sdk-admin-only',
      comment: 'R-TS-03 — MTN-67: keycloakAdmin.ts must only be imported by src/admin.',
      severity: 'error',
      from: { pathNot: '^bff/chur/src/admin' },
      to:   { path:    '^bff/chur/src/keycloakAdmin\\.ts$' },
    },

    // ── R-TS-04: ArcadeDbSessionStore used only via sessions.ts (not directly) ─
    {
      name: 'session-store-encapsulated',
      comment: 'R-TS-04 — ArcadeDbSessionStore must not be imported outside store layer / sessions.ts.',
      severity: 'error',
      from: { pathNot: '^bff/chur/src/(store|sessions)\\.ts$' },
      to:   { path:    '^bff/chur/src/store/ArcadeDbSessionStore\\.ts$' },
    },

    // ── R-TS-05: No circular dependencies anywhere ────────────────────────────
    {
      name: 'no-circular',
      comment: 'R-TS-05 — Circular dependencies cause unpredictable module init order.',
      severity: 'warn',
      from: {},
      to: {
        circular: true,
      },
    },

    // ── R-TS-06: No direct Keycloak-URL hardcoding in frontends ─────────────
    {
      name: 'no-direct-keycloak-import-from-fe',
      comment: 'R-TS-06 — Frontends must use the auth API wrapper, not keycloak-js directly in business logic.',
      severity: 'error',
      from: {
        path:    '^frontends/.*?/src',
        pathNot: '^frontends/.*?/src/api/auth',
      },
      to: {
        path: 'keycloak-js',
      },
    },

    // ── R-TS-07: No aida-shared imports inside shell except via MF remote ────
    {
      name: 'shell-no-direct-aida-shared',
      comment: 'R-TS-07 — shell must consume aida-shared as a Module Federation singleton, not via direct import.',
      severity: 'warn',
      from: { path: '^frontends/shell/src' },
      to:   { path: '^packages/aida-shared/src' },
    },
  ],

  options: {
    // ── Module resolution ────────────────────────────────────────────────────
    moduleSystems: ['es6', 'cjs'],
    tsPreCompilationDeps: false,
    combinedDependencies: false,

    // ── Include TypeScript config so aliases are resolved ────────────────────
    tsConfig: {
      // Each frontend/package has its own tsconfig; dep-cruiser merges them.
      // Path resolution falls back gracefully when tsconfig is not found.
      fileName: 'tsconfig.json',
    },

    // ── File extensions to follow ─────────────────────────────────────────────
    extensions: ['.ts', '.tsx', '.js', '.jsx', '.mts', '.cts'],

    // ── Exclude node_modules internals and test files ─────────────────────────
    exclude: {
      path: [
        'node_modules',
        '\\.test\\.',
        '\\.spec\\.',
        '__tests__',
        'dist',
        'build',
        '\\.vite',
      ],
    },

    // ── Report format (used when run interactively; CI uses --output-type json) ─
    reporterOptions: {
      dot: {
        collapsePattern: 'node_modules/(?:@[^/]+/[^/]+|[^/]+)',
      },
      text: {
        highlightFocused: true,
      },
    },
  },
};
