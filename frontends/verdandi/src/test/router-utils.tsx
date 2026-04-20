import React from 'react';
import { render, type RenderOptions, type RenderResult } from '@testing-library/react';
import { MemoryRouter, type MemoryRouterProps } from 'react-router-dom';

interface RouterRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  initialEntries?: MemoryRouterProps['initialEntries'];
  initialIndex?: MemoryRouterProps['initialIndex'];
}

/**
 * Renders a component inside MemoryRouter so that useNavigate / useLocation
 * work without a live browser history. Use this instead of plain render() for
 * any component that calls navigation hooks.
 */
export function renderWithRouter(
  ui: React.ReactElement,
  { initialEntries = ['/'], initialIndex, ...options }: RouterRenderOptions = {},
): RenderResult {
  return render(ui, {
    wrapper: ({ children }) => (
      <MemoryRouter initialEntries={initialEntries} initialIndex={initialIndex}>
        {children}
      </MemoryRouter>
    ),
    ...options,
  });
}
