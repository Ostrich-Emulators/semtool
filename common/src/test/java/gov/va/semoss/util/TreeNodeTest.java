package gov.va.semoss.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TreeNodeTest {


	@Test
	public void testTreeNodeT() {
		TreeNode<String> node = new TreeNode<String>("Some Node");
		assertNotNull(node);
	}

	@Test
	public void testTreeNode() {
		TreeNode<String> node = new TreeNode<String>();
		assertNotNull(node);
	}

	@Test
	public void testGetChildNodes() {
		TreeNode<String> parent = new TreeNode<String>("Parent");
		parent.addChild("Child");
		List<TreeNode<String>> children = parent.getChildNodes();
		assertTrue(children.get(0).getNode().equals("Child"));
	}


	public void testGetChildren() {
		TreeNode<String> parent = new TreeNode<String>("Parent");
		parent.addChild("Child");
		List<String> children = parent.getChildren();
		assertTrue(children.contains("Child"));
	}

	@Test
	public void testSetChildren() {
		TreeNode<String> parent = new TreeNode<String>("Parent");
		ArrayList<String> children = new ArrayList<String>();
		children.add("Child1");
		children.add("Child2");
		parent.setChildren(children);
		List<String> returnedChildren = parent.getChildren();
		assertTrue(returnedChildren.contains("Child1"));
		assertTrue(returnedChildren.contains("Child2"));
	}

	@Test
	public void testAddChild() {
		TreeNode<String> parent = new TreeNode<String>("Parent");
		TreeNode<String> child = parent.addChild("Child");
		String value = child.getNode();
		assertEquals("Child", value);
	}

	@Test
	public void testRemoveChild() {
		TreeNode<String> parent = new TreeNode<String>("Parent");
		parent.addChild("Child");
		parent.removeChild("Child");
		List<String> children = parent.getChildren();
		assertEquals(children.size(), 0);
	}

	@Test
	public void testSetGetNode() {
		TreeNode<String> node = new TreeNode<String>("Parent");
		node.setNode("A different parent");
		String value = node.getNode();
		assertEquals(value, "A different parent");
	}
}