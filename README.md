## Merge Request Integration

Merge Request Integration is a plugin which helps you to do Code Review right in your IDE.

<img src="https://raw.githubusercontent.com/nhat-phan/merge-request-integration/master/images/v2019.3.2.gif" alt="Merge Request Integration CE" style="width: 100%" />


What you can do:

- Filter Merge Requests which are assigned to you, waiting for your approval, etc
- Check pipeline status and approval status.
- Select and review 1 or all commits
- Do code review, navigate code with Diff View right in your IDE.
- Add, reply, resolve or delete comments
- Approve/revoke your approval
- More and more features will be coming soon :)

Currently the plugin supports GitLab only (gitlab cloud and self-hosted).

You can download the plugin on intellij plugins repository: 
[Community Edition](https://plugins.jetbrains.com/plugin/13607-merge-request-integration-ce--code-review-for-gitlab/),
[Enterprise Edition](https://plugins.jetbrains.com/plugin/13615-merge-request-integration-ee--code-review-for-gitlab/)

### How to setup Gitlab connection

#### Create Gitlab Personal Access Tokens

To get the your Personal Access Token please follow these steps:

<img src="https://raw.githubusercontent.com/nhat-phan/merge-request-integration/master/images/gitlab-1.png" />

Step 1: On top right corner > Settings

<img src="https://raw.githubusercontent.com/nhat-phan/merge-request-integration/master/images/gitlab-2.png" />

Step 2: Click to Access Tokens menu

<img src="https://raw.githubusercontent.com/nhat-phan/merge-request-integration/master/images/gitlab-3.png" />

Step 3: Create a Personal Access Tokens with api scope

#### Config connection in your IDE Settings

After creating the Personal Access Tokens:

- Go to your IDE preferences (macOS: `⌘,` Windows: `Ctrl+Alt+S`)
- Merge Request Integration > Gitlab
- Fill data, then save. *Tip: you can use Starred/Membership/Own option to search your project quicker.* 
- Click refresh button of Merge Request Integration CE window if you don't see the connection.

If your project has more than 1 repository, just setup multiple connections.

### License

The plugin is an open source released under Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International
license.

It's totally free if you are using it for public repositories.

For private repositories, this plugin is a trial. How long is the trial period? Equal to WINRAR's trial period 🙈

[Community Edition (CE)](https://plugins.jetbrains.com/plugin/13607-merge-request-integration-ce--code-review-for-gitlab/) 
is exactly the same as 
[Enterprise Edition (EE)](https://plugins.jetbrains.com/plugin/13615-merge-request-integration-ee--code-review-for-gitlab/). 
You don't need to hack or find a cracked version.
Cracking software invites virus to your computer.

### About me

My name is Nhat, I'm a software developer at [Personio](https://personio.com) 
(yes, 
[we are hiring](https://www.personio.com/about-personio/jobs/) 
all around the world, relocation to Munich is of course possible).

### Sponsor

If you love this plugin, please support me by:

- Buy an [Enterprise Edition](https://plugins.jetbrains.com/plugin/13615-merge-request-integration-ee--code-review-for-gitlab/), only 1$/month
- Buy me a beer via [Paypal](https://paypal.me/phanhoangnhat) or [Patreon](https://www.patreon.com/nhat/creators).

Thanks in advance!

### Attribution

- Icons by [Font Awesome](https://fontawesome.com/) are licensed under 
[CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)
