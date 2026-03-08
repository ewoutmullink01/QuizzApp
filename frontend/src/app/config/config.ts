import { InjectionToken } from '@angular/core';

export type AppConfig = {
  apiBaseUrl: string;
};

export const APP_CONFIG = new InjectionToken<AppConfig>('APP_CONFIG');
