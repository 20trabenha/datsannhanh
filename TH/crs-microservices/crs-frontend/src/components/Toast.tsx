import { useEffect } from 'react';

interface ToastProps {
  message: string;
  type: 'success' | 'error';
  onClose: () => void;
}

export default function Toast({ message, type, onClose }: ToastProps) {
  useEffect(() => {
    const timer = window.setTimeout(onClose, 3500);
    return () => window.clearTimeout(timer);
  }, [onClose]);

  return <div role="status" style={{ position: 'fixed', bottom: 24, right: 24, padding: '12px 20px', borderRadius: 8, color: '#fff', backgroundColor: type === 'success' ? '#15803d' : '#b91c1c', boxShadow: '0 2px 8px rgba(0,0,0,0.2)', zIndex: 1000 }}>
    {message}<button type="button" aria-label="Dong thong bao" onClick={onClose} style={{ marginLeft: 12, background: 'none', border: 'none', color: '#fff', cursor: 'pointer' }}>×</button>
  </div>;
}
