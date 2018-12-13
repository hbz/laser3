
-- fix data types
-- fix data types

ALTER TABLE public.fact ALTER COLUMN fact_value TYPE BIGINT USING fact_value::bigint; -- String factValue -> Integer factValue

-- rename tables
-- rename tables

--ALTER TABLE public.kb_comment RENAME TO `comment`; -- table 'kb_comment' -> table (name: '`comment`')
--ALTER TABLE public.kbplus_ord RENAME TO `orders`; -- table 'kbplus_ord' -> table (name: '`orders`')

