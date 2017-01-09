/*
 * Copyright (C) 2014 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.preferences;

import io.ona.collect.android.R;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.InputFilter;

/**
 * Handles 'other' specific preferences.
 * 
 * @author Carl Hartung (chartung@nafundi.com)
 */
public class OtherPreferencesActivity extends AggregatePreferencesActivity
		implements OnPreferenceChangeListener {

	protected EditTextPreference mApiUrlPreference;
	protected EditTextPreference mSubmissionUrlPreference;
	protected EditTextPreference mFormListUrlPreference;
	protected EditTextPreference mProjectsUrlPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.other_preferences);

		mApiUrlPreference = (EditTextPreference) findPreference(PreferencesActivity.KEY_API_URL);
		mFormListUrlPreference = (EditTextPreference) findPreference(PreferencesActivity.KEY_FORMLIST_URL);
		mSubmissionUrlPreference = (EditTextPreference) findPreference(PreferencesActivity.KEY_SUBMISSION_URL);
		mProjectsUrlPreference = (EditTextPreference) findPreference(PreferencesActivity
				.KEY_PROJECTS_URL);

		InputFilter[] filters = { new ControlCharacterFilter(), new WhitespaceFilter() };

		mServerUrlPreference.getEditText().setFilters(filters);

		mApiUrlPreference.setOnPreferenceChangeListener(this);
		mApiUrlPreference.setSummary(mApiUrlPreference.getText());
		mApiUrlPreference.getEditText().setFilters(filters);

		mFormListUrlPreference.setOnPreferenceChangeListener(this);
		mFormListUrlPreference.setSummary(mFormListUrlPreference.getText());
		mFormListUrlPreference.getEditText().setFilters(filters);

		mSubmissionUrlPreference.setOnPreferenceChangeListener(this);
		mSubmissionUrlPreference.setSummary(mSubmissionUrlPreference.getText());
		mSubmissionUrlPreference.getEditText().setFilters(filters);

		mProjectsUrlPreference.setOnPreferenceChangeListener(this);
		mProjectsUrlPreference.setSummary(mProjectsUrlPreference.getText());
		mProjectsUrlPreference.getEditText().setFilters(filters);
	}

	/**
	 * Generic listener that sets the summary to the newly selected/entered
	 * value
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		preference.setSummary((CharSequence) newValue);
		return true;
	}

}
