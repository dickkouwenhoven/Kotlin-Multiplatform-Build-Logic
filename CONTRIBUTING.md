# Contributing to the KMP Build Logic

Thank you for your interest in contributing!
This guide explains how to contribute.

This guide will help you to...

* maximize the chance of your changes being accepted
* work on the code
* get help if you encounter trouble

## Before you start

Before starting to work on a feature or a bug fix, please open an issue to discuss the use case or bug with me, or post a comment.
This can save everyone time and frustration.

For any non-trivial change, I need to be able to answer these questions:

* Why is this change done? What's the use case?
* For user-facing features, what will the API look like?
* What test cases should it have? What could go wrong?
* How will it roughly be implemented?

I may ask you to answer these questions directly in the GitHub issue.

### Security vulnerabilities

Do not report security vulnerabilities to the public issue tracker. Follow [Security Vulnerability Disclosure Policy](https://github.com/dickkouwenhoven/Kotlin-Multiplatform-Build-Logic/security/policy).

### Follow the Code of Conduct

Contributors must follow the Code of Conduct outlined at [Code of Conduct](CODE_OF_CONDUCT.md).

### Additional help

If you run into any trouble, please reach out to me on the issue you are working on.

## Finding issues to work on

If you are looking for good first issues, take a look at the list of [good first issues](https://github.com/dickkouwenhoven/Kotlin-Multiplatform-Build-Logic/labels/good%20first%20issue) that should be actionable and ready for a contribution.

If you are looking for a contribution that is more substantial, you can look at the [help wanted issues](https://github.com/dickkouwenhoven/Kotlin-Multiplatform-Build-Logic/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22%F0%9F%8C%B3%20help%20wanted%22).

You can share your interest in fixing the issue by commenting on it.
If somebody shared their interest in the issue, please consider letting them work on it.
However, if there are no changes for more than a week, it's safe to assume that the issue is up for grabs.
There is no need to ask for an assignment or for permission to work on those issues, just comment and start working on it.

## Setting up your development environment

In order to make changes to KMP Build Logic, you'll need:

* [Jetbrains JBR](https://github.com/JetBrains/JetBrainsRuntime) (Java Development Kit) **version 21**. Fixed version is required to use [remote cache](#remote-build-cache).
* A text editor or IDE. I use and recommend [Fleet](https://www.jetbrains.com/fleet/).
* [git](https://git-scm.com/) and a [GitHub account](https://github.com/join).

KMP Build Logic uses pull requests for contributions. Fork [Kotlin-Multiplatform-Build-Logic](https://github.com/dickkouwenhoven/Kotlin-Multiplatform-Build-Logic) and clone your fork. Configure your Git username and email with:

    git config user.name 'First Last'
    git config user.email user@example.com

#### Import KMP Build Logic into Fleet

To import KMP Build Logic into Fleet:
- Open the `build.gradle.kts` file in root of the project with Fleet and choose "Open as Project"
- Select Jetbrains JBR 21 VM as "KMP Build Logic JVM"
- Revert the Git changes to files in the `.fleet` folder

## Making your change

### Code change guidelines

All code contributions should contain the following:

* Create integration tests that exercise a KMP Build Logic build for the bug/feature.
* Annotate tests that correspond to a bug on GitHub (`@Issue("https://github.com/dickkouwenhoven/Kotlin-Multiplatform-Build-Logic/issues/123")`).
* Add documentation to the User Manual and DSL Reference (under [documentation](documentation/UserManual.md)).
* For error messages related changes, follow the [ErrorMessages Guide](documentation/contributing/ErrorMessages.md).
* For Javadocs, follow the [Javadoc Style Guide](documentation/contributing/JavadocStyleGuide.md).
* For new features, the feature should be mentioned in the [Release Notes](documentation/release/notes.md).

Your code needs to run on [all versions of Java that KMP Build Logic supports](documentation/release/compatibility.adoc) and across all supported operating systems (macOS, Windows, Linux).

You can consult the [Architecture documentation](documentation/architecture.md) to learn about some of the architecture of KMP Build Logic.

### Contributing to documentation

You can generate docs by running `./gradlew :documentation`.
This will build the whole documentation locally in [documentation](./documentation).
For more commands and examples, including local development,
see [this guide](./documentation/README.md).

### Creating commits and writing commit messages

The commit messages that accompany your code changes are an important piece of documentation. Please follow these guidelines when creating commits:

* [Write good commit messages.](https://cbea.ms/git-commit/#seven-rules)
* [Sign off your commits](https://git-scm.com/docs/git-commit#Documentation/git-commit.txt---signoff) to indicate that you agree to the terms of [Developer Certificate of Origin](https://developercertificate.org/). I can only accept PRs that have all commits signed off.
* Keep commits discrete. Avoid including multiple unrelated changes in a single commit.
* Keep commits self-contained. Avoid spreading a single change across multiple commits. A single commit should make sense in isolation.

### Testing changes

After making changes, you can test your code in 2 ways:

#### Run tests

- Run `./gradlew :<subproject>:quickTest` where `<subproject>` is the name of the subproject you've changed.
- For example: `./gradlew :launcher:quickTest`.

It's also a good idea to run `./gradlew sanityCheck` before submitting your change because this will help catch code style issues.

### Copyright and License

When updating/modifying a file, please do not make changes to the copyright header.

When creating a new file, please make sure to add a header as defined below.

#### Required Files for Copyright Headers:

- Source code files (e.g., `.java`, `.kt`, `.groovy`).
- Documentation files, where applicable (e.g., `.adoc`, `.md`).

#### Copyright Header for Source Files:

```
/*
 * Copyright [YEAR OF FILE CREATION] KMP Build Logic and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

#### Copyright Header for Documentation Files:

```
/*
 * Copyright [YEAR OF FILE CREATION] KMP Build Logic and contributors.
 *
 * Licensed under the Creative Commons Attribution-Noncommercial-ShareAlike 4.0 International License.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

### Submitting Your Change

After you submit your pull request, I will review it.

### Debugging KMP Build Logic

See the [Debugging KMP Build Logic](documentation/contributing/Debugging.md) guide for tips on debugging KMP Build Logic.

### Fixing DCO failures/Signing Off Commits After Submitting a Pull Request

You must agree to the terms of [Developer Certificate of Origin](https://developercertificate.org/) by signing off your commits. I will verify that all commit messages contain a `Signed-off-by:` line with your email address. I can only accept PRs that have all commits signed off.

If you didn't sign off your commits before creating the pull request, you can fix that after the fact.

To sign off a single commit:

`git commit --amend --signoff`

To sign off one or multiple commits:

`git rebase --signoff origin/master`

Then force push your branch:

`git push --force origin test-branch`

#### Filtering changes by severity

**************************************************************
There is a somewhat non-obvious filter present on the page that allows you to control which type of messages are displayed.
The filter is a dropdown box that appears when you click the `Severity ⬇️ ` label in the black header bar to the immediate right of the KMP Build Logic version.

If you have a large number of messages of different types, filtering by severity to see only `Error`s can be helpful when processing the report.
Errors are the only type of issues that must be resolved for the `checkBinaryCompatibility` task to succeed.

#### Accepting multiple changes

If you have multiple changes to accept (and you're sure they ought to be accepted instead of corrected), you can use the `Accept Changes for all Errors` button to speed the process.
This button will cause a Javascript alert dialog to appear asking you to type a reason for accepting the changes, e.g. "Added new API for KMP Build Logic 1.0.0".

Clicking okay on the dialog will cause a copy of the `accepted-public-api-changes.json` containing your (properly sorted) addition to be downloaded.
You can then replace the existing file with this new downloaded version.

### Java Toolchain

The KMP Build Logic build uses [Java Toolchain](https://docs.gradle.org/current/userguide/toolchains.html) support to compile and execute tests across multiple versions of Java.

Available JDKs on your machine are automatically detected and wired for the various compile and test tasks.
Some tests require multiple JDKs to be installed on your computer, be aware of this if you make changes related to anything toolchains related.

If you want to explicitly run tests with a different Java version, you need to specify `-PtestJavaVersion=#` with the major version of the JDK you want the tests to run with (e.g. `-PtestJavaVersion=21`).

### Configuration cache enabled by default

The build of KMP Build Logic enables the configuration cache by default.

Most tasks that are used to build KMP Build Logic support the configuration cache.

To disable the configuration cache, run the build with `--no-configuration-cache`.

For more information on the configuration cache, see the [gradle user manual](https://docs.gradle.org/current/userguide/configuration_cache.html).

## Our thanks

I deeply appreciate your effort toward improving KMP Build Logic. For any contribution, large or small, you will be immortalized in the release notes for the version you've contributed to.
