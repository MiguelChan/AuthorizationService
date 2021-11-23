import React from 'react';
import {
  Story,
  Meta,
} from '@storybook/react';
import { 
  LandingPage,
} from './LandingPage';

export default {
  title: 'Components/Pages/LandingPage',
  component: LandingPage,
} as Meta;

const Template: Story = () => <LandingPage />;

export const Primary = Template.bind({});