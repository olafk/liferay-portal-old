/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal.auxiliary;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.Portal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Keep track of all host names that were ever requested, including the scheme
 * (http/https). These hosts will be used to check for allowed redirection of
 * any host name used to access any content in this whole instance.
 *
 * @author Olaf Kock
 */
@Component(property = { "before-filter=Auto Login Filter",
		"dispatcher=REQUEST",
		"servlet-context-name=",

		// Note: servlet-filter-name is used as target expression in
		// com.liferay.portal.health.check.operation.RedirectHealthCheck

		"servlet-filter-name=HealthCheck Hostname Extracting Filter",
		"url-pattern=/*"
		},
service = Filter.class)
public class HostNameExtractingFilter extends BaseFilter {

	public Set<String> getAccessedUrls(long companyId) {
		return Collections.unmodifiableSet(_requestedBaseUrls.get(companyId));
	}

	@Override
	protected Log getLog() {
		return _log;
	}

	@Override
	protected void processFilter(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, FilterChain filterChain)
		throws Exception {

		String host = httpServletRequest.getServerName();
		String scheme = httpServletRequest.getScheme();
		long companyId = _portal.getCompanyId(httpServletRequest);

		if ((host != null) && (host.length() > 1)) {
			HashSet<String> urls = _requestedBaseUrls.get(companyId);

			if (urls == null) {
				urls = new HashSet<>();

				_requestedBaseUrls.put(companyId, urls);
			}

			urls.add(scheme + "://" + host);
		}

		super.processFilter(
			httpServletRequest, httpServletResponse, filterChain);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		HostNameExtractingFilter.class);

	@Reference
	private Portal _portal;

	private final HashMap<Long, HashSet<String>> _requestedBaseUrls =
		new HashMap<>();

}