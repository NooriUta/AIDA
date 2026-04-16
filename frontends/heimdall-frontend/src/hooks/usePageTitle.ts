import { useEffect } from 'react';

/**
 * Sets document.title to `${title} — Heimðallr` on mount,
 * restores the previous title on unmount.
 * Re-runs whenever `title` changes (e.g. on language switch via t()).
 */
export function usePageTitle(title: string) {
  useEffect(() => {
    const prev = document.title;
    document.title = `${title} — Heimðallr`;
    return () => { document.title = prev; };
  }, [title]);
}
