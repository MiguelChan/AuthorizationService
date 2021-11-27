import React from 'react';
import {
  Story,
  Meta,
} from '@storybook/react';
import { LogInForm, LogInFormProps } from './LogInForm';

export default {
  title: 'Components/Blocks/LogInForm',
  component: LogInForm,
  argTypes: {
    onLogIn: {
      defaultValue: 'onLogIn',
      description: 'onLogIn',
      name: 'onLogIn',
    }
  }
} as Meta;

const Template: Story<LogInFormProps> = (args) => <LogInForm {...args} />;

export const Primary = Template.bind({});