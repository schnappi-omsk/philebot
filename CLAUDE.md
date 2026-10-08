# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

PhilBot is a Telegram group bot for an Xbox gaming chat. It is a Spring Boot 3.5 app on Java 25 (Gradle toolchain), using telegrambots 7.x `AbilityBot` with long polling. It does three things:
- posts players' new Xbox achievements to the group, using XAPI (xapi.us, a paid third-party API)
- answers slash commands
- reacts to chat content: trigger words, stickers, photos via GPT-4o OCR, and social-media links

Bot-facing text is mostly Russian.

## Commands

```bash
./gradlew build                     # compile + test + bootJar
./gradlew test                      # all tests (JUnit 5)
./gradlew test --tests "com.gundomrays.philebot.command.PhilHelpCommandTest"              # one class
./gradlew test --tests "com.gundomrays.philebot.command.PhilHelpCommandTest.testExecute"  # one method
./gradlew bootRun                   # needs env vars below + a reachable PostgreSQL
```

There is no linter or formatter configured.

Required environment variables:
- `API_TOKEN`: XAPI
- `TGAPI_TOKEN`: Telegram
- `OPENAI_KEY`
- `SERVICE_HOST`: public base URL of this app; it is embedded in posted achievement links
- `PG_HOST`, `PG_PORT`, `PG_DB`, `PG_USER`, `PG_PASS`

Deployment is Heroku-style: `system.properties` sets the Java runtime, and `Procifile` runs `java -jar build/libs/philebot.jar`. That filename is a typo for `Procfile`, so Heroku-style hosts will not pick it up.

## Architecture

### Achievement pipeline
Several decoupled `@Scheduled` stages connected by in-memory queues:
1. `xbox/xapi/XboxUserActivityService` (every 5 min) loops over active `Profile`s.
2. It calls `XApiClient` through `RateLimitedExecutor`. That class is a single-thread Guava `RateLimiter` set from `ebot.requestsPerMin`; all XAPI calls must go through it to stay within XAPI limits.
3. It diffs the results against the stored `TitleHistory` and pushes new unlocks to `AchievementQueue`.
4. `worker/PhilAchievementRetriever` (every 1 min) formats each unlock as an HTML link to this app's own `/xbox/{name}/{desc}/{pts}/{rarity}` page and pushes it to `messaging/MessageQueue`.
5. `PhilBot.retrieveAndSendAchievements` (every 30 s) drains `MessageQueue` into the chat.
6. `web/AchievementController` plus the Thymeleaf templates render the card those links open.

The queues are not persisted, so anything queued is lost on restart.

`XApiClient` uses the reactive `WebClient` defined in `xbox/config/PhilConfig`, but calls `.block()` on every request. On a 401 it retries once with the `fresh-login` query parameter. `PhilConfig` also holds `@EnableScheduling`; `@EnableAsync` is not set anywhere.

### Telegram side
- **`PhilBot`** is created by `telegram/config/TelegramConfig` as a `@Bean`, not by component scan, and its collaborators are field-`@Autowired`. `PhilEbotApplication` registers it with `TelegramBotsLongPollingApplication` in a `CommandLineRunner`. `consume(Update)` is the single entry point for all incoming messages.
- **Single-chat binding**: the bot serves one group. The chat id lives in the `settings` table (key `CHAT_ID`). `SettingsService.chatId(...)` stores the first chat id it sees and then marks it `sealed`, so later chats cannot overwrite it. Scheduled jobs post to that stored id.
- **Commands are Spring beans named by their slash command**, e.g. `@Service("/top") class PhilLeaderboardCommand implements PhilCommand`.
  - `PhilCommandService` looks them up with `applicationContext.getBean(name)`.
  - `PhilBot.parseCommand` strips the `@botname` suffix. It also splits a digit suffix into the argument, so `/roll20` means `/roll` with argument `20`.
  - To add a command: create a new `@Service("/name")` `PhilCommand`, and add a row to the `help` table via a new Flyway migration (see `V8__Help_Command.sql`); `/help` reads that table.
  - A command returns `CommandResponse`. If `mediaUrl` is set, the reply is a photo with the message as its caption; otherwise it is an HTML text reply.
- **Reactions**: `ReactionService` holds the trigger-word detection, which catches:
  - Cyrillic/Latin look-alike normalization via `CyrillicLowerCaseTransformer`
  - Levenshtein-based obfuscation
  - words divided by punctuation
  - words split across short tokens

  For photos it uses `ai/ChatGptClient` (Spring AI, OpenAI) to extract text. Trigger lists, sticker/GIF file ids and canned messages all live under `messages.*` in `application.yml`.
- **`SocialMediaLinkService`** rewrites x.com, twitter.com, instagram.com and tiktok.com links to embed-friendly mirror domains.
- **`telegram/bot/clown/ClownService`** caches a remote CSV of "woke" games, refreshed every 10 h, for `/woke`.

### Persistence
- PostgreSQL schema managed by Flyway (`src/main/resources/db/migration`, `V{n}__*.sql`). JPA runs with `ddl-auto: none`, so every schema change needs a new migration.
- Production config uses `baseline-on-migrate` with `baseline-version: 11`.
- Repositories with custom queries use the `*RepositoryExtension` / `*RepositoryExtensionImpl` fragment pattern (in `data/` and `telegram/data/`).

### Tests
- Tests are plain JUnit 5 + Mockito unit tests that construct the class under test by hand. There are no `@SpringBootTest` context tests.
- `src/test/resources/application.yml` points to H2 and dummy tokens.
- The JSON files in `src/test/resources` are XAPI response fixtures.

## Local runtime artifacts

These files are created locally and must not be committed:
- `Phil E-Bot` and `Phil E-Bot.wal.0`: the AbilityBot MapDB store, named after the bot username
- `hs_err_pid*.log`: JVM crash logs
- `src/main/resources/tesseract/`: OCR data that no code references
