export enum Origin {
  Manual = 'MANUAL',
}

export type Recipient = {
  name: string;
  origin: string;
  email: string;
  state: boolean;
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

export type State = {
  loading: boolean;
  currentPage: number;
  totalRows: number;
  perPage: number;
  requests: Request[];
  showModal: boolean;
  showRequestId: string | undefined;
  editRecipientId: string | undefined;
};

export const state: State = {
  loading: false,
  totalRows: 0,
  perPage: 8,
  requests: [],
  currentPage: 0,
  showModal: false,
  showRequestId: undefined,
  editRecipientId: undefined,
};
