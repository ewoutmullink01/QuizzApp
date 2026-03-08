import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { TriviaApi } from '../util/travi-api'; // jouw pad
import { QuizResponse } from '../models/models'; // jouw pad

export const quizResolver: ResolveFn<QuizResponse> = async () => {
  const api = inject(TriviaApi);
  return await firstValueFrom(api.getQuiz(5)); // default amount = 5
};
