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
package gov.va.isaac.mdht.otf.ui.views;

import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.services.TerminologyStoreService;
import gov.va.isaac.mdht.otf.ui.dialogs.ConceptListDialog;
import gov.va.isaac.mdht.otf.ui.dialogs.ConceptSearchDialog;
import gov.va.isaac.mdht.otf.ui.internal.Activator;
import gov.va.isaac.mdht.otf.ui.providers.ComponentLabelProvider;
import gov.va.isaac.mdht.otf.ui.providers.ConceptContentProvider;
import gov.va.isaac.mdht.otf.ui.providers.ConceptItem;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class TaxonomyView extends ViewPart
	implements ITabbedPropertySheetPageContributor, ISetSelectionTarget {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "gov.va.isaac.mdht.otf.ui.views.TaxonomyView";

	/** This is the property sheet page. */
	protected TabbedPropertySheetPage propertySheetPage;

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action indexRepository;
	private Action searchConcepts;
	private Action doubleClickAction;
	
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public TaxonomyView() {
	}
	
	public TreeViewer getViewer() {
		return viewer;
	}

	public String getContributorId() {
		return "org.ihtsdo.otf.ui.properties"; //$NON-NLS-1$
	}

	/**
	 * This is how the framework determines which interfaces we implement.
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class key) {
        if (key == IPropertySheetPage.class) {
			propertySheetPage = new TabbedPropertySheetPage(this);
			return propertySheetPage;
        }
		
		return super.getAdapter(key);
	}

	@Override
	public void dispose() {
		if (propertySheetPage != null) {
			propertySheetPage.dispose();
		}

		super.dispose();
	}

	/**
	 * Set the selection to the tree viewer, and expand nodes if necessary.
	 * 
	 * @see org.eclipse.ui.part.ISetSelectionTarget#selectReveal(org.eclipse.jface.viewers.ISelection)
	 */
	public void selectReveal(ISelection selection) {
		// this code copied from CommonNavigator
		if (viewer != null) {
			ConceptVersionBI concept = null;
			
			if (selection instanceof IStructuredSelection) {
				Object[] newSelection = ((IStructuredSelection) selection).toArray();
				Object[] expandedElements = viewer.getExpandedElements();
				
				// get all parents of selected concept
				if (newSelection.length == 1 && newSelection[0] instanceof ConceptVersionBI) {
					ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
					concept = (ConceptVersionBI) newSelection[0];
					List<ConceptVersionBI> parents = queryService.getParentPath(concept);
					int numParents = parents.size();
					expandedElements = new Object[numParents];
					for (int i=numParents; i>0; i--) {
						expandedElements[numParents-i] = parents.get(i-1);
					}
				}
				
				Object[] newExpandedElements = new Object[newSelection.length + expandedElements.length];
				System.arraycopy(expandedElements, 0, newExpandedElements, 0, expandedElements.length);
				System.arraycopy(newSelection, 0, newExpandedElements, expandedElements.length, newSelection.length);
				
				// create ConceptItem instances for tree items
				for (int i = 0; i < newExpandedElements.length; i++) {
					newExpandedElements[i] = new ConceptItem((ConceptVersionBI)newExpandedElements[i], null);
				}

				// refresh is required when new content was added by an action
//				viewer.refresh();
				viewer.setExpandedElements(newExpandedElements);
			}
			
			if (concept != null) {
				ISelection selectedItem = new StructuredSelection(new ConceptItem(concept, null));
				viewer.setSelection(selectedItem, true);
			}
		}
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ConceptContentProvider());
		viewer.setLabelProvider(new ComponentLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		viewer.setAutoExpandLevel(1);

		getViewSite().setSelectionProvider(viewer);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "gov.va.isaac.mdht.otf.ui.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TaxonomyView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
//		manager.add(indexRepository);
//		manager.add(new Separator());
		manager.add(searchConcepts);
	}

	private void fillContextMenu(IMenuManager manager) {
//		manager.add(indexRepository);
//		manager.add(searchConcepts);
//		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.FIND_EXT));
		manager.add(new Separator(IWorkbenchActionConstants.M_EDIT));
		manager.add(new Separator(IWorkbenchActionConstants.PRINT_EXT));
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
//		manager.add(indexRepository);
		manager.add(searchConcepts);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		indexRepository = new Action() {
			public void run() {
				TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
				try {
					storeService.index();
				} catch (IOException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot index repository", e), 
							StatusManager.SHOW | StatusManager.LOG);
				}
			}
		};
		indexRepository.setText("Index");
		indexRepository.setToolTipText("Index Repository");
		indexRepository.setImageDescriptor(Activator.getImageDescriptor("icons/eview16/index.gif"));
		
		searchConcepts = new Action() {
			public void run() {
				try {
					ConceptSearchDialog searchDialog = new ConceptSearchDialog(getSite().getShell());
					searchDialog.open();
					List<ConceptVersionBI> results = searchDialog.getResults();
					ConceptVersionBI selectedResult = null;
					
					if (results.size() == 1) {
						selectedResult = results.get(0);
					}
					else if (results.size() > 1) {
						ConceptListDialog listDialog = new ConceptListDialog(getSite().getShell());
						listDialog.setConceptList(results);
						listDialog.open();
						Object[] selectionResult = listDialog.getResult();
						if (selectionResult != null && selectionResult.length == 1) {
							selectedResult = (ConceptVersionBI)selectionResult[0];
						}
					}

					if (selectedResult != null) {
						IViewPart taxonomy = getSite().getWorkbenchWindow().getActivePage().showView(TaxonomyView.ID);
						IStructuredSelection selection = new StructuredSelection(selectedResult);
						((ISetSelectionTarget)taxonomy).selectReveal(selection);
					}

				} catch (Exception e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error in Query Services", e), 
							StatusManager.SHOW | StatusManager.LOG);
				}
			}
		};
		searchConcepts.setText("Search");
		searchConcepts.setToolTipText("Find Concepts");
		searchConcepts.setImageDescriptor(Activator.getImageDescriptor("icons/eview16/search.gif"));
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Taxonomy View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}