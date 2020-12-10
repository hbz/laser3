<h4 class="ui header">Dropdowns</h4>
<!-- START Example with no form selection -->
    <div class="dropdown example">

        <div class="html ui top attached segment">
            <ui>
                <li>versehen mit Label, das mit Extra-Inputdfeld verbunden ist </li>
                <li><g:link controller="myInstitution" action="currentSubscriptions">zum Beispiel hier verwendet (Merkmal innerhalb Filter)</g:link></li>
            </ui>
            <br />
            <div class="field">
                <label for="filterPropDef">
                    Merkmal
                    <i aria-hidden="true" class="question circle icon la-popup"></i>
                    <div class="ui  popup ">
                        <i aria-hidden="true" class="shield alternate icon"></i> = Mein Merkmal
                    </div>
                </label>
                <div class="ui search selection dropdown ">
                    <input type="hidden" name="filterPropDef">
                    <i aria-hidden="true" class="dropdown icon"></i>
                    <input class="search" id="filterPropDef">
                    <div class="default text">Bitte auswählen</div>
                    <div class="menu">
                        <div class="item selected" data-value="1">Abbestellgrund</div>
                        <div class="item" data-value="2">Abbestellquote</div>
                        <div class="item" data-value="3">AGB <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="4">Alternativname <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="5" >Archivzugriff</div>
                        <div class="item" data-value="6" >Bei hbz Aufnahme der Metadaten nachfragen <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="7">Bestellnummer im Erwerbungssystem</div>
                        <div class="item" data-value="8">Bundesweit offen</div>
                        <div class="item" data-value="9">DBIS-Eintrag</div>
                        <div class="item" data-value="10">DBIS-Link</div>
                    </div>
                </div>
            </div>

            <div class="ui top attached label">WCAG-Proof Dropdown mit hidden Input-Feld (Barrierefrei)</div>
        </div>


            <div class="ui instructive bottom attached segment">
                <pre aria-hidden="true">
                    &lt;div class=&quot;field&quot;&gt;
                    &lt;label <strong>for=&quot;filterPropDef&quot;</strong>&gt;
                Merkmal
                &lt;i class=&quot;question circle icon la-popup&quot;&gt;&lt;/i&gt;
                &lt;div class=&quot;ui  popup &quot;&gt;
                &lt;i class=&quot;shield alternate icon&quot;&gt;&lt;/i&gt; = Meine Merkmal
                &lt;/div&gt;
                &lt;/label&gt;
                &lt;div class=&quot;ui search selection dropdown la-filterPropDef&quot;&gt;
                &lt;input type=&quot;hidden&quot; name=&quot;filterPropDef&quot;&gt; &lt;i class=&quot;dropdown icon&quot;&gt;&lt;/i&gt;
                &lt;input class=&quot;search&quot; <strong>id=&quot;filterPropDef&quot;</strong> &gt;
                &lt;div class=&quot;default text&quot;&gt;Bitte ausw&auml;hlen&lt;/div&gt;
                &lt;div class=&quot;menu&quot;&gt;
                &lt;div class=&quot;item selected&quot; data-value=&quot;linkurl&quot;&gt;Abbestellgrund&lt;/div&gt;
                &lt;div class=&quot;item&quot; data-value=&quot;linkurl&quot;&gt;Abbestellquote&lt;/div&gt;
                &lt;div class=&quot;item&quot; data-value=&quot;linkurl&quot;&gt;AGB &lt;i class=&quot;shield alternate icon&quot;&gt;&lt;/i&gt;&lt;/div&gt;
                &lt;/div&gt;
                &lt;/div&gt;
                &lt;/div&gt;
                </pre>
            </div>

    </div>
<!-- STOP Example with no form selection -->

<!-- START Example with form selection -->
<div class="dropdown example">

    <div class="html ui top attached segment">
        <ui>
            <li></li>
            <li></li>
        </ui>
        <br />
        <div class="field">
            <label for="filterPropDef">
                Merkmal
                <i aria-hidden="true" class="question circle icon la-popup"></i>
                <div class="ui  popup ">
                    <i aria-hidden="true" class="shield alternate icon"></i> = Mein Merkmal
                </div>
            </label>
            <select class="ui dropdown">
                <option value="">Gender</option>
                <option value="1">Male</option>
                <option value="0">Female</option>
            </select>
        </div>
        <div class="ui top attached label">Dropdown, dass per Semantic UI Javascript aus HTML Select gebildet wird</div>
    </div>


    <div class="ui instructive bottom attached segment">
        <pre aria-hidden="true">
            &lt;select class="ui dropdown">
                &lt;option value="">Gender</option>
                &lt;option value="1">Male</option>
             &lt;option value="0">Female</option>
            &lt;/select>
        </pre>
    </div>

</div>
<!-- STOP Example with form selection -->

<!-- START Example search dropdown -->
<div class="dropdown example">

    <div class="html ui top attached segment">
        <ui>
            <li></li>
            <li></li>
        </ui>
        <br />
        <div class="field">
            <label for="filterPropDef">
                Merkmal
                <i aria-hidden="true" class="question circle icon la-popup"></i>
                <div class="ui  popup ">
                    <i aria-hidden="true" class="shield alternate icon"></i> = Mein Merkmal
                </div>
            </label>
            <select class="ui fluid search dropdown">
                <option value="">Gender</option>
                <option value="1">Male</option>
                <option value="0">Female</option>
            </select>
        </div>
        <div class="ui top attached label">Such-Dropdown, dass per Semantic UI Javascript aus HTML Select gebildet wird</div>
    </div>


    <div class="ui instructive bottom attached segment">
        <pre aria-hidden="true">
            &lt;select class="ui fluid search dropdown">
            &lt;option value="">Gender</option>
            &lt;option value="1">Male</option>
            &lt;option value="0">Female</option>
            &lt;/select>
        </pre>
    </div>

</div>
<!-- STOP Example search dropdown -->

