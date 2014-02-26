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
package gov.va.isaac.mdht.otf.internal;

import gov.va.isaac.mdht.otf.internal.store.AppBdbTerminologyStore;

import java.util.Collection;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		
		Collection<? extends ConceptChronicleBI> uncommitted = AppBdbTerminologyStore.INSTANCE.getStore().getUncommittedConcepts();
		if (uncommitted.size() > 0) {
			System.out.println("**** closing with " + uncommitted.size() + " uncommitted concepts.");
			for (ConceptChronicleBI concept : uncommitted) {
				AppBdbTerminologyStore.INSTANCE.getStore().forget(concept);
			}
		}
		
		// shutdown the database
		AppBdbTerminologyStore.INSTANCE.shutdown();
	}

}
