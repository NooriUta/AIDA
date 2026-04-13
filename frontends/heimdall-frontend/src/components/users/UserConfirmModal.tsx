import { useTranslation } from 'react-i18next';

interface UserConfirmModalProps {
  title:     string;
  message:   string;
  danger?:   boolean;
  onConfirm: () => void;
  onClose:   () => void;
}

export function UserConfirmModal({
  title, message, danger = true, onConfirm, onClose,
}: UserConfirmModalProps) {
  const { t } = useTranslation();

  const handleConfirm = () => {
    onConfirm();
    onClose();
  };

  return (
    <div
      style={{
        position:       'fixed',
        inset:          0,
        background:     'rgba(0,0,0,.65)',
        display:        'flex',
        alignItems:     'center',
        justifyContent: 'center',
        zIndex:         200,
      }}
      onClick={e => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="confirm-box" onClick={e => e.stopPropagation()}>
        <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 8 }}>{title}</div>
        <div style={{ fontSize: 12, color: 'var(--t2)', marginBottom: 20, lineHeight: 1.6 }}>
          {message}
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button className="btn btn-secondary" onClick={onClose}>
            {t('users.cancel')}
          </button>
          <button
            className={`btn ${danger ? 'btn-danger' : 'btn-primary'}`}
            onClick={handleConfirm}
          >
            {t('users.confirm')}
          </button>
        </div>
      </div>
    </div>
  );
}
