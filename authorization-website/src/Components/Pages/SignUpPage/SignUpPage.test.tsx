import React from 'react';
import {
  render,
  RenderResult,
} from '@testing-library/react';
import { SignUpPage } from './SignUpPage';

describe('SignUpPage', () => {

  it('Should display the SignUpPage', () => {
    const renderResult = setupComponent();
    expect(renderResult.container).toMatchSnapshot();
  });

  const setupComponent = (): RenderResult => {
    return render(<SignUpPage />);
  }
})