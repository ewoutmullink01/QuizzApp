import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CheckAnswersBatchRequest, CheckAnswersBatchResponse, QuizResponse, } from '../models/models';
import { APP_CONFIG } from '../config/config';


@Injectable({ providedIn: 'root' })
export class TriviaApi {
  private http = inject(HttpClient);
  private config = inject(APP_CONFIG);

  getQuiz(amount: number): Observable<QuizResponse> {
    const params = new HttpParams().set('amount', amount);
    return this.http.get<QuizResponse>(`${this.config.apiBaseUrl}/questions`, { params });
  }

  checkAnswers(body: CheckAnswersBatchRequest): Observable<CheckAnswersBatchResponse> {
    return this.http.post<CheckAnswersBatchResponse>(`${this.config.apiBaseUrl}/checkanswers`, body);
  }
}
