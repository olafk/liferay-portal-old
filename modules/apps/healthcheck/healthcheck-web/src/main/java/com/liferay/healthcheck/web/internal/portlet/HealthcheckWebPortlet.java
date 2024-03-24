/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.web.internal.portlet;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.web.internal.constants.HealthcheckWebPortletKeys;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.osgi.service.tracker.collections.map.PropertyServiceReferenceComparator;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
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
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * A temporary ugly UI for showing Healthcheck results. Not meant to
 * be committed to master, but just provides a simple UI to check the result
 * of the first healthchecks implemented.
 *
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

		List<LocalizedHealthcheckItem> localizedHealthcheckItems =
			new LinkedList<>();

		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Locale locale = themeDisplay.getLocale();

		if (themeDisplay.getPermissionChecker(
			).isCompanyAdmin(
				themeDisplay.getCompanyId()
			) &&
			!Objects.equals(
				_portal.getHttpServletRequest(
					renderRequest
				).getMethod(),
				"HEAD")) {

			for (Healthcheck healthcheck : _serviceTrackerList.toList()) {
				try {
					Collection<HealthcheckItem> checks = healthcheck.check(
						themeDisplay.getCompanyId());

					_localize(
						locale, checks, healthcheck, localizedHealthcheckItems);
				}
				catch (Exception exception) {
					_log.error(exception);

					String healthcheckClassName = healthcheck.getClass(
					).getName();

					localizedHealthcheckItems.add(
						new LocalizedHealthcheckItem(
							false,
							_lookup(
								locale, healthcheck, healthcheck.getCategory()),
							_lookup(
								locale, healthcheck,
								"an-exception-occurred-for-x-x-x",
								healthcheckClassName,
								exception.getClass(
								).getName(),
								exception.getMessage()),
							null, healthcheckClassName + "-exception"));
				}
			}
		}
		else {
			String permissionDenied = _language.get(
				locale, "permission-denied");

			localizedHealthcheckItems.add(
				new LocalizedHealthcheckItem(
					false, permissionDenied, permissionDenied, null,
					permissionDenied));
		}

		Collections.sort(
			localizedHealthcheckItems,
			new Comparator<LocalizedHealthcheckItem>() {

				@Override
				public int compare(
					LocalizedHealthcheckItem arg0,
					LocalizedHealthcheckItem arg1) {

					if (arg0.isResolved() == arg1.isResolved()) {
						int categoryDiff = arg0.getCategory(
						).compareTo(
							arg1.getCategory()
						);

						if (categoryDiff == 0) {
							return arg0.getMessage(
							).compareTo(
								arg1.getMessage()
							);
						}

						return categoryDiff;
					}
					else if (arg0.isResolved()) {
						return 1;
					}

					return -1;
				}

			});

		PortletPreferences portletPreferences = renderRequest.getPreferences();

		boolean showIgnored = ParamUtil.get(
			renderRequest, "showIgnored", false);

		String[] ignoredChecksArray = portletPreferences.getValues(
			"ignore", new String[0]);

		Set<String> ignoredChecks = new HashSet<>(
			Arrays.asList(ignoredChecksArray));

		int failed = 0;
		int succeeded = 0;
		int ignored = 0;

		for (Iterator<LocalizedHealthcheckItem> iterator =
				localizedHealthcheckItems.iterator();
			 iterator.hasNext();) {

			LocalizedHealthcheckItem currentItem = iterator.next();

			if (ignoredChecks.contains(currentItem.getSourceKey())) {
				if (_log.isInfoEnabled()) {
					_log.info("ignored: " + currentItem.getSourceKey());
				}

				if (!showIgnored) {
					iterator.remove();
				}

				ignored++;
			}
			else if (currentItem.isResolved()) {
				if (_log.isInfoEnabled()) {
					_log.info("resolved: " + currentItem.getSourceKey());
				}

				succeeded++;
			}
			else {
				if (_log.isInfoEnabled()) {
					_log.info("failed:  " + currentItem.getSourceKey());
				}

				failed++;
			}
		}

		renderRequest.setAttribute("ignoredHealthchecks", ignoredChecks);
		renderRequest.setAttribute(
			"localizedHealthchecks", localizedHealthcheckItems);
		renderRequest.setAttribute("numberOfFailedHealthchecks", failed);
		renderRequest.setAttribute("numberOfIgnoredHealthchecks", ignored);
		renderRequest.setAttribute("numberOfSucceededHealthchecks", succeeded);

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

	public void unignoreMessage(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		if (themeDisplay.getPermissionChecker(
			).isCompanyAdmin(
				themeDisplay.getCompanyId()
			) && Objects.equals(actionRequest.getMethod(), "POST")) {

			String key = ParamUtil.getString(actionRequest, "unignore");

			PortletPreferences portletPreferences =
				actionRequest.getPreferences();

			String[] ignoredArray = portletPreferences.getValues(
				"ignore", new String[0]);

			Set<String> ignoredKeys = new HashSet<>(
				Arrays.asList(ignoredArray));

			ignoredKeys.remove(key);

			portletPreferences.setValues(
				"ignore", (String[])ignoredKeys.toArray(new String[0]));

			portletPreferences.store();

			actionResponse.getRenderParameters(
			).setValue(
				"showIgnored", "true"
			);
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerList = ServiceTrackerListFactory.open(
			bundleContext, Healthcheck.class,
			new PropertyServiceReferenceComparator<>("service.ranking"));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerList.close();
	}

	private void _localize(
		Locale locale, Collection<HealthcheckItem> healthcheckItems,
		Healthcheck healthcheck,
		List<LocalizedHealthcheckItem> localizedHealthcheckItems) {

		for (HealthcheckItem healthcheckItem : healthcheckItems) {
			String key = healthcheckItem.getMessageKey();
			Object[] parameters = healthcheckItem.getMessageParameters();

			localizedHealthcheckItems.add(
				new LocalizedHealthcheckItem(
					healthcheckItem.isResolved(),
					_lookup(locale, healthcheck, healthcheck.getCategory()),
					_lookup(locale, healthcheck, key, parameters),
					healthcheckItem.getLink(), healthcheckItem.getSourceKey()));
		}
	}

	private String _lookup(
		Locale locale, Healthcheck healthcheck, String key,
		Object... parameters) {

		String message = ResourceBundleUtil.getString(
			ResourceBundleUtil.getBundle(
				locale,
				healthcheck.getClass(
				).getClassLoader()),
			key, parameters);

		if (message == null) {
			message = _language.format(locale, key, parameters);

			if (message == null) {
				message = key;
			}
		}

		return message;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		HealthcheckWebPortlet.class);

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

	private ServiceTrackerList<Healthcheck> _serviceTrackerList;

}