import { CssBaseline } from '@mui/material';
import React from 'react';
import { createRoot } from 'react-dom/client';
import Application from './Components/App/App';

import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';
import { ProfileProvider } from './Components/Context';

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Application root element is missing');
}

createRoot(rootElement).render(
  <React.StrictMode>
    <CssBaseline />
    <ProfileProvider>
      <Application />
    </ProfileProvider>
  </React.StrictMode>
);
