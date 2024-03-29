import { 
  createTheme,
  CssBaseline, 
  ThemeProvider,
} from '@mui/material';
import {
  addDecorator,
} from '@storybook/react';
import {
  BrowserRouter,
} from 'react-router-dom';

import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';

const currentTheme = createTheme();

addDecorator(Story => (
  <>
    <CssBaseline />
    <ThemeProvider theme={currentTheme}>
      <Story />
    </ThemeProvider>
  </>
));

addDecorator(Story => (
  <BrowserRouter>
    <Story />
  </BrowserRouter>
));

export const parameters = {
  actions: { argTypesRegex: "^on[A-Z].*" },
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
}

export const decorators = [
];