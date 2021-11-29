import { 
  Grid,
  IconButton, 
  InputAdornment,
  TextField,
  Typography,
  useTheme,
} from '@mui/material';
import React from 'react';
import {
  useFormik,
} from 'formik';
import * as yup from 'yup';
import { 
  Visibility, 
  VisibilityOff,
} from '@mui/icons-material';
import { LoadingButton } from '@mui/lab';

const validationSchema = yup.object({
  email: yup
    .string()
    .email()
    .required('Email is required'),
  password: yup
    .string()
    .min(8, 'Password must be 8 characters minimum')
    .max(20, 'Password cant be longer than 20 characters')
    .required('Password is required'),
});

export interface LogInFormProps {
  onLogIn: (username: string, password: string) => void;
  isLoading: boolean;
}

export const LogInForm: React.FunctionComponent<LogInFormProps> = ({
  onLogIn,
  isLoading,
}) => {

  const currentTheme = useTheme();
  const [hidePassword, setHidePassword] = React.useState<boolean>(true);
  const formik = useFormik({
    initialValues: {
      email: '',
      password: '',
    },
    validationSchema,
    onSubmit: (values) => {
      onLogIn(values.email, values.password);
    },
  });

  const getInputAdornment = (): React.ReactElement => (
    <InputAdornment position='end'>
      <IconButton 
        aria-label='toggle password visibility'
        onClick={(): void => setHidePassword(!hidePassword)}
        edge='end'
      >
        {hidePassword ? <Visibility /> : <VisibilityOff />}
      </IconButton>
    </InputAdornment>
  );

  const centeredStyle: React.CSSProperties = {
    flexGrow: '1',
    textAlign: 'center',
  };

  return (
    <Grid container spacing={2} marginTop={currentTheme.spacing(1)}>
      <Grid item xs={12}>
        <Typography 
          variant='body1'
          sx={centeredStyle}
        >
          Log into the Auth-Hub
        </Typography>
      </Grid>
      <Grid item xs={12}>
        <TextField 
          fullWidth
          id='email'
          name='email'
          label='Email'
          value={formik.values.email}
          onChange={formik.handleChange}
          error={formik.touched.email && Boolean(formik.errors.email)}
          helperText={formik.touched.email && formik.errors.email}
        />
      </Grid>
      <Grid item xs={12}>
        <TextField
          fullWidth
          id='password'
          name='password'
          label='Password'
          type={hidePassword ? 'password' : 'text'}
          value={formik.values.password}
          onChange={formik.handleChange}
          error={formik.touched.password && Boolean(formik.errors.password)}
          helperText={formik.touched.password && formik.errors.password}
          InputProps={{
            endAdornment: getInputAdornment(),
          }}
        />
      </Grid>
      <Grid item xs='auto' sx={centeredStyle}>
        <LoadingButton 
          variant='contained' 
          onClick={formik.submitForm}
          loading={isLoading}
        >
          Log In
        </LoadingButton>
      </Grid>
    </Grid>
  );
};