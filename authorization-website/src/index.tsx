import { CssBaseline } from '@mui/material';
import React from 'react';
import ReactDOM from 'react-dom';
import Application from './Components/App/App';

import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';
import { 
  initialState as  initialProfileContext, 
  ProfileContext,
} from './Components/Context';

ReactDOM.render(
  <React.StrictMode>
    <CssBaseline />
    <ProfileContext.Provider value={initialProfileContext}>
      <Application />
    </ProfileContext.Provider>
  </React.StrictMode>,
  document.getElementById('root')
);