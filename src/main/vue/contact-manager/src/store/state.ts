import { Request } from '../utils';

export type State = {
  loading: boolean;
  currentPage: number;
  totalRows: number;
  perPage: number;
  requests: Request[];
  currentRequest: Request | undefined;
  showRequestModal: boolean;
  editRecipientId: string | undefined;
  modalErrorCode: string | undefined;
  applicationErrorCode: string | undefined;
}

export const state: State = {
  loading: false,
  totalRows: 0,
  perPage: 8,
  requests: [],
  currentPage: 0,
  showRequestModal: false,
  editRecipientId: undefined,
  currentRequest: undefined,
  modalErrorCode: undefined,
  applicationErrorCode: undefined,
};
