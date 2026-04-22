import type { TenantStatus } from '../../api/admin';

const BADGE_STYLES: Record<TenantStatus, { bg: string; color: string }> = {
  ACTIVE:       { bg: 'var(--color-success, #22c55e)', color: '#fff' },
  PROVISIONING: { bg: 'var(--color-info, #3b82f6)',    color: '#fff' },
  SUSPENDED:    { bg: 'var(--color-warn, #f59e0b)',    color: '#fff' },
  ARCHIVED:     { bg: 'var(--color-muted, #6b7280)',   color: '#fff' },
  PURGED:       { bg: 'var(--color-danger, #ef4444)',  color: '#fff' },
};

interface Props {
  status: TenantStatus;
}

export function TenantStatusBadge({ status }: Props) {
  const style = BADGE_STYLES[status] ?? BADGE_STYLES.ARCHIVED;
  return (
    <span style={{
      display: 'inline-block',
      padding: '2px 8px',
      borderRadius: 4,
      fontSize: '0.75rem',
      fontWeight: 600,
      letterSpacing: '0.04em',
      background: style.bg,
      color: style.color,
    }}>
      {status}
    </span>
  );
}
