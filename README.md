# VeloraCore (Velora SMP)

A Paper 1.21.x economy-survival plugin scaffold with SQLite-backed economy, universal material pricing, selling commands, admin economy commands, backup/reload admin commands, and optional PlaceholderAPI integration.

## Java / Build
- JDK: 21
- Build from project root:

```bash
mvn clean test package
```

Output jar:
- `target/VeloraCore-1.0.0.jar`

## Install
1. Build jar with Maven.
2. Copy jar into `plugins/`.
3. Start Paper server (1.21.11-compatible API target: Paper 1.21.1 API).
4. Configure files under `plugins/VeloraCore/`.

## Commands
- Economy: `/bal`, `/balance`, `/pay`, `/baltop`, `/eco`
- Selling: `/sell`, `/worth`, `/prices`, `/price`
- Admin: `/velora reload|backup|info`
- Additional module command stubs are registered for: `/ah`, `/orders`, `/crates`, `/rank`, `/playtime`, `/quests`, `/daily`, `/bounty`, `/bounties`, `/stats`

## Notes
- Monetary values use `BigDecimal` server-side.
- Balances are persisted by UUID in SQLite.
- Material pricing uses deterministic fallback for every valid sellable `Material`.
- PlaceholderAPI is optional (`softdepend`) and safe to run without it.
