import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import vm from 'node:vm';

class FakeElement {
    constructor(value = '') {
        this.value = value;
        this.hidden = false;
        this.dataset = {};
        this.textContent = '';
        this.attributes = {};
        this.listeners = {};
    }
    addEventListener(type, callback) { (this.listeners[type] ??= []).push(callback); }
    emit(type) { (this.listeners[type] ?? []).forEach((callback) => callback({ target: this })); }
    setAttribute(name, value) { this.attributes[name] = value; }
    focus() {}
}

function makeReport(rows) {
    const search = new FakeElement();
    const blood = new FakeElement();
    const status = new FakeElement();
    const from = new FakeElement();
    const to = new FakeElement();
    const reset = new FakeElement();
    const error = new FakeElement();
    const noMatch = new FakeElement();
    const section = {
        querySelector: (selector) => selector === '[data-error]' ? error : selector === '[data-no-match]' ? noMatch : null,
        querySelectorAll: () => rows,
    };
    const controls = {
        closest: () => section,
        querySelector(selector) {
            return ({
                '[data-search]': search,
                '[data-blood-filter]': blood,
                '[data-status-filter]': status,
                '[data-date-from]': from,
                '[data-date-to]': to,
                '[data-reset]': reset,
            })[selector] ?? null;
        },
        querySelectorAll: () => [search, blood, status, from, to],
    };
    rows.forEach((row) => { row.hidden = false; });
    return { controls, search, blood, status, from, to, reset, error, noMatch, rows };
}

test('report filters combine and remain isolated per report section', async () => {
    const donorReport = makeReport([
        Object.assign(new FakeElement(), { dataset: { search: '1 Report Donor O+ Active', blood: 'O+', status: 'Active', date: '' } }),
        Object.assign(new FakeElement(), { dataset: { search: '2 Other Donor A- Archived', blood: 'A-', status: 'Archived', date: '' } }),
    ]);
    const donationReport = makeReport([
        Object.assign(new FakeElement(), { dataset: { search: '5 Report Donor O+ COMPLETED', blood: 'O+', status: 'COMPLETED', date: '2026-03-05' } }),
        Object.assign(new FakeElement(), { dataset: { search: '6 Report Donor O+ PENDING', blood: 'O+', status: 'PENDING', date: '2026-02-03' } }),
    ]);
    const reports = [donorReport, donationReport];
    const document = {
        addEventListener(type, callback) { if (type === 'DOMContentLoaded') callback(); },
        querySelectorAll: () => reports.map((report) => report.controls),
    };
    const script = await readFile(new URL('../../main/resources/static/js/reports.js', import.meta.url), 'utf8');
    vm.runInNewContext(script, { document });

    donorReport.blood.value = 'O+';
    donorReport.blood.emit('input');
    assert.deepEqual(donorReport.rows.map((row) => row.hidden), [false, true]);
    assert.deepEqual(donationReport.rows.map((row) => row.hidden), [false, false]);
    donorReport.search.value = 'no matching donor';
    donorReport.search.emit('input');
    assert.deepEqual(donorReport.rows.map((row) => row.hidden), [true, true]);
    assert.equal(donorReport.noMatch.hidden, false);
    donorReport.reset.emit('click');
    assert.deepEqual(donorReport.rows.map((row) => row.hidden), [false, false]);

    donationReport.search.value = 'report donor';
    donationReport.blood.value = 'O+';
    donationReport.status.value = 'COMPLETED';
    donationReport.from.value = '2026-03-01';
    donationReport.to.value = '2026-03-31';
    donationReport.to.emit('change');
    assert.deepEqual(donationReport.rows.map((row) => row.hidden), [false, true]);
    assert.equal(donationReport.noMatch.hidden, true);

    donationReport.from.value = '2026-04-01';
    donationReport.to.value = '2026-03-31';
    donationReport.from.emit('input');
    assert.equal(donationReport.error.hidden, false);
    assert.equal(donationReport.error.textContent, 'The start date must be on or before the end date.');
    assert.deepEqual(donationReport.rows.map((row) => row.hidden), [true, true]);

    donationReport.reset.emit('click');
    assert.equal(donationReport.from.value, '');
    assert.equal(donationReport.to.value, '');
    assert.equal(donationReport.error.hidden, true);
    assert.deepEqual(donationReport.rows.map((row) => row.hidden), [false, false]);
});
