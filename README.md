# Rolê Match 🍽️

Aplicativo mobile SaaS para descoberta inteligente de restaurantes, bares, cafeterias e espaços gastronômicos.

## 📱 Sobre o Projeto

O Rolê Match conecta usuários a estabelecimentos através de um sistema de match personalizado, cupons por QR Code e reservas integradas.

## 🗂️ Estrutura do Projeto

```
com.pentagono.rolematch
├── core/
│   ├── common/         # Dados mock e constantes
│   ├── navigation/     # Routes + AppNavGraph
│   ├── security/       # Segurança e tokens
│   ├── theme/          # Colors, Type, Theme
│   ├── utils/          # Utilitários
│   └── validators/     # CPF, CNPJ, Email, Senha, Telefone
├── data/
│   ├── model/          # Data models (Firestore)
│   ├── repository/     # Implementações de repositório
│   └── firebase/       # Configurações Firebase
├── domain/
│   ├── model/          # Domain models
│   ├── repository/     # Interfaces de repositório
│   └── usecase/        # Casos de uso
└── presentation/
    ├── components/     # CommonComponents, RestaurantComponents
    ├── screens/
    │   ├── auth/       # Splash, Login, Register, Verify, ForgotPassword
    │   ├── user/       # Home, Search, Match, Coupons, Profile, Settings
    │   ├── restaurant/ # Setup, Home, Dashboard, Coupons, Profile, QR Validator
    │   └── admin/      # Dashboard, Users, Restaurants, Coupons, Reports
    ├── state/          # UI States
    └── viewmodel/      # ViewModels
```

## 🎨 Design System

| Cor | Hex | Uso |
|-----|-----|-----|
| PrimaryGreen | #5F9F00 | Cor principal, botões, destaques |
| DarkGreen | #3F7300 | Headers, gradientes |
| DiscountRed | #F0142F | Cupons, alertas, badges |
| OrangeAccent | #FF6B00 | Gradiente do botão Match |
| BackgroundCream | #FFF8ED | Fundo principal |

## 🚀 Como Abrir

1. Abrir no **Android Studio Hedgehog+**
2. Aguardar sync do Gradle
3. Adicionar `google-services.json` em `app/`
4. Rodar em emulador API 26+

## 🔥 Configuração Firebase

1. Criar projeto no [Firebase Console](https://console.firebase.google.com)
2. Adicionar app Android com package `com.pentagono.rolematch`
3. Baixar `google-services.json` e colocar em `app/`
4. Ativar: **Authentication** (Email/Password), **Firestore**, **Storage**, **FCM**

## 📋 Telas Implementadas

### Módulo Usuário
- ✅ Splash Screen (animada)
- ✅ Login / Cadastro (com validação CPF)
- ✅ Verificação de e-mail / Esqueceu senha
- ✅ Home com categorias e listagem
- ✅ Buscar (com Mais Famosos, Favoritos, Perto, Eventos)
- ✅ Match Perfeito (filtros avançados)
- ✅ Detalhe do Restaurante (3 abas)
- ✅ Fazer Reserva
- ✅ Cupons & Promoções
- ✅ Detalhes do Cupom
- ✅ QR Code do Cupom
- ✅ Perfil (Reservas + Avaliações)
- ✅ Configurações

### Módulo Restaurante
- ✅ Login Restaurante
- ✅ Cadastro (com validação CNPJ)
- ✅ Setup em 5 etapas
- ✅ Home Restaurante
- ✅ Dashboard (métricas + gráfico)
- ✅ Gerenciar Cupons (com QR Code)
- ✅ Validador QR Code
- ✅ Preview do perfil público
- ✅ Perfil / Configurações

### Painel Admin
- ✅ Dashboard com métricas
- ✅ Gerenciar Usuários
- ✅ Gerenciar Restaurantes (aprovação)
- ✅ Gerenciar Cupons
- ✅ Relatórios e Denúncias

## 🔐 Segurança

- Validação de CPF e CNPJ com algoritmo completo
- Senha forte (8+ chars, maiúscula, minúscula, número, especial)
- QR Code único por usuário/cupom com hash
- Status `PENDING_REVIEW` para novos restaurantes
- RBAC: USER / RESTAURANT / ADMIN
- Logs de validação de cupom

## 🛠️ Próximos Passos

- [ ] Integrar Firebase Authentication
- [ ] Implementar Firestore repositories
- [ ] Cloud Functions para validação segura de QR Code
- [ ] Firebase Storage para fotos
- [ ] Push notifications com FCM
- [ ] Geolocalização real
- [ ] Match Engine com ML

## 📦 Versão

`1.0.0` — Base SaaS estruturada, dados mock, pronta para integração Firebase
