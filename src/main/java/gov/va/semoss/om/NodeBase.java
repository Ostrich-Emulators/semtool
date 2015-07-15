/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.om;

import java.awt.Shape;

/**
 *
 * @author ryan
 */
public interface NodeBase extends NodeEdgeBase {

	public Shape getShape();

	public void setShape( Shape s );
}
