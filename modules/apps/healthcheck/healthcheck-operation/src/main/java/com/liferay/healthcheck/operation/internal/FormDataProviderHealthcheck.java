/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.dynamic.data.mapping.constants.DDMPortletKeys;
import com.liferay.dynamic.data.mapping.model.DDMDataProviderInstance;
import com.liferay.dynamic.data.mapping.service.DDMDataProviderInstanceLocalService;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.auxiliary.DataProviderData;
import com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Olaf Kock
 */
@Component(
	configurationPid = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration",
	service = Healthcheck.class
)
public class FormDataProviderHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId)
		throws PortalException {

		Locale locale = _getDefaultLocale(companyId);
		LinkedList<HealthcheckItem> result = new LinkedList<>();

		String virtualHostname = _companyLocalService.getCompany(
			companyId
		).getVirtualHostname();

		List<DDMDataProviderInstance> ddmDataProviderInstances =
			_ddmDataProviderInstanceLocalService.getDDMDataProviderInstances(
				0, 1000000);

		HealthcheckOperationalConfiguration
			healthcheckOperationalConfiguration = null;

		if (companyId != CompanyConstants.SYSTEM) {
			healthcheckOperationalConfiguration =
				_configurationProvider.getCompanyConfiguration(
					HealthcheckOperationalConfiguration.class, companyId);
		}
		else {
			healthcheckOperationalConfiguration =
				_configurationProvider.getSystemConfiguration(
					HealthcheckOperationalConfiguration.class);
		}

		Set<String> hostWhitelist = new HashSet<>(
			Arrays.asList(
				healthcheckOperationalConfiguration.
					dataProviderHostWhitelist()));

		for (Iterator<DDMDataProviderInstance> iterator =
				ddmDataProviderInstances.iterator();
			 iterator.hasNext();) {

			DDMDataProviderInstance dataProvider =
				(DDMDataProviderInstance)iterator.next();

			DataProviderData data = _jsonFactory.looseDeserialize(
				dataProvider.getDefinition(), DataProviderData.class);

			String url = data.getUrl();

			if (url != null) {

				// create a problem indicator in any case, so that users can
				// ignore it if the URL is expected. The ignore-key includes the
				// ignored host name and the current company host name, so that
				// any system restored under a different name (when
				// company-virtualhost is configured correctly) will trigger
				// new alerts.
				// Note: There is a separate health check to validate the
				// company virtualhost, that should make this unignorable.

				String host = _getHost(url);
				Group group = _groupLocalService.fetchGroup(
					dataProvider.getGroupId());

				if (hostWhitelist.contains(host)) {
					result.add(
						new HealthcheckItem(
							true, _getLink(dataProvider, group),
							_MSG_WHITELISTED,
							_getDataProviderName(dataProvider, group, locale),
							host, virtualHostname));
				}
				else {
					result.add(
						new HealthcheckItem(
							false, _getLink(dataProvider, group), _MSG,
							_getDataProviderName(dataProvider, group, locale),
							host, virtualHostname));
				}
			}
		}

		if (result.isEmpty()) {
			result.add(
				new HealthcheckItem(true, null, _MSG_NO_DATAPROVIDER_DETECTED));
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	private String _getDataProviderName(
		DDMDataProviderInstance dataProvider, Group group, Locale locale) {

		return StringBundler.concat(
			dataProvider.getName(locale), " (", group.getName(locale), ")");
	}

	private Locale _getDefaultLocale(long companyId) throws PortalException {
		return _companyLocalService.getCompany(
			companyId
		).getLocale();
	}

	private String _getHost(String url) {
		if (url.startsWith("http")) {
			int endOfHost = url.indexOf('/', 8);

			if (endOfHost > -1) {
				return url.substring(0, endOfHost);
			}
		}

		return url;
	}

	private String _getLink(DDMDataProviderInstance dataProvider, Group group) {
		return StringBundler.concat(
			"/group", group.getFriendlyURL(), "/~/control_panel/manage?p_p_id=",
			DDMPortletKeys.DYNAMIC_DATA_MAPPING_DATA_PROVIDER,
			"&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_",
			DDMPortletKeys.DYNAMIC_DATA_MAPPING_DATA_PROVIDER,
			"_displayStyle=descriptive&_",
			DDMPortletKeys.DYNAMIC_DATA_MAPPING_DATA_PROVIDER,
			"_mvcPath=%2Fedit_data_provider.jsp&_",
			DDMPortletKeys.DYNAMIC_DATA_MAPPING_DATA_PROVIDER,
			"_dataProviderInstanceId=",
			dataProvider.getDataProviderInstanceId());
	}

	private static final String _MSG =
		"found-dataprovider-x-configured-for-nonwhitelisted-host-name-x";

	private static final String _MSG_NO_DATAPROVIDER_DETECTED =
		"no-dataprovider-detected-and-checked-for-unexpected-host-names";

	private static final String _MSG_WHITELISTED =
		"dataprovider-x-is-configured-for-whitelisted-host-x";

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private DDMDataProviderInstanceLocalService
		_ddmDataProviderInstanceLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private JSONFactory _jsonFactory;

}