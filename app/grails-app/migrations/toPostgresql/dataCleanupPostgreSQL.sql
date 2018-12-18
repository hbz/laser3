
-- fix data types
-- fix data types

ALTER TABLE public.fact ALTER COLUMN fact_value TYPE BIGINT USING fact_value::bigint; -- String factValue -> Integer factValue

-- renaming tables and constraints
-- renaming tables and constraints

-- kb_comment

ALTER TABLE public.kb_comment RENAME TO `comment`; -- table 'kb_comment' -> table (name: '`comment`')

DROP INDEX public.kb_comment_pkey RESTRICT;
CREATE UNIQUE INDEX comment_pkey ON public.comment (comm_id);

ALTER TABLE public.comment DROP CONSTRAINT kb_comment_pkey;
ALTER TABLE public.comment ADD CONSTRAINT comment_pkey PRIMARY KEY (comm_id);

-- kbplus_ord

ALTER TABLE public.kbplus_ord RENAME TO ordering; -- table 'kbplus_ord' -> table (name: 'ordering')

DROP INDEX public.kbplus_ord_pkey RESTRICT;
CREATE UNIQUE INDEX ordering_pkey ON public.ordering (ord_id);

ALTER TABLE public.ordering DROP CONSTRAINT kbplus_ord_pkey;
ALTER TABLE public.ordering ADD CONSTRAINT ordering_pkey PRIMARY KEY (ord_id);

