import { Button, Grid, TextField, Typography, useTheme } from '@mui/material';
import React from 'react';
import { useFormik } from 'formik';
import * as yup from 'yup';
import 'yup-phone';
import {
  SignUpRequest,
} from '../../../Models';

const validationSchema = yup.object({
  firstName: yup
    .string()
    .required('First name is required'),
  lastName: yup
    .string()
    .required('Last name is required'),
  phoneNumber: yup
    .string()
    .phone()
    .required(),
  email: yup
    .string()
    .email()
    .required('Email is required'),
  password: yup.string()
    .min(8, 'Password must be 8 characters minimum')
    .max(20, 'Password cant be longer than 20 characters')
    .required('Password is required'),
  passwordConfirm: yup
    .string()
    .oneOf([yup.ref('password'), null], 'Passwords must match'),
});

export interface SignUpFormProps {
  onSignUpRequestCreatedListener: (signUpRequest: SignUpRequest) => void;
}

export const SignUpForm: React.FunctionComponent<SignUpFormProps> = ({
  onSignUpRequestCreatedListener,
}) => {

  const currentTheme = useTheme();

  const formik = useFormik({
    initialValues: {
      firstName: '',
      lastName: '',
      phoneNumber: '',
      password: '',
      passwordConfirm: '',
      email: '',
    },
    validationSchema,
    onSubmit: (values) => {
      onSignUpRequestCreatedListener(values);
    },
  });

  const renderButtons = (): React.ReactElement => (
    <>
      <Grid container spacing={2} marginTop={currentTheme.spacing(1)}>
        <Grid item xs='auto'>
          <Button 
            color='primary' 
            variant='contained' 
            type='submit'
            onClick={(): any => formik.submitForm()}
          >
            Create Account
          </Button>
        </Grid>
        <Grid item xs='auto'>
          <Button color='secondary' variant='contained'>Cancel Account Creation</Button>
        </Grid>
      </Grid>
    </>
  );

  const renderTextField = (
    id: string, 
    name: string, 
    label: string,
    currentValue: string,
    hasError: any,
    errorMessage: any,
  ): React.ReactElement => {
    return (
      <TextField 
        fullWidth
        id={id}
        name={name}
        label={label}
        value={currentValue}
        onChange={formik.handleChange}
        error={hasError}
        helperText={errorMessage}
      />
    );
  };

  const renderForm = (): React.ReactElement => (
    <>
      <Grid container spacing={3}>
        <Grid item xs={6}>
          {renderTextField(
            'firstName', 
            'firstName', 
            'First name', 
            formik.values.firstName, 
            formik.touched.firstName && Boolean(formik.errors.firstName), 
            formik.touched.firstName && formik.errors.firstName)}
        </Grid>
        <Grid item xs={6}>
          {renderTextField(
            'lastName', 
            'lastName', 
            'Last name', 
            formik.values.lastName, 
            formik.touched.lastName && Boolean(formik.errors.lastName), 
            formik.touched.lastName && formik.errors.lastName)}
        </Grid>
        <Grid item xs={6}>
          {renderTextField(
              'phoneNumber', 
              'phoneNumber', 
              'Phone Number', 
              formik.values.phoneNumber, 
              formik.touched.phoneNumber && Boolean(formik.errors.phoneNumber), 
              formik.touched.phoneNumber && formik.errors.phoneNumber)}
        </Grid>
        <Grid item xs={6}>
          {renderTextField(
            'email', 
            'email', 
            'Email ', 
            formik.values.email, 
            formik.touched.email && Boolean(formik.errors.email), 
            formik.touched.email && formik.errors.email)}
        </Grid>
        <Grid item xs={6}>
          {renderTextField(
            'password', 
            'password', 
            'Password', 
            formik.values.password, 
            formik.touched.password && Boolean(formik.errors.password), 
            formik.touched.password && formik.errors.password)}
        </Grid>
        <Grid item xs={6}>
          {renderTextField(
            'passwordConfirm', 
            'passwordConfirm', 
            'Confirm password', 
            formik.values.passwordConfirm, 
            formik.touched.passwordConfirm && Boolean(formik.errors.passwordConfirm), 
            formik.touched.passwordConfirm && formik.errors.passwordConfirm)}
        </Grid>
      </Grid>
    </>
  );

  const renderFormTitle = (): React.ReactElement => (
    <Grid container spacing={3}>
      <Grid item xs={12}>
        <Typography variant='h6' component='div' sx={{ flexGrow: 1 }}>Register to the Auth-Hub</Typography>
      </Grid>
    </Grid>
  );

  return (
    <>
      {renderFormTitle()}
      {renderForm()}
      {renderButtons()}
    </>
  );
};