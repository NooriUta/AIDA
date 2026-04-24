## [v1.3.6] — 2026-04-24

### Bug Fixes

- **cd**: do not send credentials to ArcadeDB /api/v1/ready (no-auth endpoint) ([`aaad131`](https://github.com/NooriUta/AIDA/commit/aaad13115fc016fe09a5215191ec6741e7dd9697))


### Miscellaneous

- **release**: update CHANGELOG for v1.3.3 [skip ci] ([`a7fbf3f`](https://github.com/NooriUta/AIDA/commit/a7fbf3fd8800fc23173a9399185cab9c3e6b14a5))

- **release**: sync master → release (v1.3.4, v1.3.5) ([`9d7321b`](https://github.com/NooriUta/AIDA/commit/9d7321b36099119d89acc6f124b6e0f50f7b40b6))


### Miscellaneous

- **release**: update CHANGELOG for v1.3.2 [skip ci] ([`4489c26`](https://github.com/NooriUta/AIDA/commit/4489c26d1fa91bd59ec88f789bc6b96b26dd1ef9))


### Miscellaneous

- **release**: update CHANGELOG for v1.3.1 [skip ci] ([`74b74e0`](https://github.com/NooriUta/AIDA/commit/74b74e0ec8bd0ef9db82790fb770ca53834652c7))


### Miscellaneous

- **release**: update CHANGELOG for v1.3.1 [skip ci] ([`bdab4ca`](https://github.com/NooriUta/AIDA/commit/bdab4ca1173e241f0511683ee5a04c46cd1c73eb))

## [v1.3.3] — 2026-04-24

### Bug Fixes

- **cd**: fix deploy order — wait infra only, then init, then app health ([`6828568`](https://github.com/NooriUta/AIDA/commit/682856821ad3ce5d8b56b49f9fe0829229465255))


### Miscellaneous

- **release**: update CHANGELOG for v1.3.2 [skip ci] ([`4489c26`](https://github.com/NooriUta/AIDA/commit/4489c26d1fa91bd59ec88f789bc6b96b26dd1ef9))

## [v1.3.2] — 2026-04-24

### Bug Fixes

- **cd**: use docker compose up --wait before init-arcadedb.sh ([`3aa11ca`](https://github.com/NooriUta/AIDA/commit/3aa11caa575c578860042ea4e1989d032bdae7b4))

- **cd**: increase init-arcadedb timeout 120→300s, add explicit ArcadeDB readiness wait ([`529bb8e`](https://github.com/NooriUta/AIDA/commit/529bb8e35e91cd728640d54e7b61ce9b4e35b3aa))


### Miscellaneous

- **release**: update CHANGELOG for v1.3.1 [skip ci] ([`74b74e0`](https://github.com/NooriUta/AIDA/commit/74b74e0ec8bd0ef9db82790fb770ca53834652c7))

## [v1.3.1] — 2026-04-24

### Bug Fixes

- **kc**: fix KC_HOSTNAME to full URL, drop hostname-backchannel-dynamic ([`b3ea775`](https://github.com/NooriUta/AIDA/commit/b3ea775984dd385d0010f6f37a2cba421c52f5cb))

- **release**: push CHANGELOG to release branch, not master ([`714aec5`](https://github.com/NooriUta/AIDA/commit/714aec5a95c2d96295726866d8ad1126e3c37060))

- **kc**: add KC_HOSTNAME_BACKCHANNEL_DYNAMIC=true for internal service calls ([`1760191`](https://github.com/NooriUta/AIDA/commit/1760191e5c76882aa7d411f3481183458d6c1ca0))


### Miscellaneous

- **release**: update CHANGELOG for v1.3.1 [skip ci] ([`bdab4ca`](https://github.com/NooriUta/AIDA/commit/bdab4ca1173e241f0511683ee5a04c46cd1c73eb))

## [v1.3.1] — 2026-04-24

### Bug Fixes

- **release**: push CHANGELOG to release branch, not master ([`714aec5`](https://github.com/NooriUta/AIDA/commit/714aec5a95c2d96295726866d8ad1126e3c37060))

- **kc**: add KC_HOSTNAME_BACKCHANNEL_DYNAMIC=true for internal service calls ([`1760191`](https://github.com/NooriUta/AIDA/commit/1760191e5c76882aa7d411f3481183458d6c1ca0))

# Changelog

All notable changes to AIDA are documented here.
Entries are generated automatically from [Conventional Commits](https://www.conventionalcommits.org/) on each release.

<!-- Releases are prepended above this line automatically -->
