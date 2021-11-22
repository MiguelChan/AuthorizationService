import React from 'react';
import {
  Story,
  Meta,
} from '@storybook/react';
import { SignUpPage } from './SignUpPage';

export default {
  title: 'Components/Pages/SignUpPage', 
  component: SignUpPage,
} as Meta;

const Template: Story = () => <SignUpPage />;

export const Primary = Template.bind({});