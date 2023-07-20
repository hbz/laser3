# Building CSS with Gulp

## Install Node.js on Linux

    sudo apt-get install --yes nodejs

## Install Node.js on Windows with NVM (Node version manager for Windows) to manage Node.js versions
1.) go to https://github.com/coreybutler/nvm-windows/releases and download nvm-setup.exe

2.) install NVM (Node version manager for Windows) on Windows via installation file

3.) install the latest Node.js version

    nvm install lts

4.) specify the node version you use

    nvm use 18.16.0

## NPM (Nodes Package Manager)

1.) change directory to files/frontend/semantic

    cd files/frontend/semantic

2.) install GULP

    npm install gulp

3.) build the CSS

    gulp build


# Update fomantic-ui

- !!!!!!!!!!! Do not install Fomantic UI from the scratch because we have some core changes because WCAG stuff


1.) change directory to files/frontend/semantic

    cd files/frontend/semantic

2.) install fomantic-ui (in directory node_modules)

    npm install fomantic-ui

3.) now executing carefully overwriting the directory files/frontend/semantic/src with files from files/frontend/node_modules/fomantic-ui/src
- in files/frontend/semantic/src/definitions/modules/dropdown.js
- in files/frontend/semantic/tasks/build.js because 2 themes

# How to customize CSS

Our custom themes and the default theme overwrite the *.less in 'src/definitions'. The folder 'src/side' is not used yet.


## Theming

- we got the themes 'laser' and 'accessibility'
- all changes in css we make by changing inside the theme!
- the original semantic ui file for gulp building 'app/semantic/tasks/build.js' is changed in order to build two themes at the same time (laser & accessibility)
- meanwhile the gulp build process temp files are built and moved around

## Example

I would like to change the padding between an icon and content in a list

1.) find the variable in default theme. In this case, there

    src/themes/default/elements/list.variables

2.) create the list.variables in the laser theme folder

    src/themes/laser/elements/list.variables

3.) change the specific variable only there

4.) Change the theme.config

    /files/frontend/semantic/src/definitions/themes/laser/theme.config

    /files/frontend/semantic/src/definitions/themes/accessibility/theme.config

old:

    @list       : 'default';

new:

    @list       : 'laser';

5.) Build CSS

    cd semantic
    gulp build
