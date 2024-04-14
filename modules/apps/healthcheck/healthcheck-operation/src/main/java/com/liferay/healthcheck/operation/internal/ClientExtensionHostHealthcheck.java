/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.client.extension.model.ClientExtensionEntry;
import com.liferay.client.extension.service.ClientExtensionEntryLocalService;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
public class ClientExtensionHostHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId)
		throws PortalException {

		Locale locale = _getDefaultLocale(companyId);
		LinkedList<HealthcheckItem> result = new LinkedList<>();

		List<ClientExtensionEntry> clientExtensionEntries =
			_clientExtensionEntryLocalService.getClientExtensionEntries(
				companyId, 0, 9999);
		String virtualHostname = _companyLocalService.getCompany(
			companyId
		).getVirtualHostname();

		for (ClientExtensionEntry clientExtensionEntry :
				clientExtensionEntries) {

			HashMap<String, String> typeSettings =
				UnicodePropertiesBuilder.create(
					true
				).load(
					clientExtensionEntry.getTypeSettings()
				).build();
			String[] urls;

			String tsUrl = typeSettings.get("url");

			if (tsUrl == null) {
				String multilineUrls = typeSettings.get("urls");
				String multilineCssUrls = typeSettings.get("cssURLs");

				if ((multilineCssUrls != null) &&
					(multilineCssUrls.length() > 5)) {

					multilineUrls += "\n" + multilineCssUrls;
				}

				urls = StringUtil.split(multilineUrls, '\n');
			}
			else {
				urls = new String[] {tsUrl};
			}

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
						clientExtensionHostWhitelist()));

			for (String url : urls) {
				String host = _getHost(url);
				String parameterizedLink =
					_LINK_BASE +
						clientExtensionEntry.getExternalReferenceCode();

				if (host != null) {
					if (hostWhitelist.contains(host)) {
						result.add(
							new HealthcheckItem(
								true, parameterizedLink, _MSG_WHITELISTED,
								clientExtensionEntry.getName(locale), host,
								virtualHostname));
					}
					else {

						// create a problem indicator in any case, so that
						// users can ignore it if the URL is expected.
						// The ignore-key includes the ignored host name
						// and the current company host name, so that any
						// system restored under a different name (when
						// company-virtualhost is configured correctly)
						// will trigger new alerts.
						// Note: There is a separate health check to
						// validate the company virtualhost, that should
						// make this unignorable.

						result.add(
							new HealthcheckItem(
								false, parameterizedLink, _MSG,
								clientExtensionEntry.getName(locale), host,
								virtualHostname));
					}
				}
				else {
					if (url.startsWith("/document")) {
						result.add(
							new HealthcheckItem(
								true, parameterizedLink, _MSG_LOCAL_DOCLIB,
								clientExtensionEntry.getName(locale), url));
					}
					else {
						result.add(
							new HealthcheckItem(
								false, parameterizedLink,
								_MSG_UNDETECTABLE_HOST,
								clientExtensionEntry.getName(locale), url));
					}
				}
			}
		}

		if (result.isEmpty()) {
			result.add(
				new HealthcheckItem(
					true, null, _MSG_NO_CLIENT_EXTENSION_DETECTED));
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	private Locale _getDefaultLocale(long companyId) throws PortalException {
		return _companyLocalService.getCompany(
			companyId
		).getLocale();
	}

	private String _getHost(String url) {
		if ((url != null) && url.startsWith("http")) {
			int endOfHost = url.indexOf('/', 8);

			if (endOfHost > -1) {
				return url.substring(0, endOfHost);
			}
		}

		return null;
	}

	private static final String _CX_NAMESPACE =
		"com_liferay_client_extension_web_internal_portlet_" +
			"ClientExtensionAdminPortlet";

	private static final String _LINK_BASE = StringBundler.concat(
		"/group/guest/~/control_panel/manage?p_p_id=", _CX_NAMESPACE,
		"&p_p_lifecycle=0&_", _CX_NAMESPACE,
		"_mvcRenderCommandName=%2Fclient_extension_admin%2F",
		"edit_client_extension_entry&_", _CX_NAMESPACE,
		"_externalReferenceCode=");

	private static final String _MSG =
		"found-client-extension-x-configured-for-nonwhitelisted-host-name-x";

	private static final String _MSG_LOCAL_DOCLIB =
		"client-extension-x-is-configured-for-local-document-library";

	private static final String _MSG_NO_CLIENT_EXTENSION_DETECTED =
		"no-client-extension-detected-and-checked-for-unexpected-host-names";

	private static final String _MSG_UNDETECTABLE_HOST =
		"could-not-detect-host-name-for-client-extension-x-from-url-x";

	private static final String _MSG_WHITELISTED =
		"client-extension-x-is-configured-for-whitelisted-host-x";

	@Reference
	private volatile ClientExtensionEntryLocalService
		_clientExtensionEntryLocalService;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private ConfigurationProvider _configurationProvider;

}