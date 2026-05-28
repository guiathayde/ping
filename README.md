# Ping

Aplicativo de mensagens simples (estilo WhatsApp) desenvolvido com **Jetpack Compose + Kotlin** para a disciplina de Desenvolvimento Móvel da UFSCar.

## Integrantes

Guilherme César Athayde

## Requisitos Atendidos

| Requisito | Descricao | Implementacao |
|-----------|-----------|---------------|
| **R1** | Identidade visual e layout bem definidos | Paleta verde-teal Material 3, dark mode, avatares com iniciais coloridas |
| **R2** | Minimo de 3 telas | 4 telas: Login, Conversas, Buscar Usuarios, Chat |
| **R3** | Acesso a rede | REST API via Retrofit + WebSocket via OkHttp para mensagens em tempo real |
| **R4** | Armazenamento local via Room | 2 entidades (conversas e mensagens) com DAOs e Flow |
| **R5** | Internacionalizacao (2 idiomas) | Portugues (padrao) e Ingles — zero strings hardcoded |
| **R6** | Boas praticas | MVVM, Repository pattern, DI manual, testes unitarios e instrumentados |

## Arquitetura

```
com.guiathayde.ping/
├── MainActivity.kt                  # Entry point
├── PingApplication.kt               # Application + inicializacao do DI
├── di/
│   └── AppContainer.kt             # Injecao de dependencia manual
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room Database
│   │   ├── dao/
│   │   │   ├── ConversationDao.kt  # DAO conversas (Flow)
│   │   │   └── MessageDao.kt       # DAO mensagens (Flow)
│   │   └── entity/
│   │       ├── ConversationEntity.kt
│   │       └── MessageEntity.kt
│   ├── remote/
│   │   ├── ApiService.kt           # Retrofit interface
│   │   ├── TokenManager.kt         # SharedPreferences para JWT
│   │   ├── WebSocketManager.kt     # Mensagens em tempo real
│   │   └── dto/Dtos.kt             # Data Transfer Objects
│   └── repository/
│       ├── AuthRepository.kt
│       ├── ConversationRepository.kt
│       ├── MessageRepository.kt
│       └── UserRepository.kt
└── ui/
    ├── theme/
    │   ├── Color.kt                # Paleta verde-teal
    │   ├── Theme.kt                # Material 3 (light + dark)
    │   └── Type.kt                 # Tipografia
    ├── navigation/
    │   ├── Screen.kt               # Rotas de navegacao
    │   └── NavGraph.kt             # Grafo de navegacao
    ├── components/
    │   └── AvatarCircle.kt         # Avatar com iniciais
    ├── auth/
    │   ├── AuthScreen.kt
    │   └── AuthViewModel.kt
    ├── conversations/
    │   ├── ConversationsScreen.kt
    │   └── ConversationsViewModel.kt
    ├── search/
    │   ├── SearchScreen.kt
    │   └── SearchViewModel.kt
    └── chat/
        ├── ChatScreen.kt
        └── ChatViewModel.kt
```

### Padrao MVVM + Repository

```
[Screen] → [ViewModel] → [Repository] → [Room DAO / Retrofit API / WebSocket]
```

- **Screen (Composable)**: Renderiza a UI e observa o estado via `StateFlow`
- **ViewModel**: Gerencia o estado da tela, chama os repositories
- **Repository**: Abstrai o acesso a dados (local + remoto)
- **Room**: Cache local offline-first
- **Retrofit**: Comunicacao REST com o backend
- **WebSocket**: Mensagens em tempo real

## Dependencias Principais

| Biblioteca | Uso |
|------------|-----|
| Jetpack Compose + Material 3 | UI declarativa |
| Navigation Compose | Navegacao entre telas |
| Room | Banco de dados local (SQLite) |
| Retrofit + Gson | Cliente HTTP + serializacao JSON |
| OkHttp | HTTP client + WebSocket |
| Coroutines + Flow | Programacao assincrona |

## Como Rodar

### Pre-requisitos

- Android Studio (com SDK 36)
- Node.js (v18+)
- npm

### 1. Backend

```bash
git clone git@github.com:guiathayde/ping-server.git
cd ping-server
pnpm install
pnpm dev
```

O servidor inicia na porta **3000**.

### 2. App Android

1. Abra a pasta `Ping/` no Android Studio
2. Sincronize o Gradle
3. Configure o IP do backend:
   - **Emulador**: use `10.0.2.2` (mapeia para localhost do host)
   - **Celular fisico**: use o IP local do computador (ex: `192.168.0.16`)
   - Altere em `AppContainer.kt` (Retrofit) e `WebSocketManager.kt` (WebSocket)
4. Rode no emulador ou dispositivo

### 3. Testando

Abra o app em **dois dispositivos/emuladores** diferentes, faca login com usernames distintos, busque o outro usuario e comece a conversar.

## Testes Automatizados

### Testes Unitarios (JVM)

```bash
./gradlew test
```

- `ApiServiceTest` — Testa endpoints da API com MockWebServer

### Testes Instrumentados (Android)

```bash
./gradlew connectedAndroidTest
```

- `ConversationDaoTest` — Testa CRUD de conversas no Room
- `MessageDaoTest` — Testa CRUD de mensagens no Room

## Internacionalizacao (i18n)

- `res/values/strings.xml` — Portugues (padrao)
- `res/values-en/strings.xml` — Ingles

Todas as strings da UI usam `stringResource(R.string.xxx)`. Para testar o ingles, mude o idioma do dispositivo para English.

## Fluxo do Usuario

```
Abre o app
    │
    ▼
Tela de Login ──(nome + username)──▶ Backend cria/autentica usuario
    │
    ▼
Lista de Conversas ◄──── WebSocket conectado (mensagens em tempo real)
    │
    ├── Tap em conversa ──▶ Tela de Chat
    │
    ├── FAB (buscar) ──▶ Tela de Busca
    │                        │
    │                        └── Tap em usuario ──▶ Cria conversa ──▶ Tela de Chat
    │
    └── Botao Sair ──▶ Logout ──▶ Tela de Login
```

## Identidade Visual

- **Primary**: `#0F7B6C` (verde-teal)
- **Primary Container**: `#A7F5E0`
- **Secondary**: `#4B635C`
- **Background**: `#FAFDFB`
- Suporte a **Dark Mode** com cores equivalentes
- Avatares com **iniciais coloridas** (8 cores distintas)
- Baloes de mensagem com **cantos arredondados assimetricos**
