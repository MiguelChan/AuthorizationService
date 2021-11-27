import React from 'react';
import {
  render,
  RenderResult,
} from '@testing-library/react';
import { SignUpPage } from './SignUpPage';
import { BrowserRouter } from 'react-router-dom';

describe('SignUpPage', () => {

  it('Should display the SignUpPage', () => {
    const renderResult = setupComponent();
    expect(renderResult.container).toMatchSnapshot();
  });

  const setupComponent = (): RenderResult => {
    return render(
      <BrowserRouter>
        <SignUpPage />
      </BrowserRouter>
    );
  }
})