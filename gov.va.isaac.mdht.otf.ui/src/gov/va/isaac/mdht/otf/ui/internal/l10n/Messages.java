/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.mdht.otf.ui.internal.l10n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "gov.va.isaac.mdht.otf.ui.internal.l10n.messages";//$NON-NLS-1$

	// ==============================================================================
	// Actions
	// ==============================================================================

	public static String RefsetSelection_input_title;

	public static String ConceptSelection_input_title;

	public static String ConceptSelection_input_message;

	public static String ConceptSelection_dialog_title;

	public static String ConceptSelection_dialog_message;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
