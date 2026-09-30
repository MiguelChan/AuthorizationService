# Contributing

Use small, verifiable changes on feature branches. Open pull requests as drafts.
The first PR in a stack targets `mainline`; dependent PRs target the preceding branch.

## Issues

Use the bug report template with these sections:

- **Overview**: the issue, actual and expected behavior, impact, and evidence.
- **Reproduction**: local prerequisites, steps, code samples or unit tests, and results.
  For production bugs, explain how to reproduce locally without real secrets or customer data.
  Be explicit when a finding has not yet been reproduced.
- **Acceptance Criteria**: observable, testable conditions that define completion.

## Pull requests and commits

Use this format for PR titles and commit subjects:

```text
feat|fix|bug|docs|chores(moduleName): <description>
```

Choose exactly one of `feat`, `fix`, `bug`, `docs`, or `chores`. Replace `moduleName`
with the affected module, such as `authorization` or `repo`. Keep the description under
80 characters. Example:

```text
fix(authorization): require authentication for application mutations
```

PR bodies use exactly these sections:

- **What is changing**: both behavior and code changes, with the related issue.
- **Why is it changing**: a concise trace of the observations, investigation, and
  decisions that led to this change.
- **Testing done**: tests actually run and their results, plus relevant gaps.

GitHub loads the PR and issue templates from `.github`. They become the repository
defaults once merged into `mainline`. Templates guide writing; they do not enforce
title or commit validation.

To enable the commit template for a checkout, run:

```sh
git config --local commit.template "$(git rev-parse --show-toplevel)/.gitmessage"
```

This is optional, local configuration. Git does not automatically activate a tracked
commit template. Commands using `git commit -m` must follow the format explicitly.
