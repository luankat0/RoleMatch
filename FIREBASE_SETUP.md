# Configuração Firebase – Rolê Match

## 1. Criar o Projeto

1. Acesse [console.firebase.google.com](https://console.firebase.google.com)
2. Clique em **Adicionar projeto** → Nome: `RoleMatch`
3. Ative o **Google Analytics** (recomendado)

---

## 2. Adicionar App Android

1. No console, clique em **+ Adicionar app** → Android
2. Package name: `com.pentagono.rolematch`
3. Apelido: `Rolê Match Android`
4. Baixe o `google-services.json`
5. Substitua o placeholder em `app/google-services.json`

---

## 3. Serviços a Ativar

### Authentication
```
Firebase Console → Authentication → Sign-in method
→ Ativar: E-mail/senha
→ Ativar: (Opcional) Google Sign-In
```

### Firestore Database
```
Firebase Console → Firestore Database → Criar banco de dados
→ Selecionar: Modo de produção
→ Região: southamerica-east1 (São Paulo)
```

Publicar as regras:
```bash
firebase deploy --only firestore:rules
```

### Storage
```
Firebase Console → Storage → Começar
→ Região: southamerica-east1
```

Publicar as regras:
```bash
firebase deploy --only storage
```

### Cloud Messaging (FCM)
```
Firebase Console → Cloud Messaging → já ativado por padrão
```

---

## 4. Coleções Iniciais no Firestore

Execute este script para criar as coleções iniciais:

```javascript
// No console do Firebase ou via Admin SDK
const admin = require('firebase-admin');

// Criar usuário admin inicial
await admin.firestore().collection('users').doc('admin-uid').set({
  name: 'Administrador',
  email: 'admin@rolematch.com',
  role: 'ADMIN',
  isVerified: true,
  createdAt: admin.firestore.FieldValue.serverTimestamp()
});
```

---

## 5. Índices Necessários

Criar os índices compostos no Firestore:

| Coleção | Campos | Ordem |
|---------|--------|-------|
| `restaurants` | `status` ASC, `rating` DESC | para listagem |
| `restaurants` | `status` ASC, `geoPoint` | para busca por proximidade |
| `coupons` | `restaurantId` ASC, `active` ASC | para busca por restaurante |
| `reservations` | `restaurantId` ASC, `date` ASC | para agenda |
| `userCoupons` | `userId` ASC, `status` ASC | para cupons do usuário |
| `interactions` | `userId` ASC, `createdAt` DESC | para histórico |

---

## 6. Cloud Functions

```bash
cd functions
npm install
firebase deploy --only functions
```

Funções principais implementadas:
- `issueCouponQrCode` – gera e assina token QR
- `validateCouponQrCode` – valida e marca como usado
- `approveRestaurant` – muda status para ACTIVE
- `rejectRestaurant` – muda status para REJECTED
- `sendVerificationCode` – envia código por e-mail

---

## 7. Variáveis de Ambiente (Cloud Functions)

```bash
firebase functions:config:set \
  app.qr_secret="SUA_CHAVE_HMAC_256_BITS" \
  app.sendgrid_key="SUA_CHAVE_SENDGRID" \
  app.twilio_sid="SUA_TWILIO_SID" \
  app.twilio_token="SEU_TWILIO_TOKEN"
```

---

## 8. Verificar Instalação

```bash
# Testar conexão
firebase firestore:indexes

# Emular localmente
firebase emulators:start
```

No `local.properties` do Android, adicione:
```
firebase.emulator.host=10.0.2.2
```

E no `MainActivity.kt`, para usar o emulador em debug:
```kotlin
if (BuildConfig.DEBUG) {
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.auth.useEmulator("10.0.2.2", 9099)
}
```
