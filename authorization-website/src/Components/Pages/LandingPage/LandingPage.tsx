import { Container, Typography } from '@mui/material';
import React from 'react';
import { AppToolbar } from '../../Blocks';
import { SimpleAppTemplate } from '../../Templates';

/**
 * .
 */
export const LandingPage: React.FunctionComponent = () => {

  const getApplicationToolbar = (): React.ReactElement => (
    <AppToolbar />
  );

  const getApplicationContent = (): React.ReactElement => (
    <Container>
      <Typography variant='h6'>Welcome to the Auth-Hub</Typography>
      <Typography variant='body1'>We're currently working on adding more features to the Website.</Typography>
      <Typography variant='body1'>Be patient until we finish.</Typography>
    </Container>
  );

  return (
    <SimpleAppTemplate 
      applicationToolbar={getApplicationToolbar()}
      applicationContent={getApplicationContent()}
    />
  );
};