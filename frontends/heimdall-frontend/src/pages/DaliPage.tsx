import { useTranslation } from 'react-i18next';

export default function DaliPage() {
  const { t } = useTranslation();
  return (
    <div style={{
      display:        'flex',
      alignItems:     'center',
      justifyContent: 'center',
      height:         '100%',
      background:     'var(--bg0)',
    }}>
      <div style={{ textAlign: 'center' }}>
        <div style={{ fontSize: '36px', marginBottom: 'var(--seer-space-3)', opacity: 0.4 }}>🗄</div>
        <div style={{ fontSize: '14px', color: 'var(--t2)', fontWeight: 500 }}>
          {t('stub.comingSoon')}
        </div>
        <div style={{ fontSize: '12px', color: 'var(--t3)', marginTop: 'var(--seer-space-2)' }}>
          Dali — ArcadeDB Graph Explorer
        </div>
      </div>
    </div>
  );
}
