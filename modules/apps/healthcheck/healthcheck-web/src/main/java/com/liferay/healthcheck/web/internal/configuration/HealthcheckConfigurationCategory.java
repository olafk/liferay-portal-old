/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.web.internal.configuration;

import com.liferay.configuration.admin.category.ConfigurationCategory;

import org.osgi.service.component.annotations.Component;

/**
 * @author Olaf Kock
 */
@Component(service = ConfigurationCategory.class)
public class HealthcheckConfigurationCategory implements ConfigurationCategory {

	public String getCategoryIcon() {
		return "check-square";
	}

	public String getCategoryKey() {
		return "healthcheck";
	}

	public String getCategorySection() {
		return "platform";
	}

}