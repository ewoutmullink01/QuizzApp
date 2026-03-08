export type Question = {
  questionId: string;
  category: string;
  difficulty: string;
  type: string;
  question: string;
  options: string[];
};

export type QuizResponse = {
  quizId: string;
  questions: Question[];
};

export type AnswerRequest = {
  questionId: string;
  selectedAnswer: string;
};

export type CheckAnswersBatchRequest = {
  quizId: string;
  answers: AnswerRequest[];
};

export type AnswerResult = {
  questionId: string;
  correct: boolean;
  correctAnswer: string | null;
};

export type CheckAnswersBatchResponse = {
  quizId: string;
  score: number;
  total: number;
  results: AnswerResult[];
};
