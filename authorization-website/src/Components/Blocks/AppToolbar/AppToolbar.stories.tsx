import React from 'react';
import {
  StoryFn,
  Meta,
} from '@storybook/react-vite';
import {
  AppToolbar,
} from './AppToolbar';
import { AppBar } from '@mui/material';

export default {
  title: 'Components/Blocks/AppToolbar',
  component: AppToolbar,
  decorators: [
    (StoryFn) => (
      <AppBar position='static'>
        <StoryFn />
      </AppBar>
    ),
  ],
} as Meta;

const Template: StoryFn = () => <AppToolbar />;

export const Primary = Template.bind({});