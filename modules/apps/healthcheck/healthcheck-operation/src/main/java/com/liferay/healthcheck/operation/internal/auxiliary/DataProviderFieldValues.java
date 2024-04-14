/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal.auxiliary;

import com.liferay.portal.kernel.json.JSON;

/**
 * @author Olaf Kock
 */
@JSON
public class DataProviderFieldValues {

	@JSON
	public String fieldReference;

	@JSON
	public String value;

}