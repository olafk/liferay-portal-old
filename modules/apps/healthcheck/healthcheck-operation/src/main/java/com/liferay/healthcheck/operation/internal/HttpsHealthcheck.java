/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.auxiliary.HostNameExtractingFilter;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import javax.servlet.Filter;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * It's 2022 (when I write this check). Make sure we're accessed through https
 * only. Unless we're on localhost
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class HttpsHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {

		// just for rubbing it in in the message

		int year = Calendar.getInstance(
		).get(
			Calendar.YEAR
		);

		HostNameExtractingFilter hostNameExtractingFilter =
			(HostNameExtractingFilter)_filter;

		Collection<HealthcheckItem> result = new LinkedList<>();
		Set<String> urls = hostNameExtractingFilter.getAccessedUrls(companyId);

		for (String requestedUrl : urls) {
			String scheme = _extractScheme(requestedUrl);
			String host = _extractHost(requestedUrl);

			if ((host != null) &&
				(StringUtil.equalsIgnoreCase(host, "localhost") ||
				 host.toLowerCase(
				 ).startsWith(
					 "localhost:"
				 ))) {

				result.add(
					new HealthcheckItem(
						this, true, getClass().getName(), _LINK, _MSG_LOCALHOST,
						year, scheme));
			}
			else {
				result.add(
					new HealthcheckItem(
						this,
						(scheme != null) &&
						StringUtil.equalsIgnoreCase(scheme, "https"),
						StringUtil.merge(
							new String[] {
								getClass().getName(),
								HtmlUtil.escapeURL(requestedUrl)
							},
							"-"),
						_LINK, _MSG, year, requestedUrl));
			}
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	private String _extractHost(String url) {
		if (url == null) {
			return "null";
		}

		int separatorIndex = url.indexOf("://");

		if (separatorIndex < 1) { // not found, and should have a scheme leading up to it

			return "???";
		}

		return HtmlUtil.escape(url.substring(separatorIndex + 3));
	}

	private String _extractScheme(String url) {
		if (url == null) {
			return "null";
		}

		int separatorIndex = url.indexOf("://");

		if (separatorIndex < 1) { // not found, and should have a scheme leading up to it

			return "???";
		}

		return HtmlUtil.escape(url.substring(0, separatorIndex));
	}

	private static final String _LINK = null;

	private static final String _MSG = "healthcheck-https-in-year-x";

	private static final String _MSG_LOCALHOST =
		"healthcheck-https-localhost-in-year-x";

	@Reference(
		target = "(servlet-filter-name=Healthcheck Hostname Extracting Filter)"
	)
	private Filter _filter;

}