import React from 'react';
import { Alert, Button } from '@mui/material';
import { Link } from 'react-router-dom';
import { SignUpRequest } from '../../../Models';
import { SignUpForm, AppToolbar } from '../../Blocks';
import { SimpleAppTemplate } from '../../Templates';
import { useSignUp } from '../../Hooks/UseSignUp/UseSignUp';

export const SignUpPage: React.FunctionComponent = () => {
  const state = useSignUp();
  const submit = (request: SignUpRequest) => state.onSignUpRequested(request.firstName,
    request.lastName, request.phoneNumber, request.emailAddress, request.password);
  return <SimpleAppTemplate applicationToolbar={<AppToolbar />} applicationContent={
    state.isProfileCreated ? <>
      <Alert severity='success'>Account created. You can now log in.</Alert>
      <Button component={Link} to='/login'>Go to login</Button>
    </> : <>
      <SignUpForm onSignUpRequestCreatedListener={submit} isLoading={state.isCreatingAccount} />
      {state.hasError && <Alert severity='error'>Unable to create your account. Check your details or try a different email.</Alert>}
    </>
  } />;
};
