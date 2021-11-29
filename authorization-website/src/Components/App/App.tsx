import React from 'react';
import {
  BrowserRouter, Route, Routes,
} from 'react-router-dom';
import { ProfileContext, ProfileState } from '../Context';
import { LandingPage, LogInPage, ProfilePage } from '../Pages';

const Application: React.FunctionComponent = () => {

  const profileState: ProfileState = React.useContext(ProfileContext);

  const getLandingPage = (): React.ReactElement => {
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
      </Routes>
    </BrowserRouter>
  );
};

export default Application;