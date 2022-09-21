import { State as ModalState } from './modal/state';
import { State as MainState } from './main/state';

export type RootState = {
  modal: ModalState;
  main: MainState;
};
