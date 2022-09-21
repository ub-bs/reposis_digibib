import { Request } from '../../utils';

export type State = {
  currentRequest: Request | undefined;
  showRequestModal: boolean;
  editRecipientId: string | undefined;
  errorCode: string | undefined;
  infoCode: string | undefined;
}

export const state: State = {
  currentRequest: undefined,
  showRequestModal: false,
  editRecipientId: undefined,
  errorCode: undefined,
  infoCode: undefined,
};
