import { CheckAnswersBatchResponse, Question } from '../models/models';

export type QuizStatus = 'idle' | 'loading' | 'ready' | 'submitting' | 'done' | 'error';

export type QuizVm = {
  status: QuizStatus;
  error: string | null;

  quizId: string | null;
  questions: Question[];
  selected: Record<string, string>;

  result: CheckAnswersBatchResponse | null;
};

export const initialQuizVm: QuizVm = {
  status: 'idle',
  error: null,
  quizId: null,
  questions: [],
  selected: {},
  result: null,
};
