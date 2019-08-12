CREATE OR REPLACE FUNCTION GENFUNC_FILTER_MATCHER(content TEXT, test TEXT)
    RETURNS boolean AS $$

DECLARE
    VERSION CONSTANT NUMERIC = 1;

    result_phrases BOOLEAN = false;
    result_terms BOOLEAN = false;

    tmp_phrases BOOLEAN = false;
    tmp_terms BOOLEAN = false;

    phrases text[];
    terms text[];

    rm_cursor text[];

    char_phrase CHARACTER VARYING;
    char_term CHARACTER VARYING;

BEGIN
    RAISE NOTICE 'Query: [%] contains [%]', content, test;

    -- find phrase with quotes

    FOR rm_cursor IN SELECT regexp_matches(trim(test), '("(\w|\d|\s|[[:punct:]])+")', 'g')
        LOOP
            phrases = array_append(phrases, rm_cursor[1]);
        END LOOP;

    RAISE NOTICE 'phrases[] ----> %', phrases;

    -- process phrases

    if array_length(phrases, 1) > 0 THEN
        FOREACH char_phrase IN ARRAY phrases
            LOOP
                test = replace(test, char_phrase, ''); -- remove match from test

                char_phrase = trim(both '"' from char_phrase);

                IF length(trim(char_phrase)) > 0 THEN
                    RAISE NOTICE 'char_phrase: [%]',  char_phrase;

                    SELECT TRUE into tmp_phrases WHERE content LIKE '%'||char_phrase||'%';
                    result_phrases = bool_or(result_phrases or tmp_phrases);
                END IF;
            END LOOP;

        RAISE NOTICE 'result_phrases --> %', result_phrases;
    END IF;

    -- find multiple terms divided by spaces

    SELECT * INTO terms FROM regexp_split_to_array(trim(test), '\s+');

    RAISE NOTICE 'terms[]   ----> %', terms;

    -- process terms

    if array_length(terms, 1) > 0 THEN
        FOREACH char_term IN ARRAY terms
            LOOP
                IF length(trim(char_term)) > 0 THEN
                    SELECT TRUE INTO tmp_terms WHERE content ILIKE '%'||trim(char_term)||'%';
                    result_terms = bool_or(result_terms or tmp_terms);
                END IF;
            END LOOP;

        RAISE NOTICE 'result_terms --> %', result_terms;
    END IF;

    RETURN bool_or(result_phrases OR result_terms);
END;
$$ LANGUAGE 'plpgsql';