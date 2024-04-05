export const ORIGIN_MANUAL = 'manual';

export const ORIGIN_FALLBACK = 'fallback';

export enum RequestState {
  RECEIVED = 0,
  PROCESSED = 10,
  FORWARDING = 20,
  FORWARDED = 30,
  CONFIRMED = 40,
}

export namespace RequestState {
  export function toString(state: RequestState): string {
    return RequestState[state];
  }
}

export enum PersonEventType {
  SENT = 'SENT',
  SENT_FAILED = 'SENT_FAILED',
  CONFIRMED = 'CONFIRMED',
  REFUSED = 'REFUSED',
}

export type PersonEvent = {
  date: Date;
  type: PersonEventType;
}

export type ContactPerson = {
  name: string;
  email: string;
  origin: string;
  events: PersonEvent[];
}

export type RequestBody = {
  name: string;
  email: string;
  message: string;
  orcid?: string;
}

export type Request = {
  id: string;
  objectId: string;
  body: RequestBody;
  created: Date;
  state: RequestState;
  contactPersons: ContactPerson[];
  comment: string;
}

export type ErrorResponse = {
  errorCode: string;
}

export const compareEvents = (a: PersonEvent, b: PersonEvent): number => {
  if (a.date < b.date) {
    return -1;
  }
  if (a.date > b.date) {
    return 1;
  }
  return 0;
};
