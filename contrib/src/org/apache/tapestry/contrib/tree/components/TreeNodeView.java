/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation", "Tapestry" 
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" 
 *    or "Tapestry", nor may "Apache" or "Tapestry" appear in their 
 *    name, without prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE TAPESTRY CONTRIBUTOR COMMUNITY
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.tapestry.contrib.tree.components;

import org.apache.tapestry.contrib.tree.model.TreeRowObject;
import org.apache.tapestry.contrib.tree.model.TreeStateEvent;
import org.apache.tapestry.contrib.tree.model.ITreeModelSource;
import org.apache.tapestry.contrib.tree.model.ITreeRowSource;
import org.apache.tapestry.contrib.tree.model.ITreeStateListener;
import org.apache.tapestry.contrib.tree.model.ITreeStateModel;
import org.apache.tapestry.contrib.tree.simple.SimpleNodeRenderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IAsset;
import org.apache.tapestry.IBinding;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRender;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.engine.IPageLoader;
import org.apache.tapestry.event.PageDetachListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.spec.ComponentSpecification;
import org.apache.tapestry.util.ComponentAddress;

public class TreeNodeView extends BaseComponent implements PageDetachListener{
    private static final Log LOG = LogFactory.getLog(TreeNodeView.class);

    private IBinding m_objNodeRenderFactoryBinding;
    private IBinding m_objShowNodeImagesBinding;
    private IBinding m_objMakeNodeDirectBinding;
    private Boolean m_objNodeState;
    private Boolean m_objShowNodeImages;
    private Boolean m_objMakeNodeDirect;
    private INodeRenderFactory m_objNodeRenderFactory;

    private IAsset m_objOpenNodeImage;
    private IAsset m_objCloseNodeImage;

    public TreeNodeView(){
        super();
        initialize();
    }

    private void initialize(){
        m_objNodeState = null;
        m_objShowNodeImages = null;
        m_objNodeRenderFactory = null;
        m_objMakeNodeDirect = null;
    }

    public IRender getCurrentRenderer(){
        INodeRenderFactory objRenderFactory = getNodeRenderFactory();
		ITreeRowSource objTreeRowSource = getTreeRowSource();
        return objRenderFactory.getRender(objTreeRowSource.getTreeRow().getTreeNode(),
                                          getTreeModelSource(),
                                          getPage().getRequestCycle());
    }

    public Object[] getNodeContext(){
		ITreeModelSource objModelSource = getTreeModelSource();
		ComponentAddress objModelSourceAddress = new ComponentAddress(objModelSource);
		ITreeRowSource objTreeRowSource = getTreeRowSource();
		TreeRowObject objTreeRowObject = objTreeRowSource.getTreeRow();
        Object objValueUID = objTreeRowObject.getTreeNodeUID();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNodeContext objValueUID = " + objValueUID);
        }

        return new Object[] { objValueUID, new Boolean(isNodeOpen()), objModelSourceAddress };
    }

    /**
     * Called when a node in the tree is clicked by the user.
     * If the node is expanded, it will be collapsed, and vice-versa,
     * that is, the tree state model is retrieved, and it is told
     * to collapse or expand the node.
     *
     * @param cycle The Tapestry request cycle object.
     */
    public void nodeSelect(IRequestCycle cycle) {
        Object context[] = cycle.getServiceParameters();
        Object objValueUID = null;
        if (context != null && context.length > 0) {
            objValueUID = context[0];
        }
		ComponentAddress objModelSourceAddress = (ComponentAddress)context[2];
		ITreeModelSource objTreeModelSource = (ITreeModelSource) objModelSourceAddress.findComponent(cycle);
		//ITreeModelSource objTreeModelSource = getTreeModelSource();
        ITreeStateModel objStateModel = objTreeModelSource.getTreeModel().getTreeStateModel();
        boolean bState = objStateModel.isUniqueKeyExpanded(objValueUID);

        if (bState) {
            objStateModel.collapse(objValueUID);
            fireNodeCollapsed(objValueUID, objTreeModelSource);
        } else {
            objStateModel.expandPath(objValueUID);
			fireNodeExpanded(objValueUID, objTreeModelSource);
        }
    }

	private void fireNodeCollapsed(Object objValueUID, ITreeModelSource objTreeModelSource){
		deliverEvent(TreeStateEvent.NODE_COLLAPSED, objValueUID, objTreeModelSource);
	
	}

	private void fireNodeExpanded(Object objValueUID, ITreeModelSource objTreeModelSource){
		deliverEvent(TreeStateEvent.NODE_EXPANDED, objValueUID, objTreeModelSource);
	}
    
	private void deliverEvent(int nEventUID, Object objValueUID, ITreeModelSource objTreeModelSource){
		ITreeStateListener objListener = objTreeModelSource.getTreeStateListener();
		if(objListener != null){
			TreeStateEvent objEvent = new TreeStateEvent(nEventUID, objValueUID, objTreeModelSource.getTreeModel().getTreeStateModel());
			objListener.treeStateChanged(objEvent); 
		}
		
	}
    
	private void deliverEventOld(int nEventUID, Object objValueUID, ITreeStateModel objStateModel){
		IBinding objBinding = getBinding("treeStateListener");
		if(objBinding != null){
			ITreeStateListener objListener = (ITreeStateListener)objBinding.getObject();
			TreeStateEvent objEvent = new TreeStateEvent(nEventUID, objValueUID, objStateModel);
			objListener.treeStateChanged(objEvent); 
		}
		
	}
    /**
     * @see
     * org.apache.tapestry.event.PageDetachListener#pageDetached(PageEvent)
     */
    public void pageDetached(PageEvent arg0) {
        initialize();
    }

    /**
     * @see org.apache.tapestry.IComponent#finishLoad(IPageLoader,
     * ComponentSpecification)
     */
    public void finishLoad(IRequestCycle objCycle, IPageLoader arg0, ComponentSpecification arg1)
    {
        super.finishLoad(objCycle, arg0, arg1);
        getPage().addPageDetachListener(this);

        m_objOpenNodeImage = getAsset("_openNodeImage");
        m_objCloseNodeImage = getAsset("_closeNodeImage");
    }

    /**
     * Returns the nodeOpen.
     * @return boolean
     */
    public boolean isNodeOpen() {
        if(m_objNodeState == null){
			ITreeRowSource objTreeRowSource = getTreeRowSource();
			TreeRowObject objTreeRowObject = objTreeRowSource.getTreeRow();
            Object objValueUID = objTreeRowObject.getTreeNodeUID();
			ITreeModelSource objTreeModelSource = getTreeModelSource();
            ITreeStateModel objStateModel = objTreeModelSource.getTreeModel().getTreeStateModel();
            boolean bState = objStateModel.isUniqueKeyExpanded(objValueUID);
            m_objNodeState = new Boolean(bState);
        }
        return m_objNodeState.booleanValue();
    }

    /**
     * Returns the openNodeImage.
     * @return IAsset
     */
    public IAsset getNodeImage() {
        if(isNodeOpen()) {
            if (m_objOpenNodeImage == null) {
                m_objOpenNodeImage = getAsset("_openNodeImage");
            }
            return m_objOpenNodeImage;
        } else {
            if (m_objCloseNodeImage == null) {
                m_objCloseNodeImage = getAsset("_closeNodeImage");
            }
            return m_objCloseNodeImage;
        }
    }

    /**
     * Returns the closeNodeImage.
     * @return IAsset
     */
    public IAsset getCloseNodeImage() {
        return m_objCloseNodeImage;
    }

    /**
     * Returns the openNodeImage.
     * @return IAsset
     */
    public IAsset getOpenNodeImage() {
        return m_objOpenNodeImage;
    }

    /**
     * Sets the closeNodeImage.
     * @param closeNodeImage The closeNodeImage to set
     */
    public void setCloseNodeImage(IAsset closeNodeImage) {
        m_objCloseNodeImage = closeNodeImage;
    }

    /**
     * Sets the openNodeImage.
     * @param openNodeImage The openNodeImage to set
     */
    public void setOpenNodeImage(IAsset openNodeImage) {
        m_objOpenNodeImage = openNodeImage;
    }

    /**
     * @see
     * org.apache.tapestry.AbstractComponent#renderComponent(IMarkupWriter,
     * IRequestCycle)
     */
    protected void renderComponent(IMarkupWriter arg0, IRequestCycle arg1) {
        super.renderComponent(arg0, arg1);
        m_objNodeState = null;
    }

    /**
     * Returns the ShowNodeImagesBinding.
     * @return IBinding
     */
    public IBinding getShowNodeImagesBinding() {
        return m_objShowNodeImagesBinding;
    }

    /**
     * Sets the ShowNodeImagesBinding.
     * @param ShowNodeImagesBinding The ShowNodeImagesBinding to set
     */
    public void setShowNodeImagesBinding(IBinding ShowNodeImagesBinding) {
        m_objShowNodeImagesBinding = ShowNodeImagesBinding;
    }

    /**
     * Returns the ShowNodeImages.
     * @return Boolean
     */
    public Boolean isShowNodeImages() {
        if(m_objShowNodeImages == null){
            if(getNodeRenderFactoryBinding() == null){
                m_objShowNodeImages = Boolean.TRUE;
            } else {
                if(m_objShowNodeImagesBinding != null) {
                    m_objShowNodeImages = (Boolean)
                        m_objShowNodeImagesBinding.getObject();
                } else {
                    m_objShowNodeImages = Boolean.TRUE;
                }
            }
        }
        return m_objShowNodeImages;
    }

    public boolean getShowImages(){
        boolean bResult = isShowNodeImages().booleanValue();
        return bResult;
    }

    public boolean getShowWithoutImages(){
        boolean bResult = !isShowNodeImages().booleanValue();
        return bResult;
    }

    public String getOffsetStyle() {
        //return "width: " + getTreeDataView().getTreeDeep() * 15;
		ITreeRowSource objTreeRowSource = getTreeRowSource();
		TreeRowObject objTreeRowObject = objTreeRowSource.getTreeRow();
        int nTreeRowDepth = 0;
        if(objTreeRowObject != null){
			nTreeRowDepth = objTreeRowObject.getTreeRowDepth();
        }
        return "padding-left: " + nTreeRowDepth * 15+"px";
    }

    /**
     * Returns the nodeRenderFactoryBinding.
     * @return IBinding
     */
    public IBinding getNodeRenderFactoryBinding() {
        return m_objNodeRenderFactoryBinding;
    }

    /**
     * Sets the nodeRenderFactoryBinding.
     * @param nodeRenderFactoryBinding The nodeRenderFactoryBinding to set
     */
    public void setNodeRenderFactoryBinding(IBinding nodeRenderFactoryBinding) {
        m_objNodeRenderFactoryBinding = nodeRenderFactoryBinding;
    }

    public INodeRenderFactory getNodeRenderFactory() {
        if(m_objNodeRenderFactory == null){
            IBinding objBinding = getNodeRenderFactoryBinding();
            if( objBinding != null){
                m_objNodeRenderFactory = (INodeRenderFactory)objBinding.getObject();
            }else{
                m_objNodeRenderFactory = new SimpleNodeRenderFactory();
            }
        }
        return m_objNodeRenderFactory;
    }

    /**
     * Returns the makeNodeDirectBinding.
     * @return IBinding
     */
    public IBinding getMakeNodeDirectBinding() {
        return m_objMakeNodeDirectBinding;
    }

    /**
     * Sets the makeNodeDirectBinding.
     * @param makeNodeDirectBinding The makeNodeDirectBinding to set
     */
    public void setMakeNodeDirectBinding(IBinding makeNodeDirectBinding) {
        m_objMakeNodeDirectBinding = makeNodeDirectBinding;
    }

    /**
     * Returns the makeNodeDirect.
     * @return Boolean
     */
    public boolean getMakeNodeDirect() {
        if(m_objMakeNodeDirect == null){
            IBinding objBinding = getMakeNodeDirectBinding();
            if(objBinding != null){
                m_objMakeNodeDirect = (Boolean)objBinding.getObject();
            }else{
                m_objMakeNodeDirect = Boolean.TRUE;
            }
        }
        return m_objMakeNodeDirect.booleanValue();
    }
    public boolean getMakeNodeNoDirect() {
        return !getMakeNodeDirect();
    }


    public String getCleanSelectedID(){
        return getSelectedNodeID();
    }

    public String getSelectedID(){
		ITreeRowSource objTreeRowSource = getTreeRowSource();
		ITreeModelSource objTreeModelSource = getTreeModelSource();
		TreeRowObject objTreeRowObject = objTreeRowSource.getTreeRow();
        Object objNodeValueUID = objTreeRowObject.getTreeNodeUID();
        Object objSelectedNode = objTreeModelSource.getTreeModel().getTreeStateModel().getSelectedNode();
        if(objNodeValueUID.equals(objSelectedNode)) {
            return getSelectedNodeID();
        }
        return "";
    }
		
	private String getSelectedNodeID(){
		//return getTreeDataView().getTreeView().getSelectedNodeID();
		return "tree";	
	}
		
    public String getNodeStyleClass() {
		ITreeRowSource objTreeRowSource = getTreeRowSource();
		ITreeModelSource objTreeModelSource = getTreeModelSource();
		TreeRowObject objTreeRowObject = objTreeRowSource.getTreeRow();
		boolean bResult = false;
		if(objTreeRowObject != null){
	        Object objNodeValueUID = objTreeRowObject.getTreeNodeUID();
	        Object objSelectedNode = objTreeModelSource.getTreeModel().getTreeStateModel().getSelectedNode();
			bResult = objNodeValueUID.equals(objSelectedNode);
		}
        if (bResult) {
            return "selectedNodeViewClass";
        }

        return "notSelectedNodeViewClass";
    }
    
    public ITreeRowSource getTreeRowSource(){
		ITreeRowSource objSource = (ITreeRowSource)getPage().getRequestCycle().getAttribute(ITreeRowSource.TREE_ROW_SOURCE_ATTRIBUTE);
    	return objSource;
    }

	public ITreeModelSource getTreeModelSource(){
		ITreeModelSource objSource = (ITreeModelSource)getPage().getRequestCycle().getAttribute(ITreeModelSource.TREE_MODEL_SOURCE_ATTRIBUTE);
		return objSource;
	}
}
