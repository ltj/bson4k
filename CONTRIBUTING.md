# Contributing Guidelines

Thank you for your interest in contributing to this project! :tada:

You can contribute in different ways:
- Submitting issues (bugs and feature requests)
- Creating pull requests

## Submitting issues

Bug reports, feature requests and general suggestions are welcome.
Submit your issue [here](https://github.com/imotions/bson4k/issues).

* Search for existing issues to avoid reporting duplicates.
* When submitting a bug report:
    * Use a 'bug report' template when creating a new issue.
    * Test it against the most recently released version. It might have been already fixed.
    * Include the code and/or steps to reproduce the issue. 
    * Please explain the expected behavior, and the experienced behavior.
* When submitting a feature request:
    * Use a 'feature request' template when creating a new issue.
    * Explain why you need the feature &mdash; what's your use-case, what's your domain.
    * Describe alternative solutions if any.

## Creating pull requests

We love PRs. Submit PRs [here](https://github.com/imotions/bson4k/pulls).
Please keep in mind that maintainers will have to support the codebase going forward,
so familiarize yourself with the following guidelines.

* All development (both new features and bug fixes) is performed in the `dev` branch.
    * The `main` branch always contains sources of the most recently released version.
    * Base PRs against the `dev` branch.
    * The `dev` branch is pushed to the `main` branch during release.
    * Documentation in markdown files can be updated directly in the `main` branch,
      unless the documentation is in the source code, and the patch changes line numbers.
* If you make any code changes:
    * Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html).
        * Use 4 spaces for indentation.
        * Use wildcard imports.
* If you fix a bug:
    * Write a test that reproduces the bug.
    * Fixes without tests are accepted only in exceptional circumstances if it can be shown that writing the
      corresponding test is too hard or otherwise impractical.
    * Follow the style of existing tests in the project.
* Comment on the existing issue if you want to work on it. Ensure that the issue not only describes a problem,
  but also describes a solution that had received a positive feedback. Propose a solution if there isn't any.

## Contacting maintainers

Currently, the only way to contact maintainers is through GitHub issues.

