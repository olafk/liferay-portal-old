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
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
public class ClientExtensionHostHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws PortalException {
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

				for (String url : urls) {
					String host = _getHost(url);
					String parameterizedLink =
						_LINK_BASE +
							clientExtensionEntry.getExternalReferenceCode();

					if (host != null) {
						String sourceKey = new StringBundler(
							getClass().getName()
						).append(
							"-"
						).append(
							virtualHostname
						).append(
							"-"
						).append(
							host
						).toString();

						if (_hostWhitelists.contains(host)) {
							result.add(
								new HealthcheckItem(
									this, true, sourceKey, parameterizedLink,
									_MSG_WHITELISTED,
									clientExtensionEntry.getName(locale),
									host));
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
									this, false, sourceKey, parameterizedLink,
									_MSG, clientExtensionEntry.getName(locale),
									host));
						}
					}
					else {
						String sourceKey = new StringBundler(
							getClass().getName()
						).append(
							"-"
						).append(
							url
						).toString();

						if (url.startsWith("/document")) {
							result.add(
								new HealthcheckItem(
									this, true, sourceKey, parameterizedLink,
									_MSG_LOCAL_DOCLIB,
									clientExtensionEntry.getName(locale), url));
						}
						else {
							result.add(
								new HealthcheckItem(
									this, false, sourceKey, parameterizedLink,
									_MSG_UNDETECTABLE_HOST,
									clientExtensionEntry.getName(locale), url));
						}
					}
				}
			}

			if (result.isEmpty()) {
				result.add(
					new HealthcheckItem(
						this, true, getClass().getName(), null,
						_MSG_NO_CLIENT_EXTENSION_DETECTED));
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
			healthcheckOperationalConfiguration.clientExtensionHostWhitelist();

		if (whitelist == null) {
			_hostWhitelists = new HashSet<>();
		}
		else {
			_hostWhitelists = new HashSet<>(Arrays.asList(whitelist));
		}
	}

	private Locale _getDefaultLocale(long companyId) {
		Locale result;

		try {
			result = _companyLocalService.getCompany(
				companyId
			).getLocale();
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(portalException);
			}

			result = LocaleUtil.US;
		}

		return result;
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
		"_com_liferay_client_extension_web_internal_portlet_" +
			"ClientExtensionAdminPortlet";

	private static final String _LINK_BASE = new StringBundler(
	).append(
		"/group/guest/~/control_panel/manage?p_p_id="
	).append(
		_CX_NAMESPACE
	).append(
		"&p_p_lifecycle=0&"
	).append(
		_CX_NAMESPACE
	).append(
		"_mvcRenderCommandName=%2Fclient_extension_admin%2F"
	).append(
		"edit_client_extension_entry&"
	).append(
		_CX_NAMESPACE
	).append(
		"_externalReferenceCode="
	).toString();

	private static final String _MSG = "healthcheck-client-extension-host";

	private static final String _MSG_LOCAL_DOCLIB =
		"healthcheck-client-extension-local-doclib";

	private static final String _MSG_NO_CLIENT_EXTENSION_DETECTED =
		"healthcheck-client-extension-none-detected";

	private static final String _MSG_UNDETECTABLE_HOST =
		"healthcheck-client-extension-undetectable-host";

	private static final String _MSG_WHITELISTED =
		"healthcheck-client-extension-host-whitelisted";

	private static final Log _log = LogFactoryUtil.getLog(
		ClientExtensionHostHealthcheck.class);

	@Reference
	private volatile ClientExtensionEntryLocalService
		_clientExtensionEntryLocalService;

	@Reference
	private CompanyLocalService _companyLocalService;

	private volatile Set<String> _hostWhitelists = new HashSet<>();

}