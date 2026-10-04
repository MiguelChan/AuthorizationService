import { Button, Grid, TextField, Typography } from '@mui/material';
import React from 'react';
import { useFormik } from 'formik';
import * as yup from 'yup';
import { SignUpRequest } from '../../../Models';

const validationSchema = yup.object({
  firstName: yup.string().trim().required('First name is required'),
  lastName: yup.string().trim().required('Last name is required'),
  phoneNumber: yup.string().matches(/^[0-9]{10}$/, 'Enter a 10-digit phone number').required('Phone number is required'),
  email: yup.string().trim().email('Enter a valid email').required('Email is required'),
  password: yup.string().min(8, 'Password must be at least 8 characters').max(20, 'Password must be at most 20 characters')
    .matches(/[a-z]/, 'Include a lowercase letter').matches(/[A-Z]/, 'Include an uppercase letter')
    .matches(/[0-9]/, 'Include a number').matches(/[!@#&()–[{}\]:;',?/*~$^+=<>]/, 'Include a special character')
    .required('Password is required'),
  passwordConfirm: yup.string().required('Confirm your password').oneOf([yup.ref('password')], 'Passwords must match'),
});
export interface SignUpFormProps {
  onSignUpRequestCreatedListener: (request: SignUpRequest) => void;
  isLoading?: boolean;
}
export const SignUpForm: React.FunctionComponent<SignUpFormProps> = ({ onSignUpRequestCreatedListener, isLoading = false }) => {
  const formik = useFormik({
    initialValues: { firstName: '', lastName: '', phoneNumber: '', email: '', password: '', passwordConfirm: '' },
    validationSchema,
    onSubmit: values => onSignUpRequestCreatedListener({ firstName: values.firstName.trim(), lastName: values.lastName.trim(),
      phoneNumber: values.phoneNumber, emailAddress: values.email.trim(), password: values.password }),
  });
  const fields = [
    ['firstName', 'First name', 'text', 'given-name'], ['lastName', 'Last name', 'text', 'family-name'],
    ['phoneNumber', 'Phone number', 'tel', 'tel-national'], ['email', 'Email', 'email', 'email'],
    ['password', 'Password', 'password', 'new-password'], ['passwordConfirm', 'Confirm password', 'password', 'new-password'],
  ];
  return <form onSubmit={formik.handleSubmit} noValidate>
    <Typography variant='h6'>Register to the Auth-Hub</Typography>
    <Grid container spacing={3}>
      {fields.map(([name, label, type, autoComplete]) => {
        const key = name as keyof typeof formik.values;
        return <Grid item xs={12} sm={6} key={name}>
          <TextField fullWidth id={name} name={name} label={label} type={type} autoComplete={autoComplete}
            disabled={isLoading} value={formik.values[key]} onChange={formik.handleChange} onBlur={formik.handleBlur}
            error={Boolean(formik.touched[key] && formik.errors[key])}
            helperText={formik.touched[key] && formik.errors[key]} />
        </Grid>;
      })}
    </Grid>
    <Button color='primary' variant='contained' type='submit' disabled={isLoading}>
      {isLoading ? 'Creating account...' : 'Create Account'}
    </Button>
    <Button type='button' disabled={isLoading} onClick={() => formik.resetForm()}>Clear form</Button>
  </form>;
};
