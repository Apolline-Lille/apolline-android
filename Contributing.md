

# Contributing to the project

As a contributor, here are the guidelines we would like you to follow:
 
 - [Commit Message Guidelines](#commit)
	 - [Commit Message Format](#format)
	 - [Revert a commit](#revert)
	 - [Commit type](#type)
	 - [Commit subject](#subject)
	 - [Commit body](#body)
	 - [Commit footer](#footer)
 - [Branch](#branch)
	 - [master branch](#master)
	 - [feature branch](#feature)


## <a name="commit"></a> Commit Message Guidelines

We have very precise rules over how our git commit messages can be formatted.  This leads to **more
readable messages** that are easy to follow when looking through the **project history**.

### <a name="format"></a> Commit Message Format
Each commit message consists of a **header**, a **body** and a **footer**.  The header has a special
format that includes a **type** and a **subject**:

```
<type>: <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

The **header** is mandatory .

Any line of the commit message cannot be longer 100 characters! This allows the message to be easier
to read on GitHub as well as in various git tools.

Footer should contain a [closing reference to an issue](https://help.github.com/articles/closing-issues-via-commit-messages/) if any.

### <a name="revert"></a>Revert
If the commit reverts a previous commit, it should begin with `revert: `, followed by the header of the reverted commit. In the body it should say: `This reverts commit <hash>.`, where the hash is the SHA of the commit being reverted.

### <a name="type"></a>Type
Must be one of the following:

* **build**: Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)
* **ci**: Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)
* **doc**: Documentation only changes
* **feat**: A new feature
* **fix**: A bug fix
* **perf**: A code change that improves performance
* **refactor**: A code change that neither fixes a bug nor adds a feature
* **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
* **test**: Adding missing tests or correcting existing tests
* **version**: Update dependencies versions

### <a name="subject"></a>Subject
The subject contains succinct description of the change

### <a name="body"></a>Body
The body should include the motivation for the change and contrast this with previous behavior.

### <a name="footer"></a>Footer
The footer should contain any information about **Breaking Changes** and is also the place to
reference GitHub issues that this commit **Closes**.

**Breaking Changes** should start with the word `BREAKING CHANGE:` with a space or two newlines. The rest of the commit message is then used for this.

## <a name="branch"></a>Branch

### <a name="master"></a>master branch
master is the production branch, you shouldn't push your change directly on master except for critical hot-fix

### <a name="feature"></a>feature branch
feature branche are created when working on a specific feature.
when the development of the feature is finished please do as following:

commit your change (**don't push them**) then :

```
git checkout featureBranch
git rebase targetBranch
```

resolve your pentential conflict, 
push your commit,
create a new pull request on github (only if github don't find any conflict)
