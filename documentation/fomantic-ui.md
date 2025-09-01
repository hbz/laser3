## Read this before you deal with Fomantic UI Build

Change one file back after installing Fomantic UI: in files/frontend/semantic/tasks/build.js we have some core changes because 2 themes

---

## Install all the dependencies on your pc

### Install nodejs on Linux

    sudo apt-get install --yes nodejs

### Install Node.js on Windows with NVM (Node version manager for Windows) to manage Node.js versions

1. go to https://github.com/coreybutler/nvm-windows/releases and download nvm-setup.exe

2. install NVM (Node version manager for Windows) on Windows via installation file

3. restart windows before next step

4. install the latest Node.js via NVM version


    nvm install lts

5. list all the installed node versions with<<<<<


    nvm list

6. specify the node version you would like to use e.g. 18.16.0


    nvm use 18.16.0


### Update or Install Fomantic UI for the first time

- change the package.json to the new fomantic ui version
- change the semantic.json to the new fomantic ui version
- check if the needed node version is installed on your pc with 'nvm current'
- go to the folder 'files\frontend'


    npm install --ignore-scripts fomantic-ui
    npm update
    cd node_modules/fomantic-ui
    npx gulp install


### Install gulp globaly

    npm install -g gulp


### Change one file back to repository version
After installing Fomantic UI: in files/frontend/semantic/tasks/build.js we have some core changes because 2 themes.
So do go to folder 'frontend\semantic\tasks'

    git checkout build.js



### Build the semantic.min.css and semantic.min.js

- go to folder 'frontend\semantic'


     gulp build

---

## How to customize CSS

Our custom themes and the default theme override the *.less in 'src/definitions'. The folder 'src/side' is not used yet.


### Theming

- we got the themes 'laser' and 'accessibility'
- all changes in css we make by changing inside the theme!
- the original semantic ui file for gulp bilding 'app/semantic/tasks/build.js' is changed in order to build two themes at the same time (laser & accessibility)
- meanwile the gulp build process temp files are builded and moved around

### Example

I would like to change the padding between an icon and content in a list

1. find the variable in default theme. In this case there


    src/themes/default/elements/list.variables

2. create the list.variables in the laser theme folder


    src/themes/laser/elements/list.variables

3. change the specifig variable only there

4. Change the theme.config


    /files/frontend/semantic/src/definitions/themes/laser/theme.config

    /files/frontend/semantic/src/definitions/themes/accessibility/theme.config

old:

    @list       : 'default';

new:

    @list       : 'laser';

5. Build css


    cd semantic
    gulp build

