/**
 * Copyright (c) 2022-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.healthcheck.web.internal.configuration;

import com.liferay.configuration.admin.category.ConfigurationCategory;

import org.osgi.service.component.annotations.Component;

@Component(immediate=true, service = ConfigurationCategory.class)
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
