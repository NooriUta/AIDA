import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ROLES } from './types';
import type { UserRole } from './types';

interface UserInviteModalProps {
  onInvite: (email: string, name: string, role: UserRole) => void;
  onClose: () => void;
}

export function UserInviteModal({ onInvite, onClose }: UserInviteModalProps) {
  const { t } = useTranslation();
  const [email, setEmail] = useState('');
  const [name,  setName]  = useState('');
  const [role,  setRole]  = useState<UserRole>('viewer');

  const handleSend = () => {
    if (!email.trim()) return;
    onInvite(email.trim(), name.trim(), role);
    onClose();
  };

  return (
    <div className="modal-overlay open" onClick={e => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="invite-modal" onClick={e => e.stopPropagation()}>

        {/* Header */}
        <div className="modal-header" style={{ height: 48 }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
            strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
            style={{ color: 'var(--t2)', flexShrink: 0 }}>
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <line x1="19" y1="8" x2="19" y2="14" />
            <line x1="22" y1="11" x2="16" y2="11" />
          </svg>
          <div className="modal-header-info">
            <div className="modal-header-name">Пригласить пользователя</div>
            <div className="modal-header-sub">Приглашение отправляется на email</div>
          </div>
          <button className="modal-close" onClick={onClose}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
              strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Body */}
        <div style={{ padding: 24 }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 14 }}>
            <div>
              <label className="field-label">Email *</label>
              <input
                className="field-input"
                placeholder="user@company.com"
                value={email}
                onChange={e => setEmail(e.target.value)}
                type="email"
              />
            </div>
            <div>
              <label className="field-label">Имя</label>
              <input
                className="field-input"
                placeholder="Иван Петров"
                value={name}
                onChange={e => setName(e.target.value)}
              />
            </div>
          </div>

          <div>
            <label className="field-label">Роль</label>
            <div style={{ maxHeight: 280, overflowY: 'auto' }}>
              {ROLES.map(r => {
                const tierClass: Record<string, string> = {
                  user: 'tier-user', tenant: 'tier-tenant', platform: 'tier-platform',
                };
                const selected = role === r.id;
                return (
                  <div
                    key={r.id}
                    className={`role-card-item ${selected ? 'selected' : ''}`}
                    onClick={() => setRole(r.id)}
                  >
                    <div
                      className="role-icon-wrap"
                      style={{
                        background: `color-mix(in srgb, ${r.clr} 16%, var(--bg3))`,
                        color: r.clr,
                      }}
                    >
                      <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                        strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                      </svg>
                    </div>
                    <div className="role-card-info">
                      <div className="role-card-name" style={{ color: r.clr }}>{r.label}</div>
                      <div className="role-card-desc">{r.desc}</div>
                    </div>
                    <span className={`role-tier-tag ${tierClass[r.tier] ?? ''}`}>{r.tier}</span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="modal-footer">
          <span style={{ fontSize: 11, color: 'var(--t3)' }}>
            Keycloak создаст аккаунт автоматически
          </span>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn btn-secondary" onClick={onClose}>
              {t('users.cancel')}
            </button>
            <button
              className="btn btn-primary"
              onClick={handleSend}
              disabled={!email.trim()}
              style={{ opacity: email.trim() ? 1 : 0.5 }}
            >
              Отправить приглашение
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
