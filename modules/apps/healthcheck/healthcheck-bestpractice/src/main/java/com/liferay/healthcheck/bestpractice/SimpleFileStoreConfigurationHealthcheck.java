/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.bestpractice.configuration.HealthcheckBestPracticeConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.settings.Settings;
import com.liferay.portal.kernel.settings.SettingsLocatorHelper;
import com.liferay.portal.util.PropsValues;

import java.io.File;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * Check various conditions for proper use of the (simple) FileSystemStore. It
 * stores all files in a single directory, which might lead to performance
 * problems. Note, that even if you don't experience performance problems at
 * runtime (because Java & your file system play well together, it might be
 * shell-based tools, e.g. used for backups, that break your neck. The expected
 * maximum number of files is configurable - the default is chosen arbitrarily,
 * to motivate a rather early change of store implementation, rather than a late
 * migration when the store has a humongous number of files already.
 *
 * @author Olaf Kock
 */
@Component(
	configurationPid = {
		"com.liferay.healthcheck.bestpractice.configuration.HealthcheckBestPracticeConfiguration",
		"com.liferay.portal.store.file.system.configuration.FileSystemStoreConfiguration"
	},
	service = Healthcheck.class
)
public class SimpleFileStoreConfigurationHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		Collection<HealthcheckItem> result = new LinkedList<>();

		if (PropsValues.DL_STORE_IMPL.equals(
				"com.liferay.portal.store.file.system.FileSystemStore")) {

			if (_rootDir.isDirectory()) {
				int files = _getRecursiveMaxFiles(_rootDir, 0);

				if (files > _maximumFiles) {
					Object[] info = {
						files, _maximumFiles, _rootDir.getAbsolutePath()
					};

					result.add(
						new HealthcheckItem(
							this, false, getClass().getName() + "-maxfiles",
							_LINK, _MSG_TOO_MANY_FILES, info));
				}
				else {
					Object[] info = {
						files, _maximumFiles, _rootDir.getAbsolutePath()
					};

					result.add(
						new HealthcheckItem(
							this, true, getClass().getName() + "-maxfiles",
							_LINK, _MSG, info));
				}
			}
			else {
				Object[] info = {_rootDir.getAbsolutePath()};

				result.add(
					new HealthcheckItem(
						this, false, getClass().getName() + "-no-directory",
						_LINK, _MSG_NO_DIR, info));
			}

			Object[] info = {_minimumUsableSpace, _rootDir.getUsableSpace()};

			result.add(
				new HealthcheckItem(
					this, _rootDir.getUsableSpace() > _minimumUsableSpace,
					getClass().getName() + "-diskspace", null,
					_MSG_USABLE_SPACE, info));
		}
		else {
			result.add(
				new HealthcheckItem(
					this, true, getClass().getName(), _LINK, _MSG_UNUSED));
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		HealthcheckBestPracticeConfiguration
			healthcheckBestPracticeConfiguration =
				ConfigurableUtil.createConfigurable(
					HealthcheckBestPracticeConfiguration.class, properties);

		_maximumFiles =
			healthcheckBestPracticeConfiguration.maximumSimpleStoreFiles();
		_minimumUsableSpace =
			healthcheckBestPracticeConfiguration.minimumUsableSpace();

		Settings fileStoreSettings =
			_settingsLocatorHelper.getConfigurationBeanSettings(
				"com.liferay.portal.store.file.system.configuration." +
					"FileSystemStoreConfiguration");

		String rootPath = fileStoreSettings.getValue("rootDir", null);

		File dir = new File(rootPath);

		if (!dir.isAbsolute()) {
			dir = new File(PropsValues.LIFERAY_HOME, rootPath);
		}

		_rootDir = dir;
	}

	@Reference(unbind = "-")
	protected void setConfigurationProvider(
		ConfigurationProvider configurationProvider) {

		// configuration update will actually be handled in the @Modified event,
		// which will only be triggered in case we have a @Reference to the
		// ConfigurationProvider

	}

	private int _getRecursiveMaxFiles(File dir, int max) {
		File[] files = dir.listFiles();

		int result = Math.max(max, files.length);

		for (File file : files) {
			if (file.isDirectory()) {
				result = _getRecursiveMaxFiles(file, result);
			}
		}

		return result;
	}

	private static final String _LINK =
		"https://docs.liferay.com/portal/7.4-latest/propertiesdoc" +
			"/portal.properties.html#Document%20Library%20Service";

	private static final String _MSG = "healthcheck-simple-file-store-ok";

	private static final String _MSG_NO_DIR =
		"healthcheck-simple-file-store-no-dir";

	private static final String _MSG_TOO_MANY_FILES =
		"healthcheck-simple-file-store-too-many-files";

	private static final String _MSG_UNUSED =
		"healthcheck-simple-file-store-unused";

	private static final String _MSG_USABLE_SPACE =
		"healthcheck-simple-file-store-usable-space";

	private volatile Integer _maximumFiles;
	private volatile Long _minimumUsableSpace;
	private volatile File _rootDir;

	@Reference
	private SettingsLocatorHelper _settingsLocatorHelper;

}