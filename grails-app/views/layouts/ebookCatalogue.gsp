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

<section class="hero is-info is-bold">
    <div class="hero-body custom-hero-body">
        <div class="container">
            <span id="custom-logo"></span>
            <div>
            <h1 class="title">ebookCatalogue (working title)</h1>
            <h2 class="subtitle">
                give us some time, but <strong>here</strong> we go ..
            </h2>
            <p>DEMO - DEMO - DEMO</p>
            </div>
            <div class="card" id="custom-statistics">
                <div class="card-content has-text-right">
                    <p class="subtitle"><strong class="has-text-weight-semibold">123456</strong> EBooks,</p>
                    <p class="subtitle"><strong class="has-text-weight-semibold">6543</strong> Produkte,</p>
                    <p class="subtitle"><strong class="has-text-weight-semibold">123</strong> Anbieter,</p>
                    <p class="subtitle"><strong class="has-text-weight-semibold">999999</strong> Suchanfragen,</p>
                    <p class="subtitle">davon heute <strong class="has-text-weight-semibold">789</strong></p>
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
    #custom-logo {
        display: inline-block;
        width: 150px;
        height: 150px;
        background-image: url("${resource(dir: 'images', file: 'ebookCatalogue/library-book.svg')}");
        background-position: center;
        background-repeat: no-repeat;
    }
    #custom-statistics {
        position: absolute;
        top: -3rem;
        right: 0;
        background-color: rgba(255,255,255, 0.2);
        border-radius: 0 0 10px 10px;
        box-shadow: none;
    }
    #custom-statistics .card-content {
        padding: 1.5rem 2rem;
    }
    #custom-statistics .subtitle {
        margin-bottom: 1rem;
    }
    #custom-statistics .subtitle:last-of-type {
        margin-bottom: 0;
    }
    .custom-section-form {
        background-color: #fafafa;
    }
    .custom-section-result {
        border-top: 1px solid #3298dc;
    }

</style>
</body>
</html>