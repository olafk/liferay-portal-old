/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal.auxiliary;

import com.liferay.healthcheck.HostnameDetector;
import com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalSystemConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Olaf Kock
 */
@Component(
	configurationPid = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalSystemConfiguration",
	service = HostnameDetector.class
)
public class HostnameDetectorImpl implements HostnameDetector {

	@Override
	public Set<String> getAccessedUrls(long companyId) {
		return _hostNameExtractingFilter.getAccessedUrls(companyId);
	}

	@Override
	public boolean isActive() {
		if (_serviceRegistration != null) {
			return true;
		}

		return false;
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		if (_log.isInfoEnabled()) {
			_log.info("Activating");
		}

		int maxFilterExecutions = ConfigurableUtil.createConfigurable(
			HealthcheckOperationalSystemConfiguration.class, properties
		).maxFilterExecutions();

		if (maxFilterExecutions < 0) {
			_useCount = Integer.MAX_VALUE;

			if (_log.isInfoEnabled()) {
				_log.info(
					"Activating unlimited filtering for host names in " +
						"healthchecks");
			}
		}
		else {
			_useCount = maxFilterExecutions;

			if (_log.isInfoEnabled()) {
				_log.info(
					"Future Filter executions limited to: " +
						maxFilterExecutions);
			}
		}

		if (_useCount > 0) {
			_startFilter();
		}
	}

	@Deactivate
	protected void deactivate() {
		if (_log.isInfoEnabled()) {
			_log.info("Deactivating");
		}

		_stopFilter();
	}

	private void _limitUsage() {
		if (_useCount < Integer.MAX_VALUE) {
			_useCount--;

			if (_log.isInfoEnabled() && ((_useCount % 1000) == 0)) {
				_log.info("Future Filter executions limited to: " + _useCount);
			}

			if (_useCount < 0) {
				_stopFilter();
			}
		}
	}

	private synchronized void _startFilter() {
		if (_serviceRegistration != null) {
			if (_log.isInfoEnabled()) {
				_log.info(
					"Filter started again, ignoring and keeping it running");
			}

			return;
		}

		if (_log.isInfoEnabled()) {
			_log.info("Starting Filter");
		}

		Bundle bundle = FrameworkUtil.getBundle(HostnameDetectorImpl.class);

		BundleContext bundleContext = bundle.getBundleContext();

		Hashtable<String, String> serviceProperties = new Hashtable<>();

		serviceProperties.put("before-filter", "Auto Login Filter");
		serviceProperties.put("dispatcher", "REQUEST");
		serviceProperties.put("servlet-context-name", "");
		serviceProperties.put(
			"servlet-filter-name", "HealthCheck Hostname Extracting Filter");
		serviceProperties.put("url-pattern", "/*");

		_serviceRegistration = bundleContext.registerService(
			Filter.class, _hostNameExtractingFilter, serviceProperties);

		if (_log.isInfoEnabled()) {
			_log.info("Filter started");
		}
	}

	private synchronized void _stopFilter() {
		if (_serviceRegistration == null) {
			if (_log.isInfoEnabled()) {
				_log.info("Filter already stopped");
			}

			return;
		}

		// If we can access a bundle, the service is still registered/running

		Bundle bundle = _serviceRegistration.getReference(
		).getBundle();

		if (bundle != null) {
			if (_log.isInfoEnabled()) {
				_log.info("Stopping Filter");
			}

			_serviceRegistration.unregister();

			_serviceRegistration = null;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		HostnameDetectorImpl.class);

	private final HostNameExtractingFilter _hostNameExtractingFilter =
		new HostNameExtractingFilter(
			new Counter() {

				public void tick() {
					_limitUsage();
				}

			});

	private ServiceRegistration<Filter> _serviceRegistration;
	private volatile int _useCount = 1000;

}