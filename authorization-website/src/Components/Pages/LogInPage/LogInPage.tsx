import { Typography } from '@mui/material';
import React from 'react';
import { 
  AppToolbar,
  LogInForm,
} from '../../Blocks';
import { ProfileContext, ProfileState } from '../../Context';
import { useLogIn, UseLogInState } from '../../Hooks';
import { SimpleAppTemplate } from '../../Templates';

export const LogInPage: React.FunctionComponent = () => {

  const profileContext: ProfileState = React.useContext(ProfileContext);

  const useLogInState: UseLogInState = useLogIn();

  React.useEffect(() => {
    const {
      currentProfile,
    } = useLogInState;

    if (currentProfile !== undefined) {
      profileContext.setCurrentProfile(currentProfile);
    }

  }, [profileContext, useLogInState]);

  const renderErrorMessage = (): React.ReactElement => {
    if (useLogInState.hasError) {
      return (
        <Typography 
          variant='subtitle2'
          color='red'
          component='b'
        >
          Invalid username or password
        </Typography>
      );
    }

    return <></>;
  };

  const getApplicationContent = (): React.ReactElement => {
    return (
      <>
        <LogInForm 
          onLogIn={useLogInState.onLogInRequested} 
          isLoading={useLogInState.isLoggingIn}
          />
        {renderErrorMessage()}
      </>
    );
  }

  const getApplicationToolbar = (): React.ReactElement => (
    <AppToolbar />
  );

  return (
    <SimpleAppTemplate 
      applicationContent={getApplicationContent()}
      applicationToolbar={getApplicationToolbar()}
    />
  );
};