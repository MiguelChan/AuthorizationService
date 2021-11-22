import React from 'react';
import { SignUpRequest } from '../../../Models';
import { 
  SignUpForm,
  AppToolbar,
} from '../../Blocks';
import {
  SimpleAppTemplate,
} from '../../Templates';

export const SignUpPage: React.FunctionComponent = () => {

  const onSignUpRequestCreated = (signUpRequest: SignUpRequest): void => {
    console.info(JSON.stringify(signUpRequest));
  }

  const renderToolbar = (): React.ReactElement => {
    return (
      <AppToolbar />
    );
  }

  const renderSignUpForm = (): React.ReactElement => {
    return (
      <SignUpForm onSignUpRequestCreatedListener={onSignUpRequestCreated} />
    );
  }

  return (
    <SimpleAppTemplate 
      applicationToolbar={renderToolbar()} 
      applicationContent={renderSignUpForm()}
    />
  );
};