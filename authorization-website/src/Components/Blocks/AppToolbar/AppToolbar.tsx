import { Button, Toolbar, Typography } from '@mui/material';
import React from 'react';
import { Link } from 'react-router-dom';
import { ProfileContext, ProfileState } from '../../Context';

export const AppToolbar: React.FunctionComponent = () => {

  const profileContext: ProfileState = React.useContext(ProfileContext);

  const renderAddons = (): React.ReactElement => {
    const profile = profileContext.getProfile();
    
    if (profile === undefined) {
      return <Button color='inherit' component={Link} to='/login'>Login</Button>;
    }

    return (
      <Typography>
        Welcome back, {`${profile.firstName} ${profile.lastName}`}!
      </Typography>
    );
  };

  return (
    <Toolbar>
      <Typography variant='h6' component='div' sx={{ flexGrow: 1 }}>
        Auth-Hub
      </Typography>
      {renderAddons()}
    </Toolbar>
  );
};