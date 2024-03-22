/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.web.internal.portlet;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.web.internal.constants.HealthcheckWebPortletKeys;
import com.liferay.portal.kernel.exception.RolePermissionsException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Olaf Kock
 */
@Component(
	property = {
		"com.liferay.portlet.ajaxable=true",
		"com.liferay.portlet.display-category=category.hidden",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=false",
		"com.liferay.portlet.remoteable=true",
		"javax.portlet.display-name=HealthcheckWeb",
		"javax.portlet.init-param.template-path=/META-INF/resources/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + HealthcheckWebPortletKeys.HEALTHCHECK_WEB_PORTLET,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=administrator",
		"javax.portlet.version=3.0"
	},
	service = Portlet.class
)
public class HealthcheckWebPortlet extends MVCPortlet {

	@Override
	public void doView(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		List<HealthcheckItem> checks = new LinkedList<>();
		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		if (themeDisplay.getPermissionChecker(
			).isCompanyAdmin(
				themeDisplay.getCompanyId()
			) &&
			!Objects.equals(
				_portal.getHttpServletRequest(
					renderRequest
				).getMethod(),
				"HEAD")) {

			for (Healthcheck healthcheck : _healthchecks) {
				try {
					checks.addAll(
						healthcheck.check(themeDisplay.getCompanyId()));
				}
				catch (Exception exception) {
					_log.warn(exception);
					checks.add(new HealthcheckItem(healthcheck, exception));
				}
			}
		}
		else {
			Healthcheck dummy = new Healthcheck() {

				@Override
				public Collection<HealthcheckItem> check(long companyId) {
					return null;
				}

				@Override
				public String getCategory() {
					return "healthcheck-category-generic";
				}

			};

			checks.add(
				new HealthcheckItem(dummy, new RolePermissionsException()));
		}

		Collections.sort(
			checks,
			new Comparator<HealthcheckItem>() {

				@Override
				public int compare(HealthcheckItem arg0, HealthcheckItem arg1) {
					if (arg0.isResolved() == arg1.isResolved()) {
						return arg0.getCategory(
						).compareTo(
							arg1.getCategory()
						);
					}
					else if (arg0.isResolved()) {
						return 1;
					}

					return -1;
				}

			});

		PortletPreferences portletPreferences = renderRequest.getPreferences();

		String[] ignoredChecksArray = portletPreferences.getValues(
			"ignore", new String[0]);

		Set<String> ignoreChecks = new HashSet<>(
			Arrays.asList(ignoredChecksArray));

		int failed = 0;
		int succeeded = 0;
		int ignored = 0;

		for (Iterator<HealthcheckItem> iterator = checks.iterator();
			 iterator.hasNext();) {

			HealthcheckItem check = (HealthcheckItem)iterator.next();

			if (ignoreChecks.contains(check.getKey())) {
				iterator.remove();
				ignored++;
			}
			else if (check.isResolved()) {
				succeeded++;
			}
			else {
				failed++;
			}
		}

		renderRequest.setAttribute("checks", checks);
		renderRequest.setAttribute("failedChecks", failed);
		renderRequest.setAttribute("ignoredChecks", ignored);
		renderRequest.setAttribute("succeededChecks", succeeded);
		renderRequest.setAttribute("the-ignored-checks", ignoreChecks);

		super.doView(renderRequest, renderResponse);
	}

	public void ignoreMessage(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		if (themeDisplay.getPermissionChecker(
			).isCompanyAdmin(
				themeDisplay.getCompanyId()
			) && Objects.equals(actionRequest.getMethod(), "POST")) {

			String key = ParamUtil.getString(actionRequest, "ignore");

			PortletPreferences portletPreferences =
				actionRequest.getPreferences();

			String[] ignoredArray = portletPreferences.getValues(
				"ignore", new String[0]);

			Set<String> ignoredKeys = new HashSet<>(
				Arrays.asList(ignoredArray));

			ignoredKeys.add(key);

			portletPreferences.setValues(
				"ignore", (String[])ignoredKeys.toArray(new String[0]));

			portletPreferences.store();
		}
	}

	public void resetIgnore(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		if (themeDisplay.getPermissionChecker(
			).isCompanyAdmin(
				themeDisplay.getCompanyId()
			)) {

			PortletPreferences portletPreferences =
				actionRequest.getPreferences();

			portletPreferences.setValues("ignore", new String[0]);

			portletPreferences.store();
		}
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policyOption = ReferencePolicyOption.GREEDY, unbind = "_unregister"
	)
	private void _register(Healthcheck healthcheck) {
		_healthchecks.add(healthcheck);
	}

	private void _unregister(Healthcheck healthcheck) {
		_healthchecks.remove(healthcheck);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		HealthcheckWebPortlet.class);

	private final List<Healthcheck> _healthchecks = new LinkedList<>();

	@Reference
	private Portal _portal;

}
