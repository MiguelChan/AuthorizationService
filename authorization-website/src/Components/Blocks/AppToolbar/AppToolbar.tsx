import { Button, Toolbar, Typography } from '@mui/material';
import React from 'react';

export const AppToolbar: React.FunctionComponent = () => {
  return (
    <Toolbar>
      <Typography variant='h6' component='div' sx={{ flexGrow: 1 }}>
        Auth-Hub
      </Typography>
      <Button color='inherit'>Login</Button>
    </Toolbar>
  );
};