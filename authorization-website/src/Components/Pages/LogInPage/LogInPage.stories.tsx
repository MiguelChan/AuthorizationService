import React from 'react';
import {
  Story,
  Meta,
} from '@storybook/react';
import { LogInPage } from './LogInPage';

export default {
  title: 'Components/Pages/LogInPage',
  component: LogInPage,
} as Meta;

const Template: Story = () => <LogInPage />;

export const Primary = Template.bind({});