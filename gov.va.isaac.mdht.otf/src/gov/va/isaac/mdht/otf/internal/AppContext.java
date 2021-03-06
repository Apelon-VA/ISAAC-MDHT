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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.mdht.otf.internal;

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.glassfish.hk2.api.ServiceLocator;

/**
 * AppContext
 *
 * Provides convenience methods for retrieving implementations of various interfaces
 * from the HK2 dependency management system.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AppContext
{
	private static ServiceLocator serviceLocator_;

	/**
	 * Call this once (and only once) to initialize the ISAAC HK2 service Locator.
	 * After this is called, you can access the service locator via convenience methods here,
	 * or via a call directly to HK2:
	 * {@code
	 *     Hk2Looker.get()
	 * }
	 * @throws IOException
	 * @throws ClassNotFoundException
	 *
	 */
	public synchronized static void setup() throws ClassNotFoundException, IOException
	{
		if (serviceLocator_ != null)
		{
			throw new RuntimeException("Only one service locator should be set");
		}
		serviceLocator_ = HK2RuntimeInitializerOTF.init();
	}

	public static ServiceLocator getServiceLocator()
	{
		return serviceLocator_;
	}

	public static <T> T getService(Class<T> contractOrService, Annotation... qualifiers)
	{
		return serviceLocator_.getService(contractOrService, qualifiers);
	}
	
	public static <T> T getService(Class<T> contractOrService, String name, Annotation... qualifiers)
	{
		return serviceLocator_.getService(contractOrService, name, qualifiers);
	}

}
