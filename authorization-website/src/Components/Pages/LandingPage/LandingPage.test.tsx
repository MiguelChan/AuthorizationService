import React from 'react';
import {
  render,
  RenderResult,
} from '@testing-library/react';
import { LandingPage } from './LandingPage';
import { BrowserRouter } from 'react-router-dom';

describe('LandingPage', () => {

  it('Should display the LandingPage', () => {
    const renderResult = setupComponent();
    expect(renderResult.container).toMatchSnapshot();
  });

  const setupComponent = (): RenderResult => {
    return render(
      <BrowserRouter>
        <LandingPage />
      </BrowserRouter>
    );
  }

});