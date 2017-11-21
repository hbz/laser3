# initial setup

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

