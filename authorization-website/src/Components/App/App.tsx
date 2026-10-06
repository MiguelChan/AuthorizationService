import React from 'react';
import { Typography } from '@mui/material';
import { AppToolbar } from '../Blocks';
import { SimpleAppTemplate } from '../Templates';
import {
  BrowserRouter, Route, Routes,
} from 'react-router-dom';
import { ProfileContext, ProfileState } from '../Context';
import { LandingPage, LogInPage, ProfilePage, SignUpPage } from '../Pages';

const Application: React.FunctionComponent = () => {

  const profileState: ProfileState = React.useContext(ProfileContext);

  const getLandingPage = (): React.ReactElement => {
    if (profileState.isCheckingSession) {
      return <SimpleAppTemplate applicationToolbar={<AppToolbar />}
        applicationContent={<Typography role='status'>Checking your session...</Typography>} />;
    }
    const currentProfile = profileState.getProfile();

    if (currentProfile !== undefined) {
      return <ProfilePage />;
    }

    return <LandingPage />;
  };

  return (
    <BrowserRouter>
      <Routes>
        <Route path='/' element={getLandingPage()} />
        <Route path='/login' element={<LogInPage />} />
        <Route path='/register' element={<SignUpPage />} />
      </Routes>
    </BrowserRouter>
  );
};

export default Application;