import { Routes } from '@angular/router';
import { Menu } from './menu/menu';
import { Quiz } from './quiz/quiz';
import { quizResolver } from './quiz/quiz.resolver';

export const routes: Routes = [
  { path: '', component: Menu},
  { path: 'quiz', component: Quiz, resolve: { quiz: quizResolver } },
];
