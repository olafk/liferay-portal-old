/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck;

import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Olaf Kock
 */
@ProviderType
public interface HostnameDetector {

	public Set<String> getAccessedUrls(long companyId);

	public boolean isActive();

}