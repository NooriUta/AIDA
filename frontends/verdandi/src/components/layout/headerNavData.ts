// ─── Navigation structure (Seiðr Studio architecture) ────────────────────────
export type NornId = 'VERDANDI' | 'URD' | 'SKULD';

export interface SubModule {
  id:       string;
  key:      string;
  route:    string | null;
  horizon?: string;
}

export interface NornDef {
  id:         NornId;
  descKey:    string;
  route:      string;
  subModules: SubModule[];
  horizon?:   string;
}

export const NORNS: NornDef[] = [
  {
    id: 'VERDANDI', descKey: 'nav.verdandiDesc', route: '/',
    subModules: [
      { id: 'LOOM',  key: 'nav.loom',  route: '/'     },
      { id: 'KNOT',  key: 'nav.knot',  route: '/knot' },
      { id: 'ANVIL', key: 'nav.anvil', route: null, horizon: 'H2' },
    ],
  },
  { id: 'URD',   descKey: 'nav.urdDesc',   route: '/urd',   subModules: [], horizon: 'H3' },
  { id: 'SKULD', descKey: 'nav.skuldDesc', route: '/skuld', subModules: [], horizon: 'H3' },
];

export const PALETTES: { id: string; key: string }[] = [
  { id: 'amber-forest', key: 'palette.amberForest' },
  { id: 'lichen',       key: 'palette.lichen'      },
  { id: 'slate',        key: 'palette.slate'        },
  { id: 'juniper',      key: 'palette.juniper'      },
  { id: 'warm-dark',    key: 'palette.warmDark'     },
];
