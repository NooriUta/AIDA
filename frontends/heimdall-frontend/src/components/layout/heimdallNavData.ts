// ── Navigation data ────────────────────────────────────────────────────────────
export type SectionId = 'BIFROST' | 'DALI' | 'SAGA' | 'FENRIR';

export interface SubTab { id: string; labelKey: string; route: string; minRole?: 'admin' | 'local-admin' }
export interface Section { id: SectionId; descKey: string; route: string; subTabs: SubTab[]; horizon?: string }

export const SECTIONS: Section[] = [
  {
    id: 'BIFROST', descKey: 'nav.overviewDesc', route: '/overview',
    subTabs: [
      { id: 'Services',  labelKey: 'nav.services',  route: '/overview/services'  },
      { id: 'Dashboard', labelKey: 'nav.dashboard', route: '/overview/dashboard' },
      { id: 'Events',    labelKey: 'nav.events',    route: '/overview/events'    },
    ],
  },
  {
    id: 'DALI', descKey: 'nav.daliDesc', route: '/dali',
    subTabs: [
      { id: 'Sessions', labelKey: 'nav.sessions', route: '/dali/sessions' },
      { id: 'Sources',  labelKey: 'nav.sources',  route: '/dali/sources'  },
    ],
  },
  {
    id: 'SAGA', descKey: 'nav.docsDesc', route: '/docs',
    subTabs: [
      { id: 'Docs', labelKey: 'nav.docs', route: '/docs' },
    ],
  },
  {
    id: 'FENRIR', descKey: 'nav.adminDesc', route: '/admin/tenants',
    subTabs: [
      { id: 'Tenants',   labelKey: 'nav.tenants',   route: '/admin/tenants', minRole: 'admin'       },
      { id: 'Users',     labelKey: 'nav.users',     route: '/users',         minRole: 'local-admin' },
      { id: 'Analytics', labelKey: 'nav.analytics', route: '/analytics',     minRole: 'admin'       },
    ],
  },
];

export const PALETTES: Array<{ id: string; key: string }> = [
  { id: 'amber-forest', key: 'palette.amberForest' },
  { id: 'lichen',       key: 'palette.lichen'      },
  { id: 'slate',        key: 'palette.slate'        },
  { id: 'juniper',      key: 'palette.juniper'      },
  { id: 'warm-dark',    key: 'palette.warmDark'     },
];
