import { memo } from 'react';
import { useTranslation } from 'react-i18next';

export const InspectorEmpty = memo(() => {
  const { t } = useTranslation();
  return (
    <div role="status" style={{ padding: '12px 10px' }}>
      <p style={{ fontSize: '11px', color: 'var(--t3)', margin: 0 }}>
        {t('inspector.empty')}
      </p>
    </div>
  );
});

InspectorEmpty.displayName = 'InspectorEmpty';
