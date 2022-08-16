# Read this before you deal with Fomantic UI Build

- do not install Fomantic UI from the scratch because we have some core changes
    - in files/frontend/semantic/src/definitions/modules/dropdown.js because WCAG stuff
    - in files/frontend/semantic/tasks/build.js because 2 themes

---
# Use the frontend build process but leave the Fomantic UI installation out of the picture

## install nodejs on Linux

    sudo apt-get install --yes nodejs

## install nodejs on Windows with NVM

-  go to https://github.com/coreybutler/nvm-windows
-  there go to "Download Now" and download Windows-Exe and install it
-  go to terminal and install node.js with nvm:


    nvm install 12.13.0

## install gulp globaly

    npm install -g gulp

## update fomantic ui

    cd frontend/semantic
    npm update fomantic-ui

## Build the semantic.min.css and semantic.min.js 

     cd ..
     cd node_modules/fomantic-ui
     npx gulp build

---

# Installing fomantic ui from scratch 

- see https://fomantic-ui.com/introduction/getting-started.html

---

# How to customize CSS

Our custom themes and the default theme override the *.less in 'src/definitions'. The folder 'src/side' is not used yet.


## Theming

- we got the themes 'laser' and 'accessibility'
- all changes in css we make by changing inside the theme!
- the original semantic ui file for gulp bilding 'app/semantic/tasks/build.js' is changed in order to build two themes at the same time (laser & accessibility)
- meanwile the gulp build process temp files are builded and moved around

## Example

I would like to change the padding between an icon and content in a list

1.) find the variable in default theme. In this case there

    src/themes/default/elements/list.variables
    
2.) create the list.variables in the laser theme folder

    src/themes/laser/elements/list.variables
    
3.) change the specifig variable only there

4.) Change the theme.config 

    /files/frontend/semantic/src/definitions/themes/laser/theme.config

    /files/frontend/semantic/src/definitions/themes/accessibility/theme.config
    
old:

    @list       : 'default';
    
new:

    @list       : 'laser';
    
5.) Build css

    cd semantic
    gulp build

