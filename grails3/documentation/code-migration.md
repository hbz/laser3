
## Code Migration

Migrating code from Grails 2 to Grails 3 and keeping the history.

#### Local configuration

    // connect to laser (grails2)
    
    git remote add grails2 https://github.com/hbz/laser
    git remote set-url --push grails2 DISABLED

#### Migration of feature branch (grails2 &rArr; grails3)

    // increase limit
    git config merge.renameLimit 10000
    
    // fetch my branch (grails2)
    git fetch grails2 my-feature-branch:grails2-my-feature-branch

    // merge into local branch (grails3)
    git checkout -b grails3-my-feature-branch
    git merge grails2-my-feature-branch

    // TODO: resolve conflicts

    git commit -m 'migration of my-feature-branch'
    
    // push to grails3 repo
    git push origin grails3-my-feature-branch
    
    // TODO: open pull request
    