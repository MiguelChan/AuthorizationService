import { createTheme, CssBaseline, ThemeProvider } from '@mui/material';
import type { Preview } from '@storybook/react-vite';
import { MemoryRouter } from 'react-router-dom';
import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';

const theme = createTheme();
const preview: Preview = {
  decorators: [
    (Story) => <ThemeProvider theme={theme}><CssBaseline /><Story /></ThemeProvider>,
    (Story) => <MemoryRouter><Story /></MemoryRouter>,
  ],
  parameters: {
    controls: { matchers: { color: /(background|color)$/i, date: /Date$/ } },
  },
};
export default preview;
