import { Request } from '../../utils';

export type State = {
  currentRequest: Request | undefined;
  showRequestModal: boolean;
  editRecipientId: string | undefined;
  modalErrorCode: string | undefined;
  modalInfoCode: string | undefined;
}

export const state: State = {
  currentRequest: undefined,
  showRequestModal: false,
  editRecipientId: undefined,
  modalErrorCode: undefined,
  modalInfoCode: undefined,
};
