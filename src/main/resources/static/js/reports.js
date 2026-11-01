document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-report-filter]').forEach((controls) => {
        const section = controls.closest('.report-section');
        const search = controls.querySelector('[data-search]');
        const bloodFilter = controls.querySelector('[data-blood-filter]');
        const statusFilter = controls.querySelector('[data-status-filter]');
        const dateFrom = controls.querySelector('[data-date-from]');
        const dateTo = controls.querySelector('[data-date-to]');
        const error = section.querySelector('[data-error]');
        const noMatch = section.querySelector('[data-no-match]');
        const rows = [...section.querySelectorAll('[data-filter-row]')];

        const applyFilters = () => {
            const from = dateFrom?.value ?? '';
            const to = dateTo?.value ?? '';
            const invalidRange = Boolean(from && to && from > to);
            error.hidden = !invalidRange;
            error.textContent = invalidRange ? 'The start date must be on or before the end date.' : '';
            if (dateFrom) dateFrom.setAttribute('aria-invalid', String(invalidRange));
            if (dateTo) dateTo.setAttribute('aria-invalid', String(invalidRange));

            let visibleRows = 0;
            rows.forEach((row) => {
                const text = (row.dataset.search || row.textContent).toLocaleLowerCase();
                const rowBlood = row.dataset.blood || '';
                const rowStatus = row.dataset.status || '';
                const rowDate = row.dataset.date || '';
                const matches = !invalidRange
                    && (!search?.value.trim() || text.includes(search.value.trim().toLocaleLowerCase()))
                    && (!bloodFilter?.value || rowBlood === bloodFilter.value)
                    && (!statusFilter?.value || rowStatus === statusFilter.value)
                    && (!from || (rowDate && rowDate >= from))
                    && (!to || (rowDate && rowDate <= to));
                row.hidden = !matches;
                if (matches) visibleRows += 1;
            });
            if (noMatch) noMatch.hidden = visibleRows !== 0 || rows.length === 0 || invalidRange;
        };

        controls.querySelectorAll('input, select').forEach((control) => {
            control.addEventListener('input', applyFilters);
            control.addEventListener('change', applyFilters);
        });
        controls.querySelector('[data-reset]')?.addEventListener('click', () => {
            controls.querySelectorAll('input, select').forEach((control) => { control.value = ''; });
            applyFilters();
            search?.focus();
        });
        applyFilters();
    });
});
