/*******************************************************************************
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package gov.va.isaac.mdht.otf.services;

import gov.va.isaac.mdht.otf.internal.store.AppBdbTerminologyStore;

public class TerminologyStoreFactory {
	
	public static TerminologyStoreFactory INSTANCE = new TerminologyStoreFactory();

	private TerminologyStoreService terminologyStoreService = null;

	private ConceptQueryService conceptQueryService = null;

	private ConceptBuilderService conceptBuilderService = null;

	private ConceptPrinterService conceptPrinterService = null;

	private TerminologyStoreFactory() { }

	public TerminologyStoreService createTerminologyStoreService() {
		if (terminologyStoreService == null) {
			terminologyStoreService = new TerminologyStoreService(AppBdbTerminologyStore.INSTANCE);
		}
		
		return terminologyStoreService;
	}

	public ConceptQueryService createConceptQueryService() {
		if (conceptQueryService == null) {
			conceptQueryService = new ConceptQueryService(AppBdbTerminologyStore.INSTANCE);
		}
		
		return conceptQueryService;
	}

	public ConceptBuilderService createConceptBuilderService() {
		if (conceptBuilderService == null) {
			conceptBuilderService = new ConceptBuilderService(AppBdbTerminologyStore.INSTANCE);
		}
		
		return conceptBuilderService;
	}

	public ConceptPrinterService createConceptPrinterService() {
		if (conceptPrinterService == null) {
			conceptPrinterService = new ConceptPrinterService(AppBdbTerminologyStore.INSTANCE);
		}
		
		return conceptPrinterService;
	}
}
