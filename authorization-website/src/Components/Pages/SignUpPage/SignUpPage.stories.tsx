import React from 'react';
import {
  StoryFn,
  Meta,
} from '@storybook/react-vite';
import { SignUpPage } from './SignUpPage';

export default {
  title: 'Components/Pages/SignUpPage', 
  component: SignUpPage,
} as Meta;

const Template: StoryFn = () => <SignUpPage />;

export const Primary = Template.bind({});