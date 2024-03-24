/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.configuration.admin.constants.ConfigurationAdminPortletKeys;
import com.liferay.dynamic.data.mapping.data.provider.configuration.DDMDataProviderConfiguration;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;

import java.util.Arrays;
import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Configuration settings allow to "trust all certificates" for DataProviders.
 * As this is a security issue in production systems, we'll signal the
 * detection of this setting. Can be ignored in DEV systems, but shouldn't be
 * ignored in PRD.
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class BlindSelfSignedCertificateTrustHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId)
		throws PortalException {

		return Arrays.asList(
			new HealthcheckItem(!getTrustSetting(companyId), _LINK, _MSG));
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	protected boolean getTrustSetting(long companyId)
		throws ConfigurationException {

		DDMDataProviderConfiguration ddmDataProviderConfiguration =
			_configurationProvider.getCompanyConfiguration(
				DDMDataProviderConfiguration.class, companyId);

		return ddmDataProviderConfiguration.trustSelfSignedCertificates();
	}

	private static final String _DDM =
		"com.liferay.dynamic.data.mapping.data.provider.configuration." +
			"DDMDataProviderConfiguration";

	// TODO: Replace with _portal.getControlPanelFullURL() implementation

	private static final String _LINK = new StringBundler(
	).append(
		"/group/control_panel/manage?p_p_id="
	).append(
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS
	).append(
		"&_"
	).append(
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS
	).append(
		"_mvcRenderCommandName=%2Fconfiguration_admin%2Fedit_configuration&_"
	).append(
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS
	).append(
		"_factoryPid="
	).append(
		_DDM
	).append(
		"&_"
	).append(
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS
	).append(
		"_pid="
	).append(
		_DDM
	).toString();

	private static final String _MSG =
		"do-not-trust-self-signed-dataprovider-certificates";

	@Reference
	private ConfigurationProvider _configurationProvider;

}