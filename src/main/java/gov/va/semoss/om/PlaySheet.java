package gov.va.semoss.om;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**    Holds playsheet data for populating a collection for the Insight Manager's "Display with" dropdown.
 * 
 * @author Thomas
 *
 */
public class PlaySheet {
    private String strIcon;
    private String strViewClass;
    private String strLabel;
    private String strDescription;
    
    public PlaySheet(){   	
    }
    
    public PlaySheet(String strIcon, String strViewClass, String strLabel, String strDescription){   	
    	this.strIcon = strIcon;
    	this.strViewClass = strViewClass;
    	this.strLabel = strLabel;
    	this.strDescription = strDescription;
    }
    //Playsheet icon:
    public String getIcon(){
    	return this.strIcon;
    }
    public void setIcon(String strIcon){
    	this.strIcon = strIcon;
    }
    //Playsheet class-path:
    public String getViewClass(){
    	return this.strViewClass;
    }
    public void setViewClass(String strViewClass){
    	this.strViewClass = strViewClass;
    }
    //Playsheet label (for dropdown):
    public String getLabel(){
    	return this.strLabel;
    }
    public void setLabel(String strLabel){
    	this.strLabel = strLabel;
    }
    //Playsheet description:
    public String getDescription(){
    	return this.strDescription;
    }
    public void setDescription(String strDescription){
    	this.strDescription = strDescription;
    }

    /**   Populates PlaySheet instance variables from the results of a database fetch.
     * 
     * @param resultSet -- (BindingSet) A row of data corresponding to one PlaySheet.
     */
    public void setFromResultSet( BindingSet resultSet ) {
		Value iconValue = resultSet.getValue("icon");
		if(iconValue != null){
			this.strIcon = iconValue.stringValue();
		}
		Value viewClassValue = resultSet.getValue("viewClass");
		if(viewClassValue != null){
			this.strViewClass = viewClassValue.stringValue();
		}
		Value labelValue = resultSet.getValue("label");
		if(labelValue != null){
			this.strLabel = labelValue.stringValue();
		}
		Value descriptionValue = resultSet.getValue("description");
		if(descriptionValue != null){
			this.strDescription = descriptionValue.stringValue();
		}
	}

	@Override
	public String toString() {
		return "PlaySheet [icon: " + this.strIcon
				+ ", viewClass: " + this.strViewClass
				+ ", label: " + this.strLabel
				+ ", description: " + this.strDescription + "]";
	}
   
}//End PlaySheet class.
