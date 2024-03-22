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
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
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

				String sourceKey = StringUtil.merge(
					new String[] {getClass().getName(), virtualHostname, host},
					"-");

				if (_hostWhitelists.contains(host)) {
					result.add(
						new HealthcheckItem(
							this, true, sourceKey,
							_LINK_BASE +
								dataProvider.getDataProviderInstanceId(),
							_MSG_WHITELISTED, dataProvider.getName(locale),
							host));
				}
				else {
					result.add(
						new HealthcheckItem(
							this, false, sourceKey,
							_LINK_BASE +
								dataProvider.getDataProviderInstanceId(),
							_MSG, dataProvider.getName(locale), host));
				}
			}
		}

		if (result.isEmpty()) {
			result.add(
				new HealthcheckItem(
					this, true, getClass().getName(), null,
					"healthcheck-dataprovider-none-detected"));
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		HealthcheckOperationalConfiguration
			healthcheckOperationalConfiguration =
				ConfigurableUtil.createConfigurable(
					HealthcheckOperationalConfiguration.class, properties);

		String[] whitelist =
			healthcheckOperationalConfiguration.dataProviderHostWhitelist();

		if (whitelist == null) {
			if (_log.isInfoEnabled()) {
				_log.info("empty DataProvider whitelist");
			}

			_hostWhitelists = new HashSet<>();
		}
		else {
			if (_log.isInfoEnabled()) {
				_log.info(
					"Number of DataProvider whitelist elements: " +
						whitelist.length);
			}

			_hostWhitelists = new HashSet<>(Arrays.asList(whitelist));
		}
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

	private static final String _LINK_BASE = new StringBundler(
	).append(
		"/group/guest/~/control_panel/manage?p_p_id="
	).append(
		DDMPortletKeys.DYNAMIC_DATA_MAPPING_DATA_PROVIDER
	).append(
		"&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_"
	).append(
		DDMPortletKeys.DYNAMIC_DATA_MAPPING_DATA_PROVIDER
	).append(
		"_displayStyle=descriptive&_"
	).append(
		DDMPortletKeys.DYNAMIC_DATA_MAPPING_DATA_PROVIDER
	).append(
		"_mvcPath=%2Fedit_data_provider.jsp&_"
	).append(
		DDMPortletKeys.DYNAMIC_DATA_MAPPING_DATA_PROVIDER
	).append(
		"_dataProviderInstanceId="
	).toString();

	private static final String _MSG =
		"healthcheck-dataprovider-detected-host-ignore-if-expected";

	private static final String _MSG_WHITELISTED =
		"healthcheck-dataprovider-whitelisted";

	private static final Log _log = LogFactoryUtil.getLog(
		FormDataProviderHealthcheck.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private DDMDataProviderInstanceLocalService
		_ddmDataProviderInstanceLocalService;

	private volatile Set<String> _hostWhitelists = new HashSet<>();

	@Reference
	private JSONFactory _jsonFactory;

}