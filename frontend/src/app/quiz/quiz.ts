import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { TriviaApi } from '../util/travi-api';
import { AnswerRequest, CheckAnswersBatchResponse, Question } from '../models/models';
import { firstValueFrom } from 'rxjs';
import { initialQuizVm, QuizVm } from './quiz.state';
import { ActivatedRoute, Router } from '@angular/router';


@Component({
  selector: 'app-quiz',
  imports: [],
  templateUrl: './quiz.html',
  styleUrl: './quiz.css',
})


export class Quiz implements OnInit {
  private api = inject(TriviaApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  ngOnInit(): void {
    const res = this.route.snapshot.data['quiz'];
    if (res) {
      this.vm.set({
        status: 'ready',
        error: null,
        quizId: res.quizId,
        questions: res.questions,
        selected: {},
        result: null,
      });
    }
  }

  goBack() {
    this.router.navigate(['/']);
  }

  vm = signal<QuizVm>(initialQuizVm);

  allAnswered = computed(() => {
    const { questions, selected } = this.vm();
    return questions.length > 0 && questions.every(q => !!selected[q.questionId]);
  });

  submitDisabled = computed(() => {
    const { status, questions, result } = this.vm();
    return status === 'loading'
      || status === 'submitting'
      || questions.length === 0
      || !this.allAnswered()
      || result !== null;
  });

  async load(amount = 5) {
    this.vm.update(v => ({ ...v, status: 'loading', error: null, result: null }));

    try {
      const res = await firstValueFrom(this.api.getQuiz(amount));
      this.vm.set({
        status: 'ready',
        error: null,
        quizId: res.quizId,
        questions: res.questions,
        selected: {},
        result: null,
      });
    } catch (e: any) {
      this.vm.update(v => ({ ...v, status: 'error', error: e?.message ?? 'Kon quiz niet laden' }));
    }
  }

  selectAnswer(questionId: string, option: string) {
    this.vm.update(v => ({ ...v, selected: { ...v.selected, [questionId]: option } }));
  }

  async submit() {
    const { quizId, selected } = this.vm();

    if (!quizId) {
      this.vm.update(v => ({ ...v, error: 'Geen actieve quiz. Klik op "Nieuwe quiz".' }));
      return;
    }
    if (!this.allAnswered()) {
      this.vm.update(v => ({ ...v, error: 'Beantwoord eerst alle vragen.' }));
      return;
    }

    this.vm.update(v => ({ ...v, status: 'submitting', error: null }));

    const answers: AnswerRequest[] = Object.entries(selected)
      .map(([questionId, selectedAnswer]) => ({ questionId, selectedAnswer }));

    try {
      const res = await firstValueFrom(this.api.checkAnswers({ quizId, answers }));
      this.vm.update(v => ({ ...v, status: 'done', result: res }));
      this.scrollToTop();
    } catch (e: any) {
      this.vm.update(v => ({ ...v, status: 'error', error: e?.message ?? 'Kon antwoorden niet checken' }));
    }
  }

  resultByQuestionId = computed(() => {
    const r = this.vm().result;
    if (!r) return {};
    return Object.fromEntries(r.results.map(x => [x.questionId, x.correct])) as Record<string, boolean>;
  });

  correctAnswerByQuestionId = computed(() => {
    const r = this.vm().result;
    if (!r) return {};
    return Object.fromEntries(
      r.results.map(x => [x.questionId, x.correctAnswer])
    ) as Record<string, string | null>;
  });

  isLocked = computed(() => this.vm().result !== null || this.vm().status === 'submitting');

  private scrollToTop() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }
}
