# Segurança – Rolê Match

## Princípios

O Rolê Match foi projetado com **secure-by-design**. Toda validação crítica ocorre no backend (Cloud Functions + Firestore Rules); o app Android é apenas a camada de apresentação.

---

## Autenticação

| Mecanismo | Implementação |
|-----------|---------------|
| Login | Firebase Auth (email + senha) |
| Hashing de senha | Firebase Auth (bcrypt internamente) |
| Sessão | Firebase ID Token (JWT, 1h) + Refresh Token |
| MFA | Firebase Auth Multi-Factor (a habilitar) |

### Rate Limiting (client-side)
- `SecurityManager.canAttemptLogin()` bloqueia após **5 tentativas** em 15 min
- Backend (Cloud Function `logSecurityEvent`) registra e pode bloquear no servidor

---

## Autorização

**Roles:** `USER` | `RESTAURANT` | `ADMIN`

| Ação | USER | RESTAURANT | ADMIN |
|------|------|------------|-------|
| Ver restaurantes ativos | ✅ | ✅ | ✅ |
| Ver próprio perfil | ✅ | ✅ | ✅ |
| Editar próprio restaurante | ❌ | ✅ (próprio) | ✅ |
| Aprovar restaurante | ❌ | ❌ | ✅ |
| Validar QR Code | ❌ | ✅ (próprio rest.) | ✅ |
| Marcar cupom como usado | ❌ | Via backend | ✅ |
| Bloquear usuário | ❌ | ❌ | ✅ |
| Ver logs | ❌ | parcial | ✅ |

---

## Sistema de Cupons por QR Code

```
Usuário toca "Gerar QR Code"
  → App chama Cloud Function issueCouponQrCode
  → CF valida elegibilidade (não expirado, não usado, dentro do limite)
  → CF gera token: base64(couponId|userId|restaurantId|issuedAt|expiresAt|HMAC-SHA256)
  → CF salva em userCoupons com status=AVAILABLE
  → App exibe QR Code com o token

Restaurante escaneia QR Code
  → App chama Cloud Function validateCouponQrCode
  → CF decodifica e verifica HMAC
  → CF checa: restaurantId correto, não expirado, não usado, não bloqueado
  → Se válido: retorna dados do cupom para confirmação
  → Operador confirma → CF marca como USED + salva log em restaurantValidationLogs
```

### Proteções contra fraude
- Token é **HMAC-SHA256** assinado com chave secreta no backend
- O app **nunca** tem acesso à chave de assinatura
- Um token usado não pode ser reutilizado (status USED)
- Cada token tem `expiresAt` curto (ex: 5 minutos após geração)
- Validação sempre ocorre no **backend**, nunca apenas no app

---

## Dados Sensíveis

| Dado | Onde armazenado | Proteção |
|------|----------------|----------|
| Senha | Firebase Auth | bcrypt (gerenciado pela Firebase) |
| CPF | Firestore (criptografado) | Não exibir em logs |
| CNPJ | Firestore | Validado e mascarado na UI |
| Tokens QR | Firestore | HMAC assinado, TTL curto |

**Nunca armazenar em SharedPreferences não-criptografado ou logs.**

---

## Firestore Rules

Ver `firestore.rules`. Pontos principais:
- Nenhuma escrita de `status` por usuários comuns
- `userCoupons` só pode ser criado/atualizado pelo backend (CF)
- `adminLogs` são imutáveis
- Cada documento valida `ownerId` antes de permitir edição

---

## Storage Rules

Ver `storage.rules`. Pontos principais:
- Apenas imagens JPEG/PNG/WEBP e PDFs aceitos
- Tamanho máximo: 5 MB para imagens, 20 MB para PDFs
- Upload restrito ao dono do documento

---

## Sanitização de Input

`SecurityManager.sanitize()` remove:
- Tags HTML e `<script>`
- Padrões SQL injection (`'`, `"`, `--`, `/*`, `xp_`)

Toda entrada do usuário é sanitizada antes de salvar no Firestore.

---

## Próximos Passos de Segurança

- [ ] Implementar HMAC real nas Cloud Functions
- [ ] Habilitar Firebase App Check
- [ ] Configurar Firebase Auth MFA
- [ ] Adicionar monitoramento de atividades suspeitas (Cloud Functions)
- [ ] Certificado SSL pinning (para produção)
- [ ] Ofuscação com ProGuard/R8 no build de release
