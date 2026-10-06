import React from 'react';
import {
  StoryFn,
  Meta,
} from '@storybook/react-vite';
import { LogInForm, LogInFormProps } from './LogInForm';

export default {
  title: 'Components/Blocks/LogInForm',
  component: LogInForm,
  argTypes: {
    onLogIn: {
      description: 'onLogIn',
      name: 'onLogIn',
    }
  }
} as Meta;

const Template: StoryFn<LogInFormProps> = (args) => <LogInForm {...args} />;

export const Primary = Template.bind({});
Primary.args = { onLogIn: () => undefined };
