package com.ostrichemulators.semtool.ui.components.semanticexplorer;

import javax.swing.tree.DefaultMutableTreeNode;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

	public class URITreeNode extends DefaultMutableTreeNode implements Comparable<URITreeNode> {
		private static final long serialVersionUID = 2744084727914202969L;
		private Value label;
		private boolean useLabels = false;

		public URITreeNode(URI userObject, boolean useLabels) {
	        super(userObject, true);
	        
	        this.useLabels = useLabels;
	    }
	    
		public URITreeNode(Value userObject, Value label, boolean useLabels) {
	        super(userObject, true);
	        
	        this.useLabels = useLabels;
	        this.label = label;
	    }
	    
	    @Override
		public String toString() {
	        if (userObject == null) {
	            return "";
	        } else if ( useLabels && (label != null)) {
        		return label.stringValue();
	        } else if (userObject instanceof URI) {
	        	URI thisURI = (URI) userObject;
	            return thisURI.getLocalName();
	        } else if (userObject instanceof Value) {
	        	Value thisValue = (Value) userObject;
	            return thisValue.stringValue();
	        } else {
	        	return "";
	        }
		}

		@Override
		public int compareTo(URITreeNode otherNode) {
			return toString().compareTo(otherNode.toString());
		}
	}