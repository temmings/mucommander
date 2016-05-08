/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.autocomplete;

import com.mucommander.ui.autocomplete.completers.Completer;

import javax.swing.text.BadLocationException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * TextFieldCompleter is a CompleterType which suite to text-field.
 * 
 * @author Arik Hadas, based on the code of Santhosh Kumar: http://www.jroller.com/santhosh/entry/file_path_autocompletion
 */

public class TextFieldCompletion extends CompletionType {
	private boolean enableTabComplate = false;
	
    private class ShowingThreadImp extends ShowingThread {
    	public ShowingThreadImp(int delay) {
    		super(delay);
    	}

		void showAutocompletionPopup() {
	        if (autocompletedtextComp.isShowing() && autocompletedtextComp.isEnabled() && updateListData(list)){
					            
	            list.setVisibleRowCount(Math.min(list.getModel().getSize() ,VISIBLE_ROW_COUNT));
	            
	            int x = 0; 
	            try{	                
	                x = autocompletedtextComp.modelToView().x;
	            } catch(BadLocationException e){ 
	                // this should never happen!!! 
	                e.printStackTrace(); 
	                return;
	            }
	            if (autocompletedtextComp.hasFocus()) {	            	
	            	if (!isStopped) {
	            		list.ensureIndexIsVisible(0);
	            		synchronized(popup) {
		            		popup.show(autocompletedtextComp.getTextComponent(), x, autocompletedtextComp.getHeight());
		            		if(delayTime == 0){
		            			selectNextPossibleValue();
		            		}
		            		// probably because of swing's bug, sometimes the popup window looks
		            		// as a gray rectangle - repainting solves it.
		            		popup.repaint();
	            		}
	            	}
	            }	            
	        }
		}
    }

    public TextFieldCompletion(AutocompleterTextComponent comp, Completer completer){
    	this(comp, completer, false);
    }

    public TextFieldCompletion(AutocompleterTextComponent comp, Completer completer, boolean pEnableTabComplete ){
    	super(comp, completer);
    	this.enableTabComplate = pEnableTabComplete;

    	autocompletedtextComp.getDocument().addDocumentListener(documentListener);
    	if(enableTabComplate){
    		comp.getTextComponent().setFocusTraversalKeysEnabled(false);
    	}

    	autocompletedtextComp.addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent keyEvent) {        		
        		int keyCode = keyEvent.getKeyCode();
        		if( enableTabComplate && keyCode == KeyEvent.VK_TAB ){
        			if( keyEvent.isShiftDown() ){
        				keyCode = KeyEvent.VK_UP;
        			}else{
        				keyCode = KeyEvent.VK_DOWN;
        			}
        		}
        		
        		switch (keyCode) {
                case KeyEvent.VK_ENTER:
                	if (isItemSelectedAtPopupList()) {
                		hideAutocompletionPopup();
                		acceptListItem((String)list.getSelectedValue());
                		keyEvent.consume();
                	}
                	else
                		autocompletedtextComp.OnEnterPressed(keyEvent);
                	break;
                case KeyEvent.VK_ESCAPE:
                	if (isPopupListShowing()) {
                		if (autocompletedtextComp.isEnabled())
                			hideAutocompletionPopup();
                		keyEvent.consume();
                	}
                	else
                		autocompletedtextComp.OnEscPressed(keyEvent);
                	break;
                case KeyEvent.VK_UP:
                	if(autocompletedtextComp.isEnabled() && popup.isVisible()) {
                		selectPreviousPossibleValue();
                		keyEvent.consume();
                	}
                	break;
                case KeyEvent.VK_SPACE:
                	// The combination of cntrl+space makes open the auto-complete popup without delay.
                	if (keyEvent.isControlDown()) {
                		if (!popup.isVisible()) {
                    		autocompletedtextComp.moveCarentToEndOfText();
                    		createNewShowingThread(0);
                    	}
                	}
                	break;
                case KeyEvent.VK_DOWN:
                	if(autocompletedtextComp.isEnabled()){ 
                        if(popup.isVisible()) {
                            selectNextPossibleValue();
                            keyEvent.consume();
                        }
                        else {                	
                        	autocompletedtextComp.moveCarentToEndOfText();
                    		createNewShowingThread(0);
                        }
                    }
                	break;
                case KeyEvent.VK_PAGE_DOWN:
                	if(autocompletedtextComp.isEnabled() && isPopupListShowing()) {
                		selectNextPage();
                		keyEvent.consume();
                	}
                	break;
                case KeyEvent.VK_PAGE_UP:
                	if(autocompletedtextComp.isEnabled() && isItemSelectedAtPopupList()) {
                		selectPreviousPage();
                		keyEvent.consume();
                	}
                	break;
                case KeyEvent.VK_HOME:
                	if(autocompletedtextComp.isEnabled() && isPopupListShowing()) {
                		selectFirstValue();
                		keyEvent.consume();
                	}
                	break;
                case KeyEvent.VK_END:
                	if(autocompletedtextComp.isEnabled() && isPopupListShowing()) {
                		selectLastValue();
                		keyEvent.consume();
                	}
                	break;
                case KeyEvent.VK_LEFT:
                	hideAutocompletionPopup();
                	break;
                default:
                }
            }
        });
    }
            
    protected void startNewShowingThread(int delay) {    	    	
    	(showingThread = new ShowingThreadImp(delay)).start();
    }
        
    protected void hideAutocompletionPopup() {
		synchronized (popup) {
			if (popup.isVisible())
        		popup.setVisible(false);
		}
	}
}