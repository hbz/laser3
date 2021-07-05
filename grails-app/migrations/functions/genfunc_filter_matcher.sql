CREATE OR REPLACE FUNCTION GENFUNC_FILTER_MATCHER(content TEXT, test TEXT)
    RETURNS BOOLEAN
    LANGUAGE plpgsql
    AS $$

DECLARE
    VERSION CONSTANT NUMERIC = 4;

    result_phrases BOOLEAN;
    result_terms BOOLEAN;

    tmp_phrases TEXT[];
    tmp_terms TEXT[];

    phrases TEXT[];
    terms TEXT[];

    rm_cursor TEXT[];

    char_phrase TEXT;
    char_term TEXT;

BEGIN
    RAISE NOTICE 'Query: [%] contains [%]', content, test;

    /* -- find phrase with quotes -- */

    FOR rm_cursor IN SELECT regexp_matches(trim(test), '("(\w|\d|\s|[äöüÄÖÜ@]|[!§$&.,:;]|[\-\+\\])+")', 'g')
        LOOP
            phrases = array_append(phrases, rm_cursor[1]);
        END LOOP;

    RAISE NOTICE 'phrases[] ----> %', phrases;

    /* -- process phrases -- */

    if array_length(phrases, 1) > 0 THEN
        FOREACH char_phrase IN ARRAY phrases
            LOOP
                test = replace(test, char_phrase, ''); /* -- remove match from test -- */

                char_phrase = trim(both '"' from char_phrase);

                IF length(trim(char_phrase)) > 0 THEN
                    RAISE NOTICE 'char_phrase: [%]',  char_phrase;

                    select array_append(tmp_phrases, '%'||trim(char_phrase)||'%') into tmp_phrases;
                END IF;
            END LOOP;
		select into result_phrases 
			case 
				when content ilike all(tmp_phrases) then true
				else false
				end;
        RAISE NOTICE 'result_phrases --> %', result_phrases;
    END IF;

    /* -- find multiple terms divided by spaces -- */

    SELECT * INTO terms FROM regexp_split_to_array(trim(test), '\s+');

    RAISE NOTICE 'terms[]   ----> %', terms;

    /* -- process terms -- */

    if array_length(terms, 1) > 0 THEN
        FOREACH char_term IN ARRAY terms
            LOOP
                IF length(trim(char_term)) > 0 THEN
                    select array_append(tmp_terms, '%'||trim(char_term)||'%') into tmp_terms;
                END IF;
            END LOOP;
		select into result_terms 
			case
				when content ilike all(tmp_terms) then true
				else false
				end;
        RAISE NOTICE 'result_terms --> %', result_terms;
    END IF;
	
	if result_phrases is not null and result_terms is not null then
    	RETURN bool_and(result_phrases AND result_terms);
	elsif result_phrases is null then
		return result_terms;
	elsif result_terms is null then
		return result_phrases;
	end if;
END;
$$;
