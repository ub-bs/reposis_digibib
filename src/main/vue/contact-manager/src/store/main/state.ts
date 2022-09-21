import { Request } from '../../utils';

export type State = {
  isBooted: boolean;
  loading: boolean;
  errorCode: string | undefined;
  currentPage: number;
  perPage: number;
  totalRows: number;
  requests: Request[];
}

export const state: State = {
  isBooted: false,
  loading: false,
  errorCode: undefined,
  currentPage: 0,
  perPage: 8,
  totalRows: 0,
  requests: [],
};
