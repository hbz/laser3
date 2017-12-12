# Initial setup

## install nodejs

    sudo apt-get install --yes nodejs

## install gulp

    npm install -g gulp

## install semantic-ui

    cd [laser]/app 
    npm install semantic-ui --save
    
Result: app/nodes_moules 
    
## change CSS or JS in source ([laser]/app/semantic/src/..)

Our custom theme overrides some optional packaged themes, which override default theme.

 
##  overwrite (build) the files in destination: ([laser]/app/web-app/semantic/..)
 
    cd [laser]/app/semantic
    gulp build --> build all JS, CSS and other Resources
    
or
    
    gulp build-css -->like build but only css
    
or
    
    gulp watch -->Watch every change in folder [laser]/app/semantic and build 
    
-----------------------------------------------------


# How to customize CSS

## Theming

- we got the theme 'laser'
- all changes in css we make by changing inside the theme!

## Example

I would like to change the padding between an icon and content in a list

1.) find the variable in default theme. In this case there

    src/themes/default/elements/list.variables
    
2.) copy the whole list.variables and past it in the laser theme folder

    src/themes/laser/elements/list.variables
    
3.) make changes only there

4.) Change the theme.config 

    app/semantic/src/theme.config
    
old:

    @list       : 'default';
    
new:

    @list       : 'laser';
    
5.) Build css

    cd semantic
    gulp build

# Important Informations

## Datepicker

- 'by hand' implemented the sementic-ui datepicker
- it is not in current semantic-ui version (2.2)
- https://github.com/Semantic-Org/Semantic-UI/pull/3256/files
