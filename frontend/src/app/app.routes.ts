// O mapa de endereço para tela
import { Routes } from '@angular/router';

import { Inicio } from './pages/inicio/inicio';
import { Identificacao } from './pages/identificacao/identificacao';
import { Modelo } from './pages/modelo/modelo';
import { Resultado } from './pages/resultado/resultado';

export const routes: Routes = [
  { path: '', component: Inicio },
  { path: 'simular', component: Identificacao },
  { path: 'modelo', component: Modelo },
  { path: 'resultado', component: Resultado },
  { path: '**', redirectTo: '' },
];
