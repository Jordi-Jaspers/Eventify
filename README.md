<p align="center">
<h1 align="center">SAT (Spring Application Template)</h1>
<p align="center">A convenient repository to start a new Spring Boot project.</p>

---

## 📝 Introduction

## 🚗 Running the Application

## 📦 Git Flow (Branching / Releasing)

Any changes to the project should be done in the development branch and will be documented in the changelog with their
respective links to
the original Jira ticket. This way all the information is well documented and ready for any body to read and start
developing and no
knowledge is dependent on a single person. If there are any questions they can be asked, but do not start complaining
about the
documentation if you did not read it.

The master branch is only for production-ready code. This is how git-flow works and how we should work with it. If you
have never heard
about it please read the following [link](https://nvie.com/posts/a-successful-git-branching-model/). If you still do not
understand it,
there is this magical engine called `Google` that can help you with that. just like this:

* https://letmegooglethat.com/?q=how+does+git+flow+work

Finally, whenever the code containing the new functionality is merged back to the development branch, a new release
branch should be
created. This branch should be named after the version that is going to be released. This way we can keep track of the
changes and the code
that is going to be released. for example:

```bash
  git checkout -b release/1.0.x
```

Here you'll change the version number in the following files and double check if the ChangeLog is up to date:

* build.gradle -> Backend
* OpenAPIConfig.java -> Backend
* CHANGELOG.md

Once done, you can create a merge request with a tag on the commit with the correct version number. Merge request is
only allowed to be
merged back whenever the jenkins build is successful AND the code is put to production.

```
    git add -A
    git commit -m "Release 1.0.0"
    git tag -a v1.0.0 -m "Release 1.0.0"
    git push --tags
    git push
```

Merging the code back should follow git flow. meaning the release branch is merged to develop and master. following with
a commit on the
develop branch adding `-SNAPSHOT` to the version numbers in the same files. This way we can start a new development
process.

## 🐞 Dang, you found a bug, now what?

No worries, just check out our [**Jira Board
**](https://vodafoneziggonl.atlassian.net/jira/software/c/projects/SMTDEV/boards/4218/backlog).
If you can't find your issue, feel free to create a new one or contact the current Product Owner / Demand Manager for
more information.
We'll try to trample that bug as soon as possible.

## 🛠️ Stack

- [Spring Boot](https://spring.io/projects/spring-boot) - Java framework for building back-end applications.
- [Redis](https://redis.io/) - In-memory data structure store.
- [Docker](https://www.docker.com/) - Containerization platform.
- [Liquibase](https://www.liquibase.org/) - Database migration tool
- [MariaDB](https://mariadb.org/) - Database
- [Git](https://git-scm.com/) - Version Control

## 📜 License

Licensed under [GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.html#license-text).

