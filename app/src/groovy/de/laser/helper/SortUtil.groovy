package de.laser.helper

import java.text.*;

class SortUtil {

    static RuleBasedCollator getCollator() {

        String EXT_RULES = "" +
                "= '-',''' " +
                "< A< a< ä< Ä< B,b< C,c< D,d< E,e< F,f< G,g< H,h< I,i< J,j" +
                "< K,k< L,l< M,m< N,n< O< o< Ö< ö< P,p< Q,q< R,r< S,s< T,t" +
                "< U< u< Ü< ü< V,v< W,w< X,x< Y,y< Z,z" +
                "& ss=ß"

        Collator.getInstance(Locale.GERMAN)

        RuleBasedCollator base = (RuleBasedCollator) Collator.getInstance()
        RuleBasedCollator german = new RuleBasedCollator(base.getRules() + EXT_RULES)

        german
    }

    static List<Object> sortList(List<Object> list) {

        RuleBasedCollator germanCollator = SortUtil.getCollator()
        Collections.sort(list, germanCollator)
        list
    }

    static testCollator() {

        List<String> words = Arrays.asList(
                "Äbc", "v", "äbc", "V", "Àbc", "ü", "U", "B", "àbc", "Abc", "abc", "ABC", "b", "Ü"
        )

        words = SortUtil.sortList(words)

        println words
    }
}