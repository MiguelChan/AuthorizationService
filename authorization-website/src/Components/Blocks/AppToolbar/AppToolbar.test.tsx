import React from 'react';
import {
  render,
  RenderResult,
} from '@testing-library/react';
import { AppToolbar } from './AppToolbar';

describe('AppToolbar', () => {

  it('Should match the Snapshot', () => {
    const currentComponent = setupComponent();
    expect(currentComponent.container).toMatchSnapshot();
  });

  const setupComponent = (): RenderResult => {
    return render(<AppToolbar />);
  }
});