import React from 'react';
import {
  StoryFn,
  Meta,
} from '@storybook/react-vite';
import { 
  LandingPage,
} from './LandingPage';

export default {
  title: 'Components/Pages/LandingPage',
  component: LandingPage,
} as Meta;

const Template: StoryFn = () => <LandingPage />;

export const Primary = Template.bind({});