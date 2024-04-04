/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {loginTest} from '../../fixtures/loginTest';
import {ViewClientExtensionPage} from './pages/ViewClientExtensionPage';

export const test = mergeTests(loginTest());

const SAMPLE = {
	erc: 'LXC:liferay-sample-js-import-maps-entry-1',
	name: 'Liferay Sample JS Import Maps Entry 1',
	url: '/o/liferay-sample-js-import-maps-entry-1/jquery.db7c7063a8b5d1298dbc.js',
};

test(`${SAMPLE.name} is registered`, async ({page}) => {
	const viewClientExtensionPage = new ViewClientExtensionPage(
		page,
		SAMPLE.erc
	);

	await viewClientExtensionPage.goto();

	expect(viewClientExtensionPage.nameLocator).toHaveValue(SAMPLE.name);
	expect(viewClientExtensionPage.fieldLocator('URL')).toHaveValue(SAMPLE.url);
});

test(`${SAMPLE.name}'s .js file can be downloaded`, async ({page}) => {
	const response = await page.goto(SAMPLE.url);

	expect(response.status()).toBe(200);
});

test(`${SAMPLE.name} appears in import maps`, async ({page}) => {
	await page.goto('/');

	const importMap = await page
		.locator('script[type="importmap"]')
		.evaluate((node: HTMLScriptElement) => node.innerText);

	expect(importMap).toContain(`"jquery":"${SAMPLE.url}"`);
});
