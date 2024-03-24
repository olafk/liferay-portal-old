/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * Check Elasticsearch's configuration and signal if Sidecar (unsupported) is
 * detected. This can easily be ignored in test/dev/demo systems, but should
 * not be running in production.
 *
 * @author Olaf Kock
 */
@Component(
	configurationPid = "com.liferay.portal.search.elasticsearch7.configuration.ElasticsearchConfiguration",
	service = Healthcheck.class
)
public class RemoteElasticHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		boolean remote = false;
		boolean configured =
			(_operationMode != null) || (_productionModeEnabled != null);

		if (configured) {
			if ((_productionModeEnabled != null) &&
				_productionModeEnabled.equals("true")) {

				remote = true;
			}
			else if ((_operationMode != null) &&
					 _operationMode.equals("REMOTE")) {

				remote = true;
			}
		}

		return Arrays.asList(
			new HealthcheckItem(
				remote, _LINK, _MSG, _productionModeEnabled, _operationMode));
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_productionModeEnabled = (String)properties.get(
			"productionModeEnabled");
		_operationMode = (String)properties.get("operationMode");
	}

	// configuration update will actually be handled in the @Modified event,
	// which will only be triggered in case we have a @Reference to the
	// ConfigurationProvider

	@Reference
	protected ConfigurationProvider configurationProvider;

	private static final String _LINK =
		"https://learn.liferay.com/dxp/latest/en/using-search/installing-and-" +
			"upgrading-a-search-engine/elasticsearch/connecting-to-" +
				"elasticsearch.html";

	private static final String _MSG =
		"elasticsearch-sidecar-not-supported-detected-prodmode-x-and-opmode-x";

	private volatile String _operationMode;
	private volatile String _productionModeEnabled;

}