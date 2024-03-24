/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck;

import com.liferay.portal.kernel.exception.PortalException;

import java.util.Collection;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Olaf Kock
 */
@ProviderType
public interface Healthcheck {

	public Collection<HealthcheckItem> check(long companyId)
		throws PortalException;

	public String getCategory();

}