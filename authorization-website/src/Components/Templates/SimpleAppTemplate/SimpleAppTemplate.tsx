import { AppBar, Container, Paper, useTheme } from '@mui/material';
import React from 'react';

export interface SimpleAppTemplateProps {
  applicationToolbar?: React.ReactNode;
  applicationContent?: React.ReactNode;
}

export const SimpleAppTemplate: React.FunctionComponent<SimpleAppTemplateProps> = ({
  applicationToolbar,
  applicationContent,
}) => {

  const currentTheme = useTheme();

  const getApplicationToolbar = (): React.ReactNode => {
    if (applicationToolbar === null || applicationToolbar === undefined) {
      return (
        <>ApplicationToolbarPosition</>
      );
    }

    return applicationToolbar!;
  };

  const getApplicationContent = (): React.ReactNode => {
    if (applicationContent === null || applicationContent === undefined) {
      return (
        <>ApplicatonContentPosition</>
      );
    }
    return applicationContent!;
  }

  return (
    <>
      <AppBar position='static'>
        {getApplicationToolbar()}
      </AppBar>
      <Container maxWidth='xl' sx={{marginTop: currentTheme.spacing(2)}}>
        <Paper sx={{padding: currentTheme.spacing(1)}}>
          {getApplicationContent()}
        </Paper>
      </Container>
    </>
  );
};