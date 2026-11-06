# Stay Local — Java shelter app (final source, November 5, 2026)

A local-only Java Swing app (NetBeans project `stay-local-java`, package
`staylocal`, 46 source files) that turns the BC 211 Shelter List into a
working bed-reservation system with three dashboards:

- **Health care worker** — search 107 real BC 211 shelters with every
  listing detail as a filter (including intake windows, sobriety stance,
  party type, area distance), place all-or-nothing holds for 1–10 beds
  with arrival needs, language and safety flags, get availability alerts,
  print handouts.
- **Shelter console** — live bed counts, hold confirm/reject, check-in /
  no-show, walk-in reserve, turn-away logging, mark-verified, day summary.
- **Admin** — MFA-gated daily report (supply + demand + turn-aways),
  account registry with add/revoke/reactivate/MFA-reset, shelter
  add/remove/hold, public notices, full audit log.

Data honesty: the directory is a snapshot of the BC 211 Shelter List
retrieved November 5, 2026 (shown in the app, with its age). Confidential
shelters are never mapped or printed with an address. Holds, accounts and
MFA are local demos — nothing is sent to any shelter or to BC 211.

Verified: `AllChecks` runs all 22 test suites (directory, search, holds,
filters, intake, needs, turn-aways, demand, sobriety, handouts, console,
admin, directory age, distance, language) — all passing, November 5, 2026.

Built phase by phase (Phases 0–26) from the Stay Local web starter in the
`stay-local` repo. Not the Tucked In course project — that is separate and
uses fictional sample data.
