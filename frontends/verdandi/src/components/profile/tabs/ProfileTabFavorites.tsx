import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';

const MOCK_FAVORITES = [
  { id: 'fav1', name: 'orders',              type: 'DaliTable',   color: 'var(--acc)', schema: 'public',    meta: '14 col · L2'  },
  { id: 'fav2', name: 'users',               type: 'DaliTable',   color: 'var(--acc)', schema: 'public',    meta: '8 col · L2'   },
  { id: 'fav3', name: 'get_order_summary',   type: 'DaliRoutine', color: 'var(--inf)', schema: 'public',    meta: 'Routine · L2' },
  { id: 'fav4', name: 'analytics.events',    type: 'DaliTable',   color: 'var(--suc)', schema: 'analytics', meta: '22 col'       },
];

export const ProfileTabFavorites = memo(() => {
  const { t } = useTranslation();
  const [favs, setFavs] = useState(MOCK_FAVORITES);

  function remove(id: string) {
    setFavs((f) => f.filter((x) => x.id !== id));
  }

  return (
    <div>
      <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>
        {t('profile.tabs.favorites')}
      </div>

      <p style={{ fontSize: '12px', color: 'var(--t3)', marginBottom: '20px', lineHeight: 1.6 }}>
        {t('profile.favorites.hint')}
      </p>

      {favs.length === 0 ? (
        <div style={{ padding: '24px', textAlign: 'center', background: 'var(--bg2)', border: '1px dashed var(--bd)', borderRadius: 'var(--seer-radius-md)' }}>
          <div style={{ fontSize: '22px', marginBottom: '8px', opacity: 0.4 }}>☆</div>
          <div style={{ fontSize: '12px', color: 'var(--t3)' }}>{t('profile.favorites.empty')}</div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          {favs.map((f) => (
            <div key={f.id} style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '9px 12px', background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)', transition: 'border-color 0.12s' }}>
              <div style={{ width: '28px', height: '28px', borderRadius: 'var(--seer-radius-sm)', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', background: `color-mix(in srgb, ${f.color} 12%, var(--bg3))`, color: f.color }}>
                {f.type === 'DaliTable' ? '⊞' : '⊡'}
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: '12px', fontWeight: 500, color: 'var(--t1)', fontFamily: 'var(--mono)' }}>{f.name}</div>
                <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '1px' }}>{f.schema} · {f.meta}</div>
              </div>
              <button
                onClick={() => remove(f.id)}
                title={t('profile.favorites.remove')}
                style={{ width: '26px', height: '26px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: 'none', background: 'transparent', borderRadius: 'var(--seer-radius-sm)', cursor: 'pointer', color: 'var(--acc)', transition: 'background 0.12s' }}
                onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
                onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
              >
                ★
              </button>
            </div>
          ))}
        </div>
      )}

      <div style={{ marginTop: '16px', padding: '10px 14px', background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)' }}>
        <div style={{ fontSize: '11px', color: 'var(--t3)' }}>{t('profile.favorites.syncHint')}</div>
      </div>
    </div>
  );
});

ProfileTabFavorites.displayName = 'ProfileTabFavorites';
