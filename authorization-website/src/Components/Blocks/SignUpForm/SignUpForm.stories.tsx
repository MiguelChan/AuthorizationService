import React from 'react';
import {
  StoryFn,
  Meta,
} from '@storybook/react-vite';
import { SignUpForm, SignUpFormProps } from './SignUpForm';

export default {
  title: 'Components/Blocks/SignUpForm',
  component: SignUpForm,
  argTypes: {
    onSignUpRequestCreatedListener: {
      description: 'onSignUpRequestCreatedListener',
      name: 'onSignUpRequestCreatedListener',
    },
  },
} as Meta;

const Template: StoryFn<SignUpFormProps> = (args) => <SignUpForm {...args} />;

export const Primary = Template.bind({});
Primary.args = { onSignUpRequestCreatedListener: () => undefined };
