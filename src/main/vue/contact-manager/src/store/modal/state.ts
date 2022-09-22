import { Request } from '../../utils';

export type State = {
  currentRequest: Request | undefined;
  showRequestModal: boolean;
  errorCode: string | undefined;
  infoCode: string | undefined;
}

export const state: State = {
  currentRequest: undefined,
  showRequestModal: false,
  errorCode: undefined,
  infoCode: undefined,
};
