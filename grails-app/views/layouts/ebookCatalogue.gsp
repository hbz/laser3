<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>ebookCatalogue</title>
    <meta name="description" content="">
    <meta name="viewport" content="initial-scale = 1.0">

    <asset:stylesheet src="ebookCatalogue.css"/>
    <asset:javascript src="ebookCatalogue.js"/>

    <tmpl:/layouts/favicon />
</head>

<body>

<section class="hero is-medium is-info is-bold">
    <div class="hero-body">
        <div class="container">
            <span id="logo"></span>
            <div>
            <h1 class="title">ebookCatalogue (working title)</h1>
            <h2 class="subtitle">
                give us some time, but <strong>here</strong> we go ..
            </h2>
            <p>DEMO - DEMO - DEMO</p>
            </div>
            <div class="card" id="statistics">
                <div class="card-content has-text-right">
                    <p class="subtitle"><strong class="has-text-weight-semibold">123456</strong> EBooks</p>
                    <p class="subtitle"><strong class="has-text-weight-semibold">6543</strong> Produkte</p>
                    <p class="subtitle"><strong class="has-text-weight-semibold">123</strong> Anbieter</p>
                    <p class="subtitle"><strong class="has-text-weight-semibold">999999</strong> Suchanfragen</p>
                </div>
            </div>
        </div>
    </div>

</section>

<g:layoutBody/>


<!--<footer class="footer">
    <div class="content has-text-centered">
        <p>give us some time, but <strong>here</strong> we go ..</p>
    </div>
</footer>-->


<style>
    #logo {

        display: inline-block;
        width: 150px;
        height: 150px;
        background-image: url("${resource(dir: 'images', file: 'ebookCatalogue/library-book.svg')}");
    }
    #statistics {
        position: absolute;
        top: -5rem;
        right: 0;
        background-color: rgba(255,255,255, 0.2);
        border-radius: 0 0 10px 10px;
        box-shadow: none;
    }
    #statistics .card-content {
        padding: 1.5rem 2rem;
    }
    #statistics .subtitle {
        margin-bottom: 1rem;
    }
    #statistics .subtitle:last-of-type {
        margin-bottom: 0;
    }
    .hero.is-medium .hero-body {
        padding: 5rem 1.5rem 6rem;
    }

</style>
</body>
</html>