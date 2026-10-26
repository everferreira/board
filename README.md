<div align="center">

# 📋 Board — Gerenciador de Tarefas (Kanban)

**Um gerenciador de boards e cards direto no terminal, feito em Java puro!**

`#Java` `#JDBC` `#MySQL` `#Liquibase` `#Lombok` `#Gradle`

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Build](https://img.shields.io/github/actions/workflow/status/everferreira/board/ci.yml?style=for-the-badge&logo=github&label=CI)](https://github.com/everferreira/board/actions)
[![Stars](https://img.shields.io/github/stars/everferreira/board?style=for-the-badge&logo=github)](https://github.com/everferreira/board)
[![Licença](https://img.shields.io/badge/licença-MIT-blue?style=for-the-badge)](LICENSE)

> Projeto desenvolvido como desafio da [DIO — Digital Innovation One](https://www.dio.me/),
> inspirado no repositório original [digitalinnovationone/board](https://github.com/digitalinnovationone/board),
> **replicado e melhorado** com testes, Docker, CI e uma experiência de terminal mais interativa.

---

</div>

## 🎯 O que o projeto faz

Você cria um **board** (quadro Kanban), define suas colunas e gerencia **cards** diretamente pelo terminal:

- ✅ Criar boards com colunas personalizadas
- ✅ Mover cards entre colunas (fluxo: Inicial → Pendente(s) → Final)
- ✅ Bloquear / desbloquear cards com motivo
- ✅ Cancelar cards
- ✅ Ver o board inteiro, colunas e detalhes de cada card

### 🏗️ Fluxo de um card

```text
┌──────────┐    ┌──────────┐    ┌──────────┐
│  INICIAL │──▶│ PENDENTE │──▶│  FINAL   │
│  (card)  │    │ (card)   │    │ (done!)  │
└──────────┘    └──────────┘    └──────────┘
     │                │
     └──▶ CANCELLED ◀──┘
     (motivo registrado)
```

## 🧱 Arquitetura

Camadas bem definidas, do UI até o banco:

```text
┌─────────────────────────────────────────────┐
│                 UI (menu)                    │
│        MainMenu · BoardMenu (Scanner)        │
├─────────────────────────────────────────────┤
│                 SERVICE                      │
│  BoardService · CardService · *QueryService  │
├─────────────────────────────────────────────┤
│                   DAO                        │
│   BoardDAO · BoardColumnDAO · CardDAO        │
├─────────────────────────────────────────────┤
│               PERSISTENCE                    │
│  JDBC + Liquibase (migrações automáticas)    │
├─────────────────────────────────────────────┤
│                 MYSQL (Docker)               │
└─────────────────────────────────────────────┘
```

### 💾 Modelo de dados

```text
BOARDS ──1:N── BOARDS_COLUMNS ──1:N── CARDS ──1:N── BLOCKS
(id, name)      (name, order, kind)  (title, desc)  (blocked_at, reason...)

kind: INITIAL | PENDING | FINAL | CANCEL
```

## 🛠️ Stack técnico

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| Java | 17+ | Linguagem principal |
| Gradle | 8.x | Build tool |
| JDBC | — | Conexão com banco |
| MySQL | 8.0 | Banco de dados |
| Liquibase | 4.29.1 | Migrações automáticas |
| Lombok | 1.18.34 | Redução de boilerplate |
| JUnit 5 | 5.10 | Testes unitários |
| Mockito | 5.12 | Mocks para testes |
| H2 | 2.2.224 | Banco de teste (in-memory) |
| Docker | — | Ambiente MySQL isolado |

## ✨ O que foi melhorado (vs. original)

| Melhoria | Original | Agora |
|----------|----------|-------|
| README | ❌ Sem README | ✅ Documentação completa com diagramas |
| Terminal UI | ⬛ Preto e branco | 🎨 Comandos coloridos com `ColorUtil` |
| Docker | ❌ Não tinha | ✅ `docker-compose.yml` para MySQL |
| Testes | ❌ Zero | ✅ JUnit 5 + Mockito (Enum, DTO, Entities) |
| CI/CD | ❌ Sem CI | ✅ GitHub Actions (build + test) |
| `.gitignore` | ⚠️ Só `.idea/` | ✅ Completo (logs, build, IDE) |

## 📁 Estrutura do repositório

```text
board/
├── .github/workflows/ci.yml    # CI GitHub Actions
├── docker-compose.yml           # MySQL 8.0 em Docker
├── build.gradle.kts             # Gradle com Application plugin
├── settings.gradle.kts          # Config do projeto
├── gradlew                      # Gradle Wrapper
├── src/
│   ├── main/java/br/com/dio/
│   │   ├── Main.java            # Ponto de entrada
│   │   ├── dto/                 # Records (DTOs)
│   │   ├── exception/           # Exceções customizadas
│   │   ├── persistence/
│   │   │   ├── config/          # Conexão JDBC
│   │   │   ├── converter/       # Conversores de data
│   │   │   ├── dao/             # CRUD com JDBC
│   │   │   ├── entity/          # Entidades (Lombok)
│   │   │   └── migration/       # Liquibase runner
│   │   ├── service/             # Regras de negócio
│   │   ├── ui/                  # Menu interativo
│   │   └── util/                # ColorUtil (ANSI colors)
│   └── test/java/br/com/dio/    # Testes JUnit 5
└── src/main/resources/db/       # Migrations Liquibase
```

## 🤝 Contribuindo

1. Fork o projeto
2. Crie sua branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📄 Licença

Distribuído sob a licença MIT. Veja `LICENSE` para mais informações.

---

<div align="center">

Feito com 💜 por [Ever Maria](https://github.com/everferreira) · DIO + Java

</div>