# Initial setup

## install nodejs (need only for changes in CSS)

    sudo apt-get install --yes nodejs

## install gulp (need only for changes in CSS)

    npm install -g gulp

## install chartist (need only for changes in CSS)

    cd [laser]/app 
    npm install chartist --save
    
    
Result: app/nodes_moules/chartist
    
## install sass (need only for changes in CSS)


    cd app
    npm i gulp-sass
    npm i gulp-postcss
    npm i node-sass
    npm i nanocss
    
## build CSS out of SCSS and minify CSS (need only for changes in CSS)

    cd app/chartist
    gulp build
    
Results: 
1. chartist.css in chartist/dist 
2. chartist.css (minfied) in web-app/chartist/css/chartist.css

## use chartist in views - include require tag

    <r:require module="chartist" />
    
## use chartist in views -  include chartist object call in javascript

     <r:script />
            
        new Chartist.Line('.ct-chart', data);
            
    </r:script>
    
## use chartist in views - include data

        var data = {
            // A labels array that can contain any sort of values
            labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'],
            // Our series array that contains series objects or in this case series data arrays
            series: [
                [5, 2, 4, 2, 0]
            ]
        };