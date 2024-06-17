/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect} from '@playwright/test';

export async function createIndividuals(apiHelpers, names: string[]) {
	const individuals = names.map((name) => ({
		emailAddress: `${name}@liferay.com`,
		fields: [
			{dataSourceId: 0, name: 'givenName', value: name},
			{dataSourceId: 0, name: 'familyName', value: name},
			{dataSourceId: 0, name: 'email', value: `${name}@liferay.com`},
		],
		firstName: name,
		id: `${name}@liferay.com`,
		lastName: name,
	}));

	await apiHelpers.jsonWebServicesOSBAsah.createIndividuals(individuals);
}

export async function changeTimeFilterTo(
	page: Page,
	timeFilter: string,
	cardName?: string
) {
	if (cardName) {
		await page
			.locator(`[id="container\\.report\\.${cardName}Card"]`)
			.getByRole('button', {name: 'Last 30 days'})
			.click();
		await page.getByRole('menuitem', {name: timeFilter}).click();
	}
	else {
		await page.getByRole('button', {name: 'Last 30 days'}).click();
		await page.getByRole('menuitem', {name: timeFilter}).click();
	}
}

export async function searchTerm(page: Page, searchTerm: string) {
	await page.getByPlaceholder('Search').first().click();
	await page.getByPlaceholder('Search').first().fill(searchTerm);
	await page.getByPlaceholder('Search').first().press('Enter');
}

export async function viewNameListIsNotPresent(
	page: Page,
	itemNames: string[]
) {
	for (const itemName of itemNames) {
		await expect(page.getByRole('cell', {name: itemName})).toBeHidden({
			timeout: 100 * 1000,
		});
	}
}

export async function viewNameListIsPresent(page: Page, itemNames: string[]) {
	for (const itemName of itemNames) {
		await expect(page.getByRole('cell', {name: itemName})).toBeVisible({
			timeout: 100 * 1000,
		});
	}
}
