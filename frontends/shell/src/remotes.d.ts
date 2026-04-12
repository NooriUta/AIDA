/**
 * Type declarations for Module Federation remotes.
 * Each remote exposes its App component as a default export.
 */
declare module 'verdandi/App' {
  import type React from 'react';
  const App: React.ComponentType;
  export default App;
}

declare module 'heimdall-frontend/App' {
  import type React from 'react';
  const App: React.ComponentType;
  export default App;
}
