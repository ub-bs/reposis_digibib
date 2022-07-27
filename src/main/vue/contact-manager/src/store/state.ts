export type Recipient = {
  name: string;
  source: string;
  email: string;
}

export type Request = {
  id: number;
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
  expanded: number[];
  showModal: boolean;
  showRequestId: number | undefined;
};

export const state: State = {
  loading: false,
  totalRows: 0,
  perPage: 8,
  requests: [],
  currentPage: 0,
  expanded: [],
  showModal: false,
  showRequestId: undefined,
};
