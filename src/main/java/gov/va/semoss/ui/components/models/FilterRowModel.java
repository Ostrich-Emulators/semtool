package gov.va.semoss.ui.components.models;

import org.openrdf.model.URI;

public class FilterRowModel {
	private boolean show, isHeader, isEdge;
	private URI type, instance;

	public FilterRowModel( boolean show, boolean isHeader, boolean isEdge, URI type, URI instance ) {
		this.show = show;
		this.isHeader = isHeader;
		this.isEdge = isEdge;
		this.type = type;
		this.instance = instance;
	}

	public boolean show() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public boolean isHeader() {
		return isHeader;
	}

	public void setHeader(boolean isHeader) {
		this.isHeader = isHeader;
	}

	public boolean isEdge() {
		return isEdge;
	}

	public void setIsEdge(boolean isEdge) {
		this.isEdge = isEdge;
	}

	public URI getType() {
		return type;
	}

	public void setType(URI type) {
		this.type = type;
	}

	public URI getInstance() {
		return instance;
	}

	public void setInstance(URI instance) {
		this.instance = instance;
	}
}