create or replace function create_cell(format text, cell_data text, cell_style text)
	returns text
	language plpgsql
    as $$

declare
	VERSION CONSTANT NUMERIC = 2;
begin
	if format = 'xlsx' then
		return json_build_object('field', cell_data, 'style', cell_style);
	else
		if format = 'kbart' and (cell_data is null or cell_data = '') then
			return ' ';
		else 
			return '"' || cell_data || '"';
		end if;
	end if;
end;
$$;