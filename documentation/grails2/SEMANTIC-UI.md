# Initial setup

## install nodejs on Linux

    sudo apt-get install --yes nodejs

## install nodejs on Windows with NVM

-  go to https://github.com/coreybutler/nvm-windows
-  there go to "Download Now" and download Windows-Exe and install it
-  go to terminal and install node.js with nvm:


    nvm install 10.0.0

## install gulp

    npm install -g gulp

## install semantic-ui

    cd [laser]/files/frontend/semantic/
    npm install semantic-ui --save
    
The File semantic.json is automaticly build from your choises in the intallation prozess.
You have to choose the Folders for the source and the build
    


# How to update semantic ui
!!!!! Do not use the otherwise recommended update function of a node module 

But:

 - install the newest version of semantic ui anywhere else
 - rename the folder
 - put the renamed copy of the semantic folder in your IDE on the same level as the to updated semantic folder
 - and compare the semantic folder in your IDE
 - update the files by hand
 - be carefully and keep the old code here:
    - /files/frontend/semantic/src/definitions/modules/dropdown.js --> changes for accessibility
    - /files/frontend/semantic/src/definitions/modules/calendar --> for added UI convenience
    - /files/frontend/semantic/src/tasks/ --> changed for opportunity of two parallel themes at the same time
    
  


 
    
-----------------------------------------------------


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
    
2.) copy THE WHOLE list.variables and past it in the laser theme folder

    src/themes/laser/elements/list.variables
    
3.) make changes only there

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


# Important Informations

## Datepicker

- 'by hand' implemented the sementic-ui datepicker
- it is not in current semantic-ui version (2.4.2)
- https://github.com/Semantic-Org/Semantic-UI/pull/3256/files
