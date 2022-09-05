export enum Origin {
  Manual = 'MANUAL',
}

export type Recipient = {
  name: string;
  origin: string;
  email: string;
  enabled: boolean;
}

export type Request = {
  uuid: string;
  name: string;
  email: string;
  orcid?: string;
  created: Date;
  state: string;
  message: string;
  objectID: string;
  recipients: Recipient[];
}

export type ErrorResponse = {
  errorCode: string;
}

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
};
