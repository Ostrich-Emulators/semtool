/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.om;

import java.util.ArrayList;
import java.util.Objects;

import org.openrdf.model.URI;

/**
 * A class for managing a collection of {@link Insight}.
 *
 * @author Thomas
 */
public class Perspective {

	private URI id;
	private String label;
	private String description;
	private ArrayList<Insight> arylInsights;

	//Constructors:
	//-------------
	public Perspective() {

	}

	public Perspective( URI id ) {
		this.id = id;
	}

	public Perspective( String label ) {
		this.label = label;
	}

	public Perspective( URI id, String label, String description ) {
		this( id );
		this.label = label;
		this.description = description;
	}

	//URI getter/setter:
	//------------------
	public URI getUri() {
		return id;
	}
	public void setUri( URI u ) {
		id = u;
	}

	//Label getter/setter:
	//--------------------
	public String getLabel() {
		return label;
	}
	public void setLabel( String label ) {
		this.label = label;
	}

	//Description getter/setter:
	//--------------------------
	public String getDescription() {
        if(description == null){
      	   description = "";
        }
		return description;
	}
	public void setDescription( String description ) {
		this.description = description;
	}

	//Insights getter/setter:
	//-----------------------
	public ArrayList<Insight> getInsights(){
		return arylInsights;
	}
	public void setInsights(ArrayList<Insight> arylInsights){
		this.arylInsights = arylInsights;
	}
	
	@Override
	public String toString() {
		return label;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 23 * hash + Objects.hashCode( this.id );
		return hash;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		final Perspective other = (Perspective) obj;
		if ( !Objects.equals( this.id, other.id ) ) {
			return false;
		}
		return true;
	}
}
