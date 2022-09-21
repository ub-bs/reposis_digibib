import { Request } from '../../utils';

export type State = {
  loading: boolean;
  currentPage: number;
  totalRows: number;
  perPage: number;
  requests: Request[];
  errorCode: string | undefined;
}

export const state: State = {
  loading: false,
  totalRows: 0,
  perPage: 8,
  requests: [],
  currentPage: 0,
  errorCode: undefined,
};
