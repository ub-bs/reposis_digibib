export type Request = {
  id: number;
  name: string;
  email: string;
  orcid?: string;
  created: Date;
  state: string;
  message: string;
  objectID: string;
}

export type State = {
  loading: boolean;
  currentPage: number;
  totalRows: number;
  perPage: number;
  requests: Request[];
  expanded: number[];
};

export const state: State = {
  loading: false,
  totalRows: 0,
  perPage: 8,
  requests: [],
  currentPage: 0,
  expanded: [],
};
