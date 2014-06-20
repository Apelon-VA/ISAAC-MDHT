/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright 
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.mdht.otf.internal;

import java.io.IOException;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.ihtsdo.otf.query.lucene.LuceneDescriptionIndexer;
import org.ihtsdo.otf.query.lucene.LuceneRefexIndexer;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HK2RuntimeInitializer
 */
public class HK2RuntimeInitializerOTF
{
	static Logger log = LoggerFactory.getLogger(HK2RuntimeInitializerOTF.class);
	
	/**
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static ServiceLocator init() throws IOException, ClassNotFoundException 
	{
		ServiceLocator locator = Hk2Looker.get();

		ServiceLocatorUtilities.addClasses(locator, LuceneDescriptionIndexer.class);
		ServiceLocatorUtilities.addClasses(locator, LuceneRefexIndexer.class);
		
		return locator;
	}
}
