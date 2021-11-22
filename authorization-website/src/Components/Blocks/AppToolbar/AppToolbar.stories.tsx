import React from 'react';
import {
  Story,
  Meta,
} from '@storybook/react';
import {
  AppToolbar,
} from './AppToolbar';
import { AppBar } from '@mui/material';

export default {
  title: 'Components/Blocks/AppToolbar',
  component: AppToolbar,
  decorators: [
    (Story) => (
      <AppBar position='static'>
        <Story />
      </AppBar>
    ),
  ],
} as Meta;

const Template: Story = () => <AppToolbar />;

export const Primary = Template.bind({});