/**
 * Heimdall UI string constants — centralised localisation.
 *
 * All user-visible text should be defined here rather than inlined in components.
 * This makes future translation / branding changes a single-file edit.
 *
 * Structure: nested objects by feature area. Keep keys in English (en).
 */

export const UI = {

  // ── Dali: ParseForm ──────────────────────────────────────────────────────────
  dali: {
    form: {
      title:          'New parse session',
      modePathBtn:    'Path',
      modeUploadBtn:  'Upload',
      dialectLabel:   'Dialect',
      sourceLabel:    'Source path',
      sourcePlaceholder: '/data/sql-sources/my_package.pck',
      fileLabel:      'File',
      dropPlaceholder: 'Drop .zip / .rar / .sql here — or click to browse',
      previewLabel:   'Preview (dry-run)',
      clearLabel:     'Clear YGG before write',
      submitParse:    'Parse',
      submitUpload:   'Upload & Parse',
      submitting:     '...',
      errSelectFile:      'Select a file to upload',
      errSourceRequired:  'Source path is required',
      errAnotherActive:   'Another session is active — wait for it to finish before starting a new one',
    },

    // ── Dali: SessionList ──────────────────────────────────────────────────────
    sessions: {
      panelTitle:     'Sessions',
      sessionCount:   (n: number) => `${n} session${n !== 1 ? 's' : ''}`,

      colSessionId:   'Session ID',
      colStatus:      'Status',
      colDialect:     'Dialect',
      colSource:      'Source',
      colProgress:    'Progress',
      colDuration:    'Duration',

      emptyTitle:     'No sessions yet',
      emptySub:       'Start a parse session using the form above\nor press Enter after filling the source path',

      // Timing labels (detail expand)
      timingStarted:   'Started',
      timingFinished:  'Finished',
      timingFailed:    'Failed',
      timingCancelled: 'Cancelled',
      timingTotal:     'Total',

      // Stats labels
      statAtoms:      'Atoms',
      statVertices:   'Vertices',
      statEdges:      'Edges',
      statResolution: 'Resolution',
      statDuration:   'Duration',

      statSubInYgg:    'in YGG',
      statSubInserted: 'inserted',
      statSubLineage:  'lineage links',
      statSubColumnLevel: 'column-level',
      statSubWallTime: 'wall time',
      statSubFilesDone: 'files done',
      statSubPartial:  'partial',

      // File breakdown
      fileBreakdownTitle: (_n: number, top25: boolean) => `Duration by file${top25 ? ' (top 25)' : ''}`,
      fileBreakdownTotal: (dur: string) => `total ${dur}`,
      parseTimeline:      (n: number) => `Parse timeline (${n} files)`,
      filesHeader:        (count: number, failed: boolean, total: number) =>
        `Files (${count}${failed ? ` of ${total}` : ''})`,

      // Vertex breakdown
      vertexBreakdown:    'Vertex breakdown',
      colType:        'Type',
      colInserted:    'Inserted',
      colDuplicate:   'Duplicate',
      colTotal:       'Total',

      // FRIGG badge
      friggSaved:     'Saved to FRIGG',
      friggPending:   'Not yet saved to FRIGG',

      // Instance badge
      instanceTitle:  (id: string) => `Dali instance: ${id}`,

      // Errors / warnings
      errorsBtn:      (n: number) => `${n} error${n !== 1 ? 's' : ''}`,
      warningsBtn:    (n: number) => `${n} warning${n !== 1 ? 's' : ''} total`,
    },

    // ── Dali: DaliPage ─────────────────────────────────────────────────────────
    page: {
      title:          'Parse engine',
      description:    'SQL parsing engine · JobRunr monitoring · lineage → YGG',
      refreshBtn:     'Refresh',
      retryBtn:       'Retry',

      // Stat cards
      statTotal:      'Total sessions',
      statTotalSub:   'all time',
      statRunning:    'Running',
      statRunningSub: 'active',
      statCompleted:  'Completed',
      statCompletedSub: 'since startup',
      statAtoms:      'Atoms parsed',
      statAtomsSub:   'total extracted',
      statAvgRes:     'Avg resolution',
      statAvgResSub:  'column-level',

      // YGG strip
      yggLoading:       'loading…',
      yggTables:        'tables',
      yggColumns:       'columns',
      yggStmts:         'stmts',
      yggRoutines:      'routines',
      yggAtoms:         'atoms',
      yggResolved:      (pct: string) => `${pct}% resolved`,
      yggUnresolved:    (n: number) => `${n} unresolved`,
      yggPending:       (n: number) => `${n} pending`,
      yggRefreshTitle:  'Refresh YGG stats',

      // Availability banner
      connecting:     'Connecting to Dali :9090…',
      offline:        (retryS: number) => `Dali :9090 unavailable — retrying in ${retryS}s`,

      // Archive section
      archiveTitle:   'Session archive (FRIGG)',
      archiveRecords: (n: number) => `${n} records`,
      archiveLoading: 'loading…',

      // Toast messages
      toastQueued:    (id: string) => `Session ${id} queued`,
      toastCompleted: (id: string, atoms: string) => `Session ${id} completed · ${atoms} atoms`,
      toastFailed:    (id: string) => `Session ${id}: parse failed`,
      toastArchiveErr: 'FRIGG archive unavailable',

      // Footer
      footerJobsTracked: (n: number) => `${n} jobs tracked`,
      footerReady:       'jobrunr ready',
    },
  },

  // ── Services ──────────────────────────────────────────────────────────────────
  services: {
    healthTitle:  'Click to open Services',
    healthLabel:  'Services',
  },

  // ── Auth ─────────────────────────────────────────────────────────────────────
  auth: {
    loginTitle:   'Sign in to Heimdall',
    userLabel:    'Username',
    passLabel:    'Password',
    loginBtn:     'Sign in',
    logoutBtn:    'Sign out',
  },

  // ── Common ───────────────────────────────────────────────────────────────────
  common: {
    loading:   'Loading…',
    error:     'Error',
    unknown:   '—',
    save:      'Save',
    cancel:    'Cancel',
    confirm:   'Confirm',
    delete:    'Delete',
    close:     'Close',
    back:      'Back',
  },
};
