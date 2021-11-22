import React from 'react';
import {
  Story,
  Meta,
} from '@storybook/react';
import { SignUpForm, SignUpFormProps } from './SignUpForm';

export default {
  title: 'Components/Blocks/SignUpForm',
  component: SignUpForm,
  argTypes: {
    onSignUpRequestCreatedListener: {
      defaultValue: 'onSignUpRequestCreatedListener',
      description: 'onSignUpRequestCreatedListener',
      name: 'onSignUpRequestCreatedListener',
    },
  },
} as Meta;

const Template: Story<SignUpFormProps> = (args) => <SignUpForm {...args} />;

export const Primary = Template.bind({});