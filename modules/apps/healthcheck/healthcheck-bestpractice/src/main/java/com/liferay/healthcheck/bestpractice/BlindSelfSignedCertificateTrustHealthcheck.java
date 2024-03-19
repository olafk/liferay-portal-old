/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice;

import com.liferay.dynamic.data.mapping.data.provider.configuration.DDMDataProviderConfiguration;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;

import java.util.Arrays;
import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Olaf Kock
 */
@Component(
	configurationPid = "com.liferay.healthcheck.bestpractice.configuration.HealthcheckBestPracticeConfiguration",
	service = Healthcheck.class
)
public class BlindSelfSignedCertificateTrustHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		try {
			Object[] info = {};

			return Arrays.asList(
				new HealthcheckItem(
					this, !getTrustSetting(companyId), getClass().getName(),
					_LINK, _MSG, info));
		}
		catch (ConfigurationException configurationException) {
			return Arrays.asList(
				new HealthcheckItem(this, configurationException));
		}
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

	@Reference
	protected void setConfigurationProvider(
		ConfigurationProvider configurationProvider) {

		_configurationProvider = configurationProvider;
	}

	private static final String _LINK =
		"/group/control_panel/manage?p_p_id=com_liferay_configuration_admin_" +
			"web_portlet_InstanceSettingsPortlet&p_p_lifecycle=0&" +
				"p_p_state=maximized&p_p_mode=view&_com_liferay_configuration_admin" +
					"_web_portlet_InstanceSettingsPortlet_mvcRenderCommandName=" +
						"%2Fconfiguration_admin%2Fedit_configuration&_com_liferay_" +
							"configuration_admin_web_portlet_InstanceSettingsPortlet_factoryPid" +
								"=com.liferay.dynamic.data.mapping.data.provider.configuration." +
									"DDMDataProviderConfiguration&_com_liferay_configuration_admin_web_" +
										"portlet_InstanceSettingsPortlet_pid=com.liferay.dynamic.data." +
											"mapping.data.provider.configuration.DDMDataProviderConfiguration";

	private static final String _MSG =
		"healthcheck-trust-self-signed-dataprovider-certificates";

	@Reference
	private ConfigurationProvider _configurationProvider;

}