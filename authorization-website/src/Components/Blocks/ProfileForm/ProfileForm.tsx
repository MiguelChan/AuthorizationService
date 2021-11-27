import { Grid, TextField, useTheme } from '@mui/material';
import React from 'react';
import { Profile } from '../../../Models';

export interface ProfileFormProps {
  profile: Profile;
}

export const ProfileForm: React.FunctionComponent<ProfileFormProps> = ({
  profile,
})=> {

  const currentTheme = useTheme();

  const {
    firstName,
    lastName,
    phoneNumber,
  } = profile;

  return (
    <Grid container spacing={2} marginTop={currentTheme.spacing(1)}>
      <Grid container>
        <Grid item xs='auto'>
          <TextField fullWidth disabled value={firstName} label='First Name' />
        </Grid>
        <Grid item xs='auto'>
          <TextField fullWidth disabled value={lastName} label='Last Name' />
        </Grid>
        <Grid item xs='auto'>
          <TextField fullWidth disabled value={phoneNumber} label='Phone Number' />
        </Grid>
      </Grid>
    </Grid>
  ); 
}