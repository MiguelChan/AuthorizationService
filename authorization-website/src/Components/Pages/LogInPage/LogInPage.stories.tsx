import React from 'react';
import {
  StoryFn,
  Meta,
} from '@storybook/react-vite';
import { LogInPage } from './LogInPage';

export default {
  title: 'Components/Pages/LogInPage',
  component: LogInPage,
} as Meta;

const Template: StoryFn = () => <LogInPage />;

export const Primary = Template.bind({});