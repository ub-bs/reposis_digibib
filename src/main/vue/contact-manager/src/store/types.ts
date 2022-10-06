import { State as RequestState } from './request/state';
import { State as MainState } from './main/state';

export type RootState = {
  request: RequestState;
  main: MainState;
};
