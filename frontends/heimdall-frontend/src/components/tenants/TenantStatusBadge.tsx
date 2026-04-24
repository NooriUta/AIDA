import { useTranslation } from 'react-i18next';
import type { TenantStatus } from '../../api/admin';

const BADGE_STYLES: Record<TenantStatus, { bg: string; color: string; border: string }> = {
  ACTIVE:              { bg: 'color-mix(in srgb, var(--suc) 18%, transparent)',     color: 'var(--suc)',     border: 'color-mix(in srgb, var(--suc) 40%, transparent)' },
  PROVISIONING:        { bg: 'color-mix(in srgb, var(--inf) 18%, transparent)',    color: 'var(--inf)',    border: 'color-mix(in srgb, var(--inf) 40%, transparent)' },
  PROVISIONING_FAILED: { bg: 'color-mix(in srgb, var(--danger) 18%, transparent)', color: 'var(--danger)', border: 'color-mix(in srgb, var(--danger) 40%, transparent)' },
  SUSPENDED:           { bg: 'color-mix(in srgb, var(--wrn) 18%, transparent)',    color: 'var(--wrn)',    border: 'color-mix(in srgb, var(--wrn) 40%, transparent)' },
  ARCHIVED:            { bg: 'color-mix(in srgb, var(--t3) 14%, transparent)',     color: 'var(--t2)',     border: 'color-mix(in srgb, var(--t3) 35%, transparent)' },
  PURGED:              { bg: 'color-mix(in srgb, var(--danger) 14%, transparent)', color: 'var(--danger)', border: 'color-mix(in srgb, var(--danger) 35%, transparent)' },
};

interface Props {
  status: TenantStatus;
}

export function TenantStatusBadge({ status }: Props) {
  const { t } = useTranslation();
  const style = BADGE_STYLES[status] ?? BADGE_STYLES.ARCHIVED;
  return (
    <span style={{
      display: 'inline-block',
      padding: '2px 8px',
      borderRadius: 4,
      fontSize: 11,
      fontWeight: 600,
      letterSpacing: '0.02em',
      background: style.bg,
      color: style.color,
      border: `1px solid ${style.border}`,
      whiteSpace: 'nowrap',
    }}>
      {t(`tenants.statusName.${status}`, status)}
    </span>
  );
}
