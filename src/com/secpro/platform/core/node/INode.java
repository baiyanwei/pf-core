package com.secpro.platform.core.node;

/**
 * @author baiyanwei
 * Oct 22, 2013
 *
 * define that node should have behavior.
 *
 */
public interface INode {

	/**
	 * register node to console.
	 */
	public void registerNode();

	/**
	 * unregister node to console.
	 */
	public void unregisterNode();

	/**
	 * get region of node
	 * 
	 * @return
	 */
	public String getNodeRegion();

	/**
	 * set region to node.
	 * 
	 * @param region
	 * @return
	 */
	public String setNodeRegion(String region);
}
