import { CssBaseline } from '@mui/material';
import React from 'react';
import ReactDOM from 'react-dom';
import Application from './Components/App/App';

ReactDOM.render(
  <React.StrictMode>
    <CssBaseline />
    <Application />
  </React.StrictMode>,
  document.getElementById('root')
);