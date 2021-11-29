import { Typography } from '@mui/material';
import React from 'react';
import { AppToolbar } from '../../Blocks';
import { ProfileContext, ProfileState } from '../../Context';
import { SimpleAppTemplate } from '../../Templates';

export const ProfilePage: React.FunctionComponent = () => {

  const profileState: ProfileState = React.useContext(ProfileContext);

  const getAppToolbar = (): React.ReactElement => {
    return <AppToolbar />
  };

  const getAppContent = (): React.ReactElement => {
    const currentProfile = profileState.getProfile();
    return (
      <Typography>
        {`${currentProfile!.firstName} ${currentProfile!.lastName}`}
      </Typography>
    );
  };

  return (
    <SimpleAppTemplate 
      applicationToolbar={getAppToolbar()}
      applicationContent={getAppContent()}
    />
  );
};