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
package gov.va.isaac.mdht.otf.ui.properties;

import gov.va.isaac.mdht.otf.refset.RefsetAttributeType;
import gov.va.isaac.mdht.otf.refset.RefsetMember;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Input dialog where valueString is limited to Refex primitive types.  Based on Eclipse InputDialog.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class PrimitiveInputDialog extends Dialog {

    /**
     * The title of the dialog.
     */
    private String title;

    /**
     * The message to display, or <code>null</code> if none.
     */
    private String message;

    /**
     * Message label widget.
     */
    private Text messageText;
    
    /**
     * The input valueString; the empty string by default.
     */
    private String valueString = "";//$NON-NLS-1$

    /**
     * The input validator, or <code>null</code> if none.
     */
    private IInputValidator validator;

    /**
     * Ok button widget.
     */
    private Button okButton;

    /**
     * Input text widget.
     */
    private Text text;

    /**
     * Error message label widget.
     */
    private Text errorMessageText;
    
    /**
     * Error message string.
     */
    private String errorMessage;

	private CCombo valueKindButton = null;
	
	private RefsetAttributeType valueKind = null;

    /**
     * Creates an input dialog with OK and Cancel buttons. Note that the dialog
     * will have no visual representation (no widgets) until it is told to open.
     * <p>
     * Note that the <code>open</code> method blocks for input dialogs.
     * </p>
     * 
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level
     *            shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message, or <code>null</code> if none
     * @param initialValue
     *            the initial input valueString, or <code>null</code> if none
     *            (equivalent to the empty string)
     * @param validator
     *            an input validator, or <code>null</code> if none
     */
    public PrimitiveInputDialog(Shell parentShell, String dialogTitle,
            String dialogMessage, String initialValue, IInputValidator validator) {
        super(parentShell);
        this.title = dialogTitle;
        message = dialogMessage;
        
        if (initialValue == null) {
			valueString = "";//$NON-NLS-1$
		} else {
			valueString = initialValue;
		}
        
        if (validator != null) {
        	this.validator = validator;
        }
        else {
        	this.validator = createValidator();
        }
    }
    
    protected IInputValidator createValidator() {
		return new IInputValidator() {  
			@Override
			public String isValid(String newText) {
				try {
					if (newText.length() > 0) {
						if (RefsetAttributeType.String == getValueKind()) {
							new String(newText);
						}
						else if (RefsetAttributeType.Integer == getValueKind()) {
							new Integer(newText);
						}
						else if (RefsetAttributeType.Long == getValueKind()) {
							new Long(newText);
						}
						else if (RefsetAttributeType.Boolean == getValueKind()) {
							new Boolean(newText);
						}
						else if (RefsetAttributeType.Float == getValueKind()) {
							new Float(newText);
						}
					}
				}
				catch (Exception e) {
					return "Invalid " + getValueKind().name() + " value.";
				}
				
				return null;
			}
		};
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            valueString = text.getText();
        } else {
            valueString = null;
        }
        super.buttonPressed(buttonId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
			shell.setText(title);
		}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        //do this here because setting the text will set enablement on the ok
        // button
        text.setFocus();
        if (valueString != null) {
            text.setText(valueString);
            text.selectAll();
        }
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        // create composite
        Composite composite = (Composite) super.createDialogArea(parent);
        // create message
        messageText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
        messageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        messageText.setBackground(messageText.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        // Set the error message text
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
        setMessage(message);
        
        text = new Text(composite, getInputTextStyle());
        text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        // create valueString type combo box
        Label label = new Label(composite, SWT.NONE);
        label.setText("Type ");
        label.setFont(parent.getFont());

        int itemIndex = -1;
        int selectIndex = 0;
		valueKindButton = new CCombo(composite, SWT.BORDER | SWT.READ_ONLY);
		for (RefsetAttributeType attrType : RefsetMember.getPrimitiveTypes()) {
			itemIndex++;
			valueKindButton.add(attrType.name());
			if (valueKind == attrType) {
				selectIndex = itemIndex;
			}
		}
		valueKindButton.select(selectIndex);
		
		valueKindButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				widgetDefaultSelected(event);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				try {
					String selectedKind = valueKindButton.getText();
					RefsetAttributeType type = RefsetAttributeType.valueOf(selectedKind);
					setValueKind(type);
				}
				catch (Exception e) {
					// should not occur for selected type
				}
			}
		});
        
        errorMessageText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
        errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        errorMessageText.setBackground(errorMessageText.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        // Set the error message text
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
        setErrorMessage(errorMessage);

        applyDialogFont(composite);
        return composite;
    }

    /**
     * Returns the ok button.
     * 
     * @return the ok button
     */
    protected Button getOkButton() {
        return okButton;
    }

    /**
     * Returns the text area.
     * 
     * @return the text area
     */
    protected Text getText() {
        return text;
    }

    /**
     * Sets or clears the message.
     * 
     * @param message
     *            the message, or <code>null</code> to clear
     */
    public void setMessage(String newMessage) {
    	this.message = newMessage;
    	if (messageText != null && !messageText.isDisposed()) {
    		messageText.setText(newMessage == null ? " \n " : newMessage); //$NON-NLS-1$
    		messageText.getParent().update();
    	}
    }
    
    /**
     * Returns the validator.
     * 
     * @return the validator
     */
    protected IInputValidator getValidator() {
        return validator;
    }

    /**
     * Returns the string typed into this input dialog.
     * 
     * @return the input string
     */
    public String getStringValue() {
        return valueString;
    }
    
    public void setValueString(String value) {
    	this.valueString = value;
    }

    /**
     * Returns the primitive Object typed into this input dialog.
     * 
     * @return object from the input string
     */
    public Object getValue() {
    	Object objectValue = null;
    	
		if (RefsetAttributeType.String == valueKind) {
			objectValue = new String(valueString);
		}
		else if (RefsetAttributeType.Integer == valueKind) {
			objectValue = new Integer(valueString);
		}
		else if (RefsetAttributeType.Long == valueKind) {
			objectValue = new Long(valueString);
		}
		else if (RefsetAttributeType.Boolean == valueKind) {
			objectValue = new Boolean(valueString);
		}
		else if (RefsetAttributeType.Float == valueKind) {
			objectValue = new Float(valueString);
		}
		
        return objectValue;
    }

    /**
     * Validates the input.
     * <p>
     * The default implementation of this framework method delegates the request
     * to the supplied input validator object; if it finds the input invalid,
     * the error message is displayed in the dialog's message line. This hook
     * method is called whenever the text changes in the input field.
     * </p>
     */
    protected void validateInput() {
        String errorMessage = null;
        if (validator != null) {
            errorMessage = validator.isValid(text.getText());
        }
        // Bug 16256: important not to treat "" (blank error) the same as null
        // (no error)
        setErrorMessage(errorMessage);
    }

    /**
     * Sets or clears the error message.
     * If not <code>null</code>, the OK button is disabled.
     * 
     * @param errorMessage
     *            the error message, or <code>null</code> to clear
     * @since 3.0
     */
    public void setErrorMessage(String errorMessage) {
    	this.errorMessage = errorMessage;
    	if (errorMessageText != null && !errorMessageText.isDisposed()) {
    		errorMessageText.setText(errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
    		// Disable the error message text control if there is no error, or
    		// no error text (empty or whitespace only).  Hide it also to avoid
    		// color change.
    		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
    		boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
    		errorMessageText.setEnabled(hasError);
    		errorMessageText.setVisible(hasError);
    		errorMessageText.getParent().update();
    		// Access the ok button by id, in case clients have overridden button creation.
    		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
    		Control button = getButton(IDialogConstants.OK_ID);
    		if (button != null) {
    			button.setEnabled(errorMessage == null);
    		}
    	}
    }
    
	/**
	 * Returns the style bits that should be used for the input text field.
	 * Defaults to a single line entry. Subclasses may override.
	 * 
	 * @return the integer style bits that should be used when creating the
	 *         input text
	 * 
	 * @since 3.4
	 */
	protected int getInputTextStyle() {
		return SWT.SINGLE | SWT.BORDER;
	}
	
	public RefsetAttributeType getValueKind() {
		return valueKind;
	}
	
	public void setValueKind(RefsetAttributeType type) {
		valueKind = type;
		setMessage("Enter " + valueKind.name() + " value");
	}
}
