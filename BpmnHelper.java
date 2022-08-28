package com.bpmnxmlgenerator.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.bpmnxmlgenerator.dto.ProcessTableDto;
import com.bpmnxmlgenerator.dto.StepConnectorDto;
import com.bpmnxmlgenerator.enums.FlowType;
import com.bpmnxmlgenerator.enums.ShapeType;
import com.bpmnxmlgenerator.infra.constant.XMLConstant;
import com.bpmnxmlgenerator.infra.helper.PropertiesHelper;
import com.bpmnxmlgenerator.model.Bound;
import com.bpmnxmlgenerator.model.BpmnData;
import com.bpmnxmlgenerator.model.Participant;
import com.bpmnxmlgenerator.utils.ValidationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class BpmnHelper {

	private static Logger logger = LogManager.getLogger(BpmnHelper.class);

	private ObjectMapper objectMapper = new ObjectMapper();

	private static final String GeneratedFileExtension = "bpmn";
	
	private static final String DEFAULT_LANE_ID = "DEFAULT_LANE";
	
	@Autowired
	private Environment env;

	/**
	 * 
	 * @param key
	 * @param targetType
	 * @return
	 */
	private <T> T getProperty(String key, Class<T> targetType) {
		return env.getProperty(key, targetType);
	}

	/**
	 * Used for create new ObjectNode.
	 * 
	 * @return
	 */
	private ObjectNode getObjectNode() {
		return objectMapper.createObjectNode();
	}

	/**
	 * Used for create new ArrayNode.
	 * 
	 * @return
	 */
	private ArrayNode getArrayNode() {
		return objectMapper.createArrayNode();
	}

	/**
	 *
	 * @return
	 */
	private String generateUUId() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	/**
	 * Used for generate BPMN XML file from list of process.
	 * 
	 * @param processes
	 * @param fileName
	 * @param folderName
	 * @return
	 * @throws IOException
	 */
	public String generateXMLFile(List<ProcessTableDto> processes, String fileName, String folderName) throws IOException {
		logger.info("--------generateXMLFile Executed -------");
		String json = convertToJson(processes);
		return generateBPMNFile(json, fileName,folderName);
	}

	/**
	 * Used for generate BPMN XML file from list of process.
	 * 
	 * @param processes
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public String generateXMLFileWithLane(List<ProcessTableDto> processes, String fileName, String folderName) throws IOException {
//		String json = convertToJsonFromLane(processes, fileName);
//		logger.info("JSON :  " + json);
//		return generateBPMNFile(json, fileName,folderName);
		try {
			int a = 10;
			int b = 0;
			
			int c = a/b;
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	/**
	 *
	 * @param model
	 * @return
	 */
	private ObjectNode createBpmnModelObjectNode(BpmnData model, Map<String, BpmnData> bpmnDataMap) {

		ObjectNode editorNode = getObjectNode();
		editorNode.put(XMLConstant.RESOURCE_ID, model.getResourceId());

		// Set BPMN Properties.
		ObjectNode propertiesNode = getObjectNode();
		setBpmnProperties(propertiesNode, model);
		editorNode.set(XMLConstant.PROPERTIES, propertiesNode);

		// Set stencil Id
		ObjectNode childStencilNode = getObjectNode();
		editorNode.set(XMLConstant.STENCIL, childStencilNode);
		childStencilNode.put(XMLConstant.ID, model.getShapeType().getStencilId());

		// Set Child Shapes
		editorNode.set(XMLConstant.CHILD_SHAPES, getArrayNode());

		// Set upperLeft & Lower right Boundaries for BPMN Model.
		setBoundProperties(model.getBound(), editorNode);

		// Set Docker Properties.
		setDockerProperties(editorNode, model, null, bpmnDataMap);

		// Set Outgoing Properties.
		ArrayNode outgoingNode = getArrayNode();
		editorNode.set(XMLConstant.OUTGOING, outgoingNode);
		if ((model.getOutgoing() != null) && !model.getOutgoing().isEmpty()) {
			for (String outGoindStepId : model.getOutgoing()) {
				ObjectNode outgoingChildNode = getObjectNode();
				outgoingChildNode.put(XMLConstant.RESOURCE_ID, bpmnDataMap.get(outGoindStepId).getResourceId());
				outgoingNode.add(outgoingChildNode);
			}
		}

		return editorNode;
	}

	/**
	 * Used for set Bound Properties of BpmnData.
	 * 
	 * @param model
	 * @param editorNode
	 */
	private void setBoundProperties(Bound model, ObjectNode editorNode) {
		ObjectNode boundsNode = getObjectNode();
		editorNode.set(XMLConstant.BOUNDS, boundsNode);

		ObjectNode lowerRightNode = getObjectNode();
		boundsNode.set(XMLConstant.LOWER_RIGHT, lowerRightNode);
		lowerRightNode.put(XMLConstant.XBOUND, model.getLowerRightX());
		lowerRightNode.put(XMLConstant.YBOUND, model.getLowerRightY());

		ObjectNode upperLeftNode = getObjectNode();
		boundsNode.set(XMLConstant.UPPER_LEFT, upperLeftNode);
		upperLeftNode.put(XMLConstant.XBOUND, model.getUpperLeftX());
		upperLeftNode.put(XMLConstant.YBOUND, model.getUpperLeftY());
	}

	/**
	 * Used for set docker properties for BpmnData.
	 * 
	 * @param editorNode
	 * @param model
	 * @param bpmnDataMap
	 */
	private void setDockerProperties(ObjectNode editorNode, BpmnData model, Bound currentPoolBound,
			Map<String, BpmnData> bpmnDataMap) {
		ArrayNode dockerNode = getArrayNode();
		editorNode.set(XMLConstant.DOCKERS, dockerNode);
		if (ShapeType.SEQUENCE_FLOW.equals(model.getShapeType()) || ShapeType.ASSOCIATION.equals(model.getShapeType())) {

			BpmnData fromModel = bpmnDataMap.get(model.getFromStepId());
			BpmnData toModel = bpmnDataMap.get(model.getOutgoing().get(0));

			// Set Sequence Flow Type for Model.
			setSequenceFlowType(model, fromModel, toModel,bpmnDataMap);

			Bound sourceBound = fromModel.getBound();
			Bound destinBound = toModel.getBound();

			int sourceXBound = ((sourceBound.getLowerRightX() - sourceBound.getUpperLeftX()) / 2);
			int sourceYBound = ((sourceBound.getLowerRightY() - sourceBound.getUpperLeftY()) / 2);
			int destinXBound = ((destinBound.getLowerRightX() - destinBound.getUpperLeftX()) / 2);
			if(ShapeType.TEXT_ANNOTATION.equals(toModel.getShapeType())) {
				destinXBound = destinXBound - (destinXBound / 4) ;
			}
			int destinYBound = ((destinBound.getLowerRightY() - destinBound.getUpperLeftY()) / 2);

			List<ObjectNode> childNodes = null;
			if (ShapeType.SEQUENCE_FLOW.equals(model.getShapeType()) && !(FlowType.STRAIGHT.equals(model.getFlowType()))) {

				childNodes = new LinkedList<>();
				ObjectNode childObjectNodeSource = getObjectNode();
				ObjectNode childObjectNodeDestin = getObjectNode();
				
                int sourceX =(sourceBound.getLowerRightX() + sourceBound.getUpperLeftX()) / 2; 
        		int destinX = (destinBound.getLowerRightX() + destinBound.getUpperLeftX()) / 2;
				// For Lane Set X Point of docker.
				if (!ValidationUtils.getInstance().isEmptyString(fromModel.getLaneId())
						&& !ValidationUtils.getInstance().isEmptyString(toModel.getLaneId())) {
					int laneStartWidth = getProperty(PropertiesHelper.BPMN_LANE_UPPERLEFTX, Integer.class);
					int poolStartWidth = getProperty(PropertiesHelper.BPMN_POOL_UPPERLEFTX, Integer.class);
					int upperLeftXforAdd = laneStartWidth + poolStartWidth;
					sourceX = (sourceX + upperLeftXforAdd);
					destinX = (destinX + upperLeftXforAdd);
					if(ShapeType.SEQUENCE_FLOW.equals(model.getShapeType()) && (FlowType.UPPER_FORWARD.equals(model.getFlowType()) || FlowType.LOWER_FORWARD.equals(model.getFlowType()))) {
						if(FlowType.LOWER_FORWARD.equals(model.getFlowType()) || !ValidationUtils.getInstance().isEmptyString(fromModel.getTextAnnotation())) {
							int midSourcePointX = (sourceBound.getLowerRightX() - sourceBound.getUpperLeftX()) / 2 ;
							sourceX = sourceX + midSourcePointX;
						}
						if(FlowType.LOWER_FORWARD.equals(model.getFlowType()) || !ValidationUtils.getInstance().isEmptyString(toModel.getTextAnnotation())) {
					        int midDestinationPointX = (destinBound.getLowerRightX() - destinBound.getUpperLeftX()) / 2;
							destinX = destinX - midDestinationPointX;
						}
					}
				}

				childObjectNodeSource.put(XMLConstant.XBOUND, sourceX);
				childObjectNodeDestin.put(XMLConstant.XBOUND, destinX);

				// set docker y point for sequential_Flow.
				int dockerYPoint = getDockerYPoint(model.getIndex(), model.getFlowType(), fromModel, toModel,true,bpmnDataMap);

				if (currentPoolBound != null) {
					dockerYPoint = dockerYPoint + currentPoolBound.getUpperLeftY();
				}
				childObjectNodeSource.put(XMLConstant.YBOUND, dockerYPoint);
				childObjectNodeDestin.put(XMLConstant.YBOUND, dockerYPoint);

				childNodes.add(childObjectNodeSource);
				childNodes.add(childObjectNodeDestin);

			}

			ObjectNode objectNodeSource = getObjectNode();
			objectNodeSource.put(XMLConstant.XBOUND, sourceXBound);
			objectNodeSource.put(XMLConstant.YBOUND, sourceYBound);
			dockerNode.add(objectNodeSource);

			
			if (!FlowType.STRAIGHT.equals(model.getFlowType()) && childNodes != null) {
				for (ObjectNode node : childNodes) {
					dockerNode.add(node);
				}
			}

			ObjectNode objectNodeDestin = getObjectNode();
			objectNodeDestin.put(XMLConstant.XBOUND, destinXBound);
			objectNodeDestin.put(XMLConstant.YBOUND, destinYBound);
			dockerNode.add(objectNodeDestin);

			ObjectNode targetNode = getObjectNode();
			targetNode.put(XMLConstant.RESOURCE_ID, bpmnDataMap.get(model.getOutgoing().get(0)).getResourceId());
			editorNode.set(XMLConstant.TARGET, targetNode);

		}
	}
	
	
	/**
	 * 
	 * @param editorNode
	 * @param sourceModel
	 * @param toModel
	 */
	private void setDocerPointsForDifferentLane(ObjectNode editorNode,BpmnData sourceModel, BpmnData toModel) {
		ArrayNode dockerNode = getArrayNode();
		editorNode.set(XMLConstant.DOCKERS, dockerNode);
		
		FlowType flowType = FlowType.STRAIGHT;
		if((sourceModel.getLaneIndex() - toModel.getLaneIndex()) < 0 ) {
			flowType = FlowType.LOWER_BACKWARD;
		} else {
			flowType = FlowType.UPPER_FORWARD;
		}
		
		Bound sourceBound = sourceModel.getBound();
		Bound destinBound = toModel.getBound();
		int sourceXBound = ((sourceBound.getLowerRightX() - sourceBound.getUpperLeftX()) / 2);
		int sourceYBound = ((sourceBound.getLowerRightY() - sourceBound.getUpperLeftY()) / 2);
		int destinXBound = ((destinBound.getLowerRightX() - destinBound.getUpperLeftX()) / 2);
		int destinYBound = ((destinBound.getLowerRightY() - destinBound.getUpperLeftY()) / 2);
		
		ObjectNode objectNodeSource = getObjectNode();
		objectNodeSource.put(XMLConstant.XBOUND, sourceXBound);
		objectNodeSource.put(XMLConstant.YBOUND, sourceYBound);
		dockerNode.add(objectNodeSource);
		
		
		int laneStartWidth = getProperty(PropertiesHelper.BPMN_LANE_UPPERLEFTX, Integer.class);
		int poolStartWidth = getProperty(PropertiesHelper.BPMN_POOL_UPPERLEFTX, Integer.class);
		int upperLeftXforAdd = laneStartWidth + poolStartWidth;
		
		Bound fromPoolBound = sourceModel.getCurrentPoolBound();
		Bound toPoolBound = toModel.getCurrentPoolBound();
		if(FlowType.LOWER_BACKWARD.equals(flowType)) {
			int sourceX = ((sourceBound.getLowerRightX() + sourceBound.getUpperLeftX()) / 2) + upperLeftXforAdd;
//			int sourceY = sourceBound.getLowerRightY() + DEFAULT_HEIGHT + fromPoolBound.getUpperLeftY();
			int sourceY = fromPoolBound.getLowerRightY() - 40;
			
			ObjectNode objectNodeSource1 = getObjectNode();
			objectNodeSource1.put(XMLConstant.XBOUND, sourceX);
			objectNodeSource1.put(XMLConstant.YBOUND, sourceY);
			dockerNode.add(objectNodeSource1);
			
			
			int destinX = ((destinBound.getLowerRightX() + destinBound.getUpperLeftX()) / 2) + upperLeftXforAdd;
//			int destinY = (toPoolBound.getUpperLeftY()) + DEFAULT_Y_DIFFERENCE;
			int destinY = (toPoolBound.getUpperLeftY()) + 40;
			if(!ValidationUtils.getInstance().isEmptyString(toModel.getTextAnnotation()) ) {
				if(sourceBound.getUpperLeftX() < destinBound.getUpperLeftX()) {
					destinX = (destinBound.getUpperLeftX() + upperLeftXforAdd) - 30;
					destinXBound = destinXBound / 4;
				}else if(sourceBound.getUpperLeftX() >  destinBound.getUpperLeftX()) {
					destinX = (destinBound.getLowerRightX() + upperLeftXforAdd) + 30;
					destinXBound = (destinXBound * 2) - 10;
				}
			}
			
			ObjectNode objectNodeDestin1 = getObjectNode();
			objectNodeDestin1.put(XMLConstant.XBOUND, destinX);
			objectNodeDestin1.put(XMLConstant.YBOUND, destinY);
			dockerNode.add(objectNodeDestin1);
			
		}else if(FlowType.UPPER_FORWARD.equals(flowType)) {
			int sourceX = ((sourceBound.getLowerRightX() + sourceBound.getUpperLeftX()) / 2) + upperLeftXforAdd;
//			int sourceY = (sourceBound.getUpperLeftY() + fromPoolBound.getUpperLeftY()) - DEFAULT_HEIGHT;
			int sourceY = fromPoolBound.getUpperLeftY() + 30;
			if(!ValidationUtils.getInstance().isEmptyString(sourceModel.getTextAnnotation()) ) {
				if(sourceBound.getUpperLeftX() < destinBound.getUpperLeftX()) {
					sourceX = sourceBound.getLowerRightX() +  upperLeftXforAdd + 30;
				}else if(sourceBound.getUpperLeftX() > destinBound.getUpperLeftX()) {
					sourceX = (sourceBound.getUpperLeftX() + upperLeftXforAdd) - 30;
				}
			}
			
			ObjectNode objectNodeSource1 = getObjectNode();
			objectNodeSource1.put(XMLConstant.XBOUND, sourceX);
			objectNodeSource1.put(XMLConstant.YBOUND, sourceY);
			dockerNode.add(objectNodeSource1);
			
			
			int destinX = ((destinBound.getLowerRightX() + destinBound.getUpperLeftX()) / 2) + upperLeftXforAdd;
//			int destinY = (toPoolBound.getLowerRightY()) + DEFAULT_Y_DIFFERENCE;
			int destinY = (toPoolBound.getLowerRightY()) - 30;
			
			ObjectNode objectNodeDestin1 = getObjectNode();
			objectNodeDestin1.put(XMLConstant.XBOUND, destinX);
			objectNodeDestin1.put(XMLConstant.YBOUND, destinY);
			dockerNode.add(objectNodeDestin1);
		}
		
		
		
		ObjectNode objectNodeDestin = getObjectNode();
		objectNodeDestin.put(XMLConstant.XBOUND, destinXBound);
		objectNodeDestin.put(XMLConstant.YBOUND, destinYBound);
		dockerNode.add(objectNodeDestin);
		
		ObjectNode targetNode = getObjectNode();
		targetNode.put(XMLConstant.RESOURCE_ID, toModel.getResourceId());
		editorNode.set(XMLConstant.TARGET, targetNode);
	}

	/**
	 * Used for retrieve docker Y bound for sequential Flow.
	 * 
	 * @param seqFlowIndex
	 * @param flowType
	 * @param sourceModel
	 * @param toModel
	 * @return
	 */
	private int getDockerYPoint(int seqFlowIndex, FlowType flowType, BpmnData sourceModel, BpmnData toModel,boolean withAnnotation,Map<String,BpmnData> bpmnDataMap) {
		int dockerPoint = 0;

		Bound sourceBound = sourceModel.getBound();
		Bound destinBound = toModel.getBound();

		if (FlowType.UPPER_FORWARD.equals(flowType)) {
			int distance = toModel.getIndex() - sourceModel.getIndex();
			int maxHeightDifference = getMaxHeightDifference();
			int maxYBound = sourceBound.getUpperLeftY() > destinBound.getUpperLeftY() ? sourceBound.getUpperLeftY()
					: destinBound.getUpperLeftY();

			dockerPoint = ((maxYBound) - (XMLConstant.DOCKER_DIFFERENCE * (seqFlowIndex + distance))) - maxHeightDifference;
			
			boolean isContainTextAnnotation = false;
			if(withAnnotation) {
				for(String stepId : bpmnDataMap.keySet()) {
					BpmnData bpmndata = bpmnDataMap.get(stepId);
					if(!(ShapeType.SEQUENCE_FLOW.equals(bpmndata.getShapeType()) || ShapeType.ASSOCIATION.equals(bpmndata.getShapeType()) || ShapeType.TEXT_ANNOTATION.equals(bpmndata.getShapeType()))) {
						if(bpmndata.getLaneId().equalsIgnoreCase(sourceModel.getLaneId()) && ((bpmndata.getIndex() > sourceModel.getIndex()) || (bpmndata.getIndex() < toModel.getIndex()))) {
							if(!ValidationUtils.getInstance().isEmptyString(bpmndata.getTextAnnotation())) {
								isContainTextAnnotation = true;
								break;
							}
						}
					}
				}
			}
			
			if(isContainTextAnnotation) {
				int textAnnotationHeight = getProperty(PropertiesHelper.BPMN_TEXT_ANNOTATION_HEIGHT, Integer.class);
				int textAnnotationDistance = getProperty(PropertiesHelper.BPMN_TEXT_ANNOTATION_DISTANCE, Integer.class);
				dockerPoint = (dockerPoint - (textAnnotationHeight + textAnnotationDistance));
			}
		} else if (FlowType.LOWER_BACKWARD.equals(flowType) || FlowType.LOWER_FORWARD.equals(flowType)) {
			int distance = toModel.getIndex() - sourceModel.getIndex();
			if(distance < 0) {
				distance = Math.abs(distance);
			}
			int maxHeightDifference = getMaxHeightDifference();

			int maxYBound = sourceBound.getLowerRightY() > destinBound.getLowerRightY() ? sourceBound.getLowerRightY()
					: destinBound.getLowerRightY();

			dockerPoint = (((maxYBound + maxHeightDifference)
					+ (XMLConstant.DOCKER_DIFFERENCE * (seqFlowIndex + distance))));

		}
		return dockerPoint;
	}

	/**
	 * Used for set Flow_Type for Sequential Flow.
	 * 
	 * @param model
	 * @param fromModel
	 * @param toModel
	 */
	private void setSequenceFlowType(BpmnData model, BpmnData fromModel, BpmnData toModel,Map<String, BpmnData> bpmnDataMap) {
		FlowType flowType = FlowType.UPPER_FORWARD;
		int distance = toModel.getIndex() - fromModel.getIndex();

		if (distance == 1 || distance == 0) {
			flowType = FlowType.STRAIGHT;
		} else if (distance < 0) {
			flowType = FlowType.LOWER_BACKWARD;
		}

		if (!ValidationUtils.getInstance().isEmptyString(fromModel.getLaneId())
				&& !ValidationUtils.getInstance().isEmptyString(toModel.getLaneId())
				&& !fromModel.getLaneId().equals(toModel.getLaneId())) {
			int laneDistance = fromModel.getLaneIndex() - toModel.getLaneIndex();
			if (laneDistance == -1 || laneDistance == 1) {
				flowType = FlowType.STRAIGHT;
			}
		}
		
		if(FlowType.UPPER_FORWARD.equals(flowType) && !ValidationUtils.getInstance().isEmptyString(fromModel.getLaneId()) && fromModel.getLaneId().equalsIgnoreCase(toModel.getLaneId())) {
			if(!ValidationUtils.getInstance().isEmptyString(toModel.getTextAnnotation())) {
				flowType = FlowType.LOWER_FORWARD;
			}else if(!ValidationUtils.getInstance().isEmptyString(fromModel.getTextAnnotation())) {
				for(String key : bpmnDataMap.keySet()) {
					BpmnData bpmnData = bpmnDataMap.get(key);
					if(!(ShapeType.SEQUENCE_FLOW.equals(bpmnData.getShapeType()) || ShapeType.ASSOCIATION.equals(bpmnData.getShapeType()) || ShapeType.TEXT_ANNOTATION.equals(bpmnData.getShapeType())) && fromModel.getLaneId().equals(bpmnData.getLaneId())) {
						if(bpmnData.getIndex() == (fromModel.getIndex() + 1) && !ValidationUtils.getInstance().isEmptyString(bpmnData.getTextAnnotation())) {
							flowType = FlowType.LOWER_FORWARD;
							break;
						}
					}
				}
			}
			
		}
		model.setFlowType(flowType);
	}

	/**
	 * Used to get new Bounds for next BPMN element.
	 * 
	 * @param shapeType
	 * @param previouseBound
	 * @return
	 */
	private Bound getBound(ShapeType shapeType, Bound previouseBound) {
		int upperLeftX = getProperty(PropertiesHelper.BPMN_DEFAULT_START_EVENT_UPPERLEFTX, Integer.class);
		int upperLeftY = getProperty(PropertiesHelper.BPMN_DEFAULT_START_EVENT_UPPERLEFTY, Integer.class);

		int elementHeight = getElementHeight(shapeType);
		int elementWidth = getElementWidth(shapeType);

		int lowerRightX = upperLeftX + elementWidth;
		int lowerRightY = upperLeftY + elementHeight;

		int elementWidthGap = getProperty(PropertiesHelper.BPMN_ELEMENT_DISTANCE, Integer.class);

		if ((previouseBound != null)) {
			upperLeftX = previouseBound.getLowerRightX() + elementWidthGap;
			lowerRightX = upperLeftX + elementWidth;

			upperLeftY = ((previouseBound.getLowerRightY() + previouseBound.getUpperLeftY()) / 2) - (elementHeight / 2);
			lowerRightY = ((previouseBound.getLowerRightY() + previouseBound.getUpperLeftY()) / 2)
					+ (elementHeight / 2);
		}

		return new Bound(lowerRightX, lowerRightY, upperLeftX, upperLeftY);
	}

	/**
	 * Used for getting Width of Element on Shape Basis.
	 * 
	 * @param shapeType
	 * @return
	 */
	private int getElementWidth(ShapeType shapeType) {
		String propertyKey = PropertiesHelper.BPMN_ELEMENT_DISTANCE;
		if (ShapeType.EVENT_START_NONE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_START_EVENT_WIDTH;
		} else if (ShapeType.TASK_MANUAL.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_MANUAL_TASK_WIDTH;
		} else if (ShapeType.TASK_USER.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_USER_TASK_WIDTH;
		} else if (ShapeType.TASK_SERVICE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_SERVICE_TASK_WIDTH;
		} else if (ShapeType.SUB_PROCESS.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_SUB_PROCESS_WIDTH;
		} else if (ShapeType.DATA_STORE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_DATA_STORE_WIDTH;
		} else if (ShapeType.GATEWAY_EXCLUSIVE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_EXCLUSIVE_GATEWAY_WIDTH;
		} else if (ShapeType.GATEWAY_PARALLEL.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_PARALLEL_EXCLUSIVE_GATEWAY_WIDTH;
		} else if (ShapeType.EVENT_END_NONE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_FINAL_EVENT_WIDTH;
		} else if (ShapeType.EVENT_CATCH_MESSAGE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_CATCH_MESSAGE_EVENT_WIDTH;
		} else if (ShapeType.EVENT_THROW_NONE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_THROW_NONE_EVENT_WIDTH;
		} else if (ShapeType.TEXT_ANNOTATION.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_TEXT_ANNOTATION_WIDTH;
		}

		return getProperty(propertyKey, Integer.class);
	}

	/**
	 * Used for getting Height of Element on Shape Basis.
	 * 
	 * @param shapeType
	 * @return
	 */
	private int getElementHeight(ShapeType shapeType) {

		String propertyKey = PropertiesHelper.BPMN_DEFAULT_HEIGHT;
		if (ShapeType.EVENT_START_NONE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_START_EVENT_HEIGHT;
		} else if (ShapeType.TASK_MANUAL.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_MANUAL_TASK_HEIGHT;
		} else if (ShapeType.TASK_USER.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_USER_TASK_HEIGHT;
		} else if (ShapeType.TASK_SERVICE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_SERVICE_TASK_HEIGHT;
		} else if (ShapeType.SUB_PROCESS.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_SUB_PROCESS_HEIGHT;
		} else if (ShapeType.DATA_STORE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_DATA_STORE_HEIGHT;
		} else if (ShapeType.GATEWAY_EXCLUSIVE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_EXCLUSIVE_GATEWAY_HEIGHT;
		} else if (ShapeType.GATEWAY_PARALLEL.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_PARALLEL_EXCLUSIVE_GATEWAY_HEIGHT;
		} else if (ShapeType.EVENT_END_NONE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_FINAL_EVENT_HEIGHT;
		} else if (ShapeType.EVENT_CATCH_MESSAGE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_CATCH_MESSAGE_EVENT_HEIGHT;
		} else if (ShapeType.EVENT_THROW_NONE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_THROW_NONE_EVENT_HEIGHT;
		} else if (ShapeType.TEXT_ANNOTATION.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_TEXT_ANNOTATION_HEIGHT;
		} else if (ShapeType.DATA_STORE.equals(shapeType)) {
			propertyKey = PropertiesHelper.BPMN_TEXT_ANNOTATION_HEIGHT;
		}
		return getProperty(propertyKey, Integer.class);

	}

	/**
	 * Used for get Maximum Height difference between two elements.
	 * 
	 * @return
	 */
	private int getMaxHeightDifference() {
		int maxHeight = 0;
		int minHeight = 0;
		for (ShapeType shapeType : ShapeType.values()) {
			if (shapeType.isEnable()) {
				int shapeHeight = getElementHeight(shapeType);
				if (shapeHeight > maxHeight) {
					maxHeight = shapeHeight;
				}
				if ((minHeight == 0) || (minHeight > shapeHeight)) {
					minHeight = shapeHeight;
				}
			}
		}

		return ((maxHeight / 2) - (minHeight / 2));
	}

	/**
	 * Used for get Bounds for Connectors(Sequential_Flow).
	 * 
	 * @param sourceBound
	 * @param destinBound
	 * @return
	 */
	private Bound getBoundForConnector(Bound sourceBound, Bound destinBound) {

		int upperLeftX = sourceBound.getLowerRightX();
		int upperLeftY = ((sourceBound.getLowerRightY() + sourceBound.getUpperLeftY()) / 2);

		int lowerRightX = destinBound.getUpperLeftX();
		int lowerRightY = ((destinBound.getUpperLeftY() + destinBound.getLowerRightY()) / 2);

		return new Bound(lowerRightX, lowerRightY, upperLeftX, upperLeftY);
	}
	
	/**
	 * 
	 * @param sourceBound
	 * @param destinBound
	 * @return
	 */
	private Bound getBoundForAssociation(Bound poolBound ,Bound sourceBound, Bound destinBound) {

		int upperLeftY = poolBound.getUpperLeftY() + destinBound.getLowerRightY();
		int upperLeftX = poolBound.getUpperLeftX() + sourceBound.getUpperLeftX() + getProperty(PropertiesHelper.BPMN_LANE_UPPERLEFTX, Integer.class);
		int lowerRightX = upperLeftX + getProperty(PropertiesHelper.BPMN_TEXT_ANNOTATION_WIDTH, Integer.class);
		int lowerRightY = sourceBound.getUpperLeftY() + poolBound.getUpperLeftY();
		
		return new Bound(lowerRightX, lowerRightY, upperLeftX, upperLeftY);
	}

	/**
	 * Used for create Bpmn Json String from list of BpmnData.
	 * 
	 * @param bpmnDataList
	 * @param dataMap
	 * @param noOfTask
	 * @return
	 */
	private String createJson(List<BpmnData> bpmnDataList, Map<String, BpmnData> dataMap) {

		String json = "";
		ObjectNode editorNode = getObjectNode();

		// Set Common Properties.
		setCommonBpmnProperties(editorNode);

		ArrayNode childShapeArray = getArrayNode();
		editorNode.set(XMLConstant.CHILD_SHAPES, childShapeArray);

		// Set Properties for All child Elements.
		for (BpmnData model : bpmnDataList) {
			// Need to set bounds for SEQUENCE_FLOW.
			if (ShapeType.SEQUENCE_FLOW.equals(model.getShapeType())) {
				Bound sourceBound = dataMap.get(model.getFromStepId()).getBound();
				Bound destinBound = dataMap.get(model.getOutgoing().get(0)).getBound();
				model.setBound(getBoundForConnector(sourceBound, destinBound));
			}
			ObjectNode objectNode = createBpmnModelObjectNode(model, dataMap);
			childShapeArray.add(objectNode);
		}

		// Set Bound properties for Bpmn file.
		setBpmnBoundProperties(editorNode, bpmnDataList);

		json = editorNode.toString();
		logger.info("Model Editor Json : " + json);
		return json;
	}

	/**
	 * Used set Common BPMN Properties for Bpmn Shape Type.
	 * 
	 * @param editorNode
	 * @param noOfTask
	 */
	private void setCommonBpmnProperties(ObjectNode editorNode) {
		editorNode.put(XMLConstant.MODEL_ID, generateUUId());
		// Set Stencil Id
		ObjectNode stencilNode = getObjectNode();
		ObjectNode childStencilNode = getObjectNode();
		childStencilNode.put(XMLConstant.ID, ShapeType.BPMN_DIAGRAM.getStencilId());
		stencilNode.set(XMLConstant.STENCIL, childStencilNode);

		ObjectNode stencilSetNode = getObjectNode();
		stencilSetNode.put(XMLConstant.NAMESPACE, "http://b3mn.org/stencilset/bpmn2.0#");
		stencilSetNode.put(XMLConstant.URL, "../editor/stencilsets/bpmn2.0/bpmn2.0.json");
		editorNode.set(XMLConstant.STENCILSET, stencilSetNode);

		ObjectNode propertiesNode = getObjectNode();
		propertiesNode.put(XMLConstant.PROCESS_ID, generateUUId());
		propertiesNode.put(XMLConstant.NAME, XMLConstant.DEFAULT_BPMN_DIAGRAM_NAME);
		editorNode.set(XMLConstant.PROPERTIES, propertiesNode);
	}

	/**
	 * Used for set VBpmn Bound Properties.
	 * 
	 * @param editorNode
	 * @param bpmnDatalist
	 */
	private void setBpmnBoundProperties(ObjectNode editorNode, List<BpmnData> bpmnDatalist) {

		int lowerRightX = getProperty(PropertiesHelper.BPMN_DEFAULT_LOWERRIGHTX, Integer.class);
		int lowerRightY = getProperty(PropertiesHelper.BPMN_DEFAULT_LOWERRIGHTY, Integer.class);
		int upperLeftX = getProperty(PropertiesHelper.BPMN_DEFAULT_UPPERLEFTX, Integer.class);
		int upperLeftY = getProperty(PropertiesHelper.BPMN_DEFAULT_UPPERLEFTY, Integer.class);

		for (BpmnData model : bpmnDatalist) {
			if (!ShapeType.SEQUENCE_FLOW.equals(model.getShapeType())) {
				if (model.getBound().getLowerRightX() > lowerRightX) {
					lowerRightX = model.getBound().getLowerRightX();
				}
				if (model.getBound().getLowerRightY() > lowerRightY) {
					lowerRightY = model.getBound().getLowerRightY();
				}
			}
		}

		lowerRightX = lowerRightX + XMLConstant.BPMN_BOUND_EDGE_DIFFERENCE;
		lowerRightY = lowerRightY + (XMLConstant.BPMN_BOUND_EDGE_DIFFERENCE * bpmnDatalist.size());

		// Set Bounds for BPMN Diagram
		setBoundProperties(new Bound(lowerRightX, lowerRightY, upperLeftX, upperLeftY), editorNode);
	}

	/**
	 * Used for set properties of BPMN Model.
	 *
	 * @param propertiesNode
	 * @param model
	 */
	private void setBpmnProperties(ObjectNode propertiesNode, BpmnData model) {
		propertiesNode.put(XMLConstant.NAME,
				ValidationUtils.getInstance().isEmptyString(model.getDescription()) ? "" : model.getDescription());
		propertiesNode.put(XMLConstant.OVERRIDE_ID, model.getResourceId());
		if (ValidationUtils.getInstance().isEmptyString(model.getDescription())) {
			propertiesNode.put(XMLConstant.DOCUMENTATION, model.getDescription());
		}

		if (ShapeType.LANE.equals(model.getShapeType())) {
			propertiesNode.put(XMLConstant.SHOW_CAPTION, false);
		} else if (ShapeType.GATEWAY_EXCLUSIVE.equals(model.getShapeType())) {
			propertiesNode.put(XMLConstant.ASYNCHRONOUS_DEFINITION, "false");
			propertiesNode.put(XMLConstant.EXCLUSIVE_DEFINITION, "false");
		} else if (ShapeType.SEQUENCE_FLOW.equals(model.getShapeType())) {
			propertiesNode.put(XMLConstant.DEFAULT_FLOW, "false");
		} else if(ShapeType.TEXT_ANNOTATION.equals(model.getShapeType())) {
			propertiesNode.put(XMLConstant.DEFAULT_FLOW, "false");
			propertiesNode.put(XMLConstant.TEXT, model.getDescription());
		}

	}

	/**
	 * Used for generate BPMN XML file from model_json
	 * @param modelEditorJson
	 * @param fileName
	 * @param folderName
	 * @return
	 * @throws IOException
	 */
	public String generateBPMNFile(String modelEditorJson, String fileName, String folderName) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
		BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

		final String generatedFileName = fileName + "." + GeneratedFileExtension;
		FileOutputStream fos = null;
		File file = null;
		try {

			ObjectNode editorJsonNode = (ObjectNode) mapper.readTree(modelEditorJson);
			BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(editorJsonNode, new HashMap<>(),
					new HashMap<>());

			logger.info(bpmnModel.toString());

			for (org.flowable.bpmn.model.Process process : bpmnModel.getProcesses()) {
				if (!ValidationUtils.getInstance().isEmptyString(process.getId())) {
					char firstCharacter = process.getId().charAt(0);
					// no digit is allowed as first character
					if (Character.isDigit(firstCharacter)) {
						process.setId("a" + process.getId());
					}
				}
			}
			byte[] xmlBytes = bpmnXMLConverter.convertToXML(bpmnModel);

			file = new File(
					getProperty(PropertiesHelper.BPMN_UPLOAD_PATH, String.class) + File.separator +/* folderName + File.separator +*/ generatedFileName);

			if (!file.exists() ? file.createNewFile() : file.delete() && file.createNewFile()) {
				fos = new FileOutputStream(file);
				// Writes bytes from the specified byte array to this file output stream
				fos.write(xmlBytes);
			}
		}

		finally {
			// close the streams using close method
			if (fos != null) {
				fos.close();
			}
		}
		return file != null ? generatedFileName : null;
	}

	/**
	 * Used to convert bpmn_editor_json from list of ProcessTableDto.
	 *
	 * @param processes
	 * @return
	 */
	private String convertToJson(List<ProcessTableDto> processes) {
		String json = "";
		List<BpmnData> bpmnDataList = new LinkedList<>();
		Map<String, BpmnData> bpmnDataMap = new LinkedHashMap<>();
		Bound previouseBound = null;

		for (ProcessTableDto process : processes) {
			Bound bound = getBound(process.getShapeType(), previouseBound);
			previouseBound = bound;
			addBpmnDataFromProcessTable(process, new BpmnData(), bound, bpmnDataList, bpmnDataMap);
		}

		json = createJson(bpmnDataList, bpmnDataMap);

		return json;

	}

	/**
	 * Used to convert bpmn_editor_json from list of ProcessTableDto.
	 *
	 * @param processes
	 * @return
	 */
	private String convertToJsonFromLane(List<ProcessTableDto> processes, String fileName) {

		List<BpmnData> bpmnDataList = new LinkedList<>();
		Map<String, BpmnData> bpmnDataMap = new LinkedHashMap<>();

		for (ProcessTableDto process : processes) {
			addBpmnDataFromProcessTable(process, null, null, bpmnDataList, bpmnDataMap);
		}

		List<Participant> participants = getLaneList(bpmnDataList, bpmnDataMap);
		for (Participant participant : participants) {
			updateDataInDataMap(participant, bpmnDataMap);
		}

		for (String stepId : bpmnDataMap.keySet()) {
			BpmnData bpmnData = bpmnDataMap.get(stepId);
			if (ShapeType.SEQUENCE_FLOW.equals(bpmnData.getShapeType()) || ShapeType.ASSOCIATION.equals(bpmnData.getShapeType())) {
				BpmnData fromModel = bpmnDataMap.get(bpmnData.getFromStepId());
				BpmnData toModel = bpmnDataMap.get(bpmnData.getOutgoing().get(0));

				if (ShapeType.SEQUENCE_FLOW.equals(bpmnData.getShapeType()) || !(fromModel.getLaneId().equals(toModel.getLaneId()))) {
					Bound bound = getBoundForConnector(fromModel.getBound(), toModel.getBound());
					bpmnData.setBound(bound);
					bpmnDataMap.replace(stepId, bpmnData);
				}else if(ShapeType.ASSOCIATION.equals(bpmnData.getShapeType())) {
					Bound bound = getBoundForAssociation(bpmnData.getBound(),fromModel.getBound(), toModel.getBound());
					bpmnData.setBound(bound);
					bpmnDataMap.replace(stepId, bpmnData);
				}
			}
		}

		ObjectNode bpMnModelNode = generateBpmnNode(participants, bpmnDataMap, fileName);
		return bpMnModelNode.toString();

	}

	/**
	 * 
	 * @param participant
	 * @param bpmnDataMap
	 */
	private void updateDataInDataMap(Participant participant, Map<String, BpmnData> bpmnDataMap) {
		for (BpmnData model : participant.getBpmnDataList()) {
			bpmnDataMap.replace(model.getStepId(), model);
		}
		
		Map<String,BpmnData> participantsDataMap = new LinkedHashMap<String,BpmnData>();
		for (BpmnData model : participant.getBpmnDataList()) {
			participantsDataMap.put(model.getStepId(), model);
		}
		
		if (participant.getConnectorsList() != null && !participant.getConnectorsList().isEmpty()) {
			for (BpmnData sequenceFlow : participant.getConnectorsList()) {
				bpmnDataMap.replace(sequenceFlow.getStepId(), sequenceFlow);
			}
		}
		
		if(participant.getTextAnnotationList() != null && !participant.getTextAnnotationList().isEmpty()) {
			for(BpmnData textAnnotation : participant.getTextAnnotationList()) {
				bpmnDataMap.put(textAnnotation.getStepId(), textAnnotation);
			}
			for(BpmnData association : participant.getAssociationList()) {
				bpmnDataMap.put(association.getStepId(), association);
				BpmnData taskModel = participantsDataMap.get(association.getFromStepId());
				List<String> outGoingList = taskModel.getOutgoing();
				if(outGoingList == null) {
					outGoingList = new LinkedList<String>();
				}
				outGoingList.add(association.getStepId());
				taskModel.setOutgoing(outGoingList);
				participantsDataMap.replace(taskModel.getStepId(), taskModel);
			}
		}
		
		List<BpmnData> bpmnDataList = new LinkedList<BpmnData>();
		for(String key : participantsDataMap.keySet()) {
			bpmnDataMap.replace(key, participantsDataMap.get(key));
			bpmnDataList.add(participantsDataMap.get(key));
		}
		participant.setBpmnDataList(bpmnDataList);

	}

	/**
	 * Used for generate ObjectNode of BPMN Graph.
	 * 
	 * @param participants
	 * @param bpmnDataMap
	 * @return
	 */
	private ObjectNode generateBpmnNode(List<Participant> participants, Map<String, BpmnData> bpmnDataMap,
			String fileName) {
		List<ObjectNode> poolNodes = new LinkedList<ObjectNode>();
		
		ObjectNode bpmnNode = getObjectNode();

		setCommonBpmnProperties(bpmnNode);

		// Set stencil Id
		ObjectNode childStencilNode = getObjectNode();
		bpmnNode.set(XMLConstant.STENCIL, childStencilNode);
		childStencilNode.put(XMLConstant.ID, ShapeType.BPMN_DIAGRAM.getStencilId());

		ArrayNode childShapes = getArrayNode();
		ObjectNode poolNode = generatePoolNode(participants, bpmnDataMap, fileName);
		childShapes.add(poolNode);
		
		bpmnNode.set(XMLConstant.CHILD_SHAPES, childShapes);

		for (String key : bpmnDataMap.keySet()) {
			BpmnData model = bpmnDataMap.get(key);
			if (ShapeType.SEQUENCE_FLOW.equals(model.getShapeType()) || ShapeType.ASSOCIATION.equals(model.getShapeType())) {
				Bound poolBound = null;
				if (participants != null && !participants.isEmpty()) {
					BpmnData fromModel = bpmnDataMap.get(model.getFromStepId());
					BpmnData toModel = bpmnDataMap.get(model.getOutgoing().get(0));
//					if (fromModel.getLaneId().equals(toModel.getLaneId())) {
						for (Participant participant : participants) {
							if (participant.getStepId().equals(fromModel.getLaneId())) {
								poolBound = participant.getPoolBound();
								fromModel.setCurrentPoolBound(poolBound);
							}else if(participant.getStepId().equals(toModel.getLaneId())) {
								toModel.setCurrentPoolBound(participant.getPoolBound());
							}
							if(poolBound != null && toModel.getCurrentPoolBound() != null) {
								break;
							}
						}
//					}

				}
				ObjectNode sequentialFlowNode = generateConnectorNode(model, poolBound, bpmnDataMap);
				childShapes.add(sequentialFlowNode);
			}
			
		}

		poolNodes.add(poolNode);
		setBpmnBoundNode(bpmnNode, poolNodes);

		return bpmnNode;
	}

	/**
	 * Used for set Bounds for BPMN ObjectNode.
	 * 
	 * @param bpmnNode
	 * @param poolNode
	 */
	private void setBpmnBoundNode(ObjectNode bpmnNode, List<ObjectNode> childShapes) {

		int upperLeftX = getProperty(PropertiesHelper.BPMN_DEFAULT_UPPERLEFTX, Integer.class);
		int upperLeftY = getProperty(PropertiesHelper.BPMN_DEFAULT_UPPERLEFTY, Integer.class);

		int lowerRightX = XMLConstant.BPMN_BOUND_EDGE_DIFFERENCE;
		int lowerRightY = XMLConstant.BPMN_BOUND_EDGE_DIFFERENCE;
		for (ObjectNode poolNode : childShapes) {
			ObjectNode poolBoundNode = (ObjectNode) poolNode.get(XMLConstant.BOUNDS);
			ObjectNode poolLowerRightNode = (ObjectNode) poolBoundNode.get(XMLConstant.LOWER_RIGHT);

			int poolLowerRightX = Integer.parseInt(String.valueOf(poolLowerRightNode.get(XMLConstant.XBOUND)));
			int poolLowerRightY = Integer.parseInt(String.valueOf(poolLowerRightNode.get(XMLConstant.YBOUND)));

			int poolLowerRightX1 = poolLowerRightX + XMLConstant.BPMN_BOUND_EDGE_DIFFERENCE;
			int poolLowerRightY2 = poolLowerRightY + XMLConstant.BPMN_BOUND_EDGE_DIFFERENCE;
			if (lowerRightX < poolLowerRightX1) {
				lowerRightX = poolLowerRightX1;
			}
			if (lowerRightY < poolLowerRightY2) {
				lowerRightY = poolLowerRightY2;
			}

		}

		// Set Bound Properties for BPMN Diagram.
		setBoundProperties(new Bound(lowerRightX, lowerRightY, upperLeftX, upperLeftY), bpmnNode);
	}

	/**
	 * Used for get ObjectNode for All Sequential_Flows of BPMN Diagram.
	 * 
	 * @param model
	 * @param bpmnDataMap
	 * @return
	 */
	private ObjectNode generateConnectorNode(BpmnData model, Bound poolBound, Map<String, BpmnData> bpmnDataMap) {
		ObjectNode editorNode = getObjectNode();

		BpmnData sorceModel = bpmnDataMap.get(model.getFromStepId());
		BpmnData destinModel = bpmnDataMap.get(model.getOutgoing().get(0));

		if (ValidationUtils.getInstance().isEmptyString(model.getLaneId())) {
			Bound bound = getBoundForConnector(sorceModel.getBound(), destinModel.getBound());
			model.setBound(bound);
		}
		editorNode.put(XMLConstant.RESOURCE_ID, model.getResourceId());
		// Set BPMN Properties.
		ObjectNode propertiesNode = getObjectNode();
		setBpmnProperties(propertiesNode, model);
		editorNode.set(XMLConstant.PROPERTIES, propertiesNode);

		// Set stencil Id
		ObjectNode childStencilNode = getObjectNode();
		editorNode.set(XMLConstant.STENCIL, childStencilNode);
//		String stencilId = "DataStore".equalsIgnoreCase(model.getDescription()) ? ShapeType.DATA_ASSOCIATION.getStencilId() : ShapeType.SEQUENCE_FLOW.getStencilId();
//		
//		System.out.println("Stencil : " + stencilId);
		childStencilNode.put(XMLConstant.ID, ShapeType.SEQUENCE_FLOW.getStencilId());

		// Set Child Shapes
		editorNode.set(XMLConstant.CHILD_SHAPES, getArrayNode());

		// Set upperLeft & Lower right Boundaries for BPMN Model.
		setBoundProperties(model.getBound(), editorNode);

		// Set Docker Properties.
		BpmnData fromModel = bpmnDataMap.get(model.getFromStepId());
		BpmnData toModel = bpmnDataMap.get(model.getOutgoing().get(0));
		
		int laneDifference = fromModel.getLaneIndex() - toModel.getLaneIndex();
		if(laneDifference < 0) {
			laneDifference = Math.abs(laneDifference);
		}
		
		if(laneDifference > 1 || fromModel.getLaneId().equalsIgnoreCase(toModel.getLaneId())) {
			setDockerProperties(editorNode, model, poolBound, bpmnDataMap);
		}else {
			setDocerPointsForDifferentLane(editorNode, fromModel, toModel);
		}

		// Set Outgoing Properties.
		ArrayNode outgoingNode = getArrayNode();
		editorNode.set(XMLConstant.OUTGOING, outgoingNode);
		if ((model.getOutgoing() != null) && !model.getOutgoing().isEmpty()) {
			for (String outGoindStepId : model.getOutgoing()) {
				ObjectNode outgoingChildNode = getObjectNode();
				outgoingChildNode.put(XMLConstant.RESOURCE_ID, bpmnDataMap.get(outGoindStepId).getResourceId());
				outgoingNode.add(outgoingChildNode);
			}
		}

		return editorNode;

	}

	/**
	 * Used for get ObjectNode for all Lane Element.
	 * 
	 * @param participant
	 * @param bpmnDataMap
	 * @return
	 */
	private ObjectNode getParticipantNode(Participant participant, Map<String, BpmnData> bpmnDataMap) {
		ObjectNode participantNode = getObjectNode();
		// Set Common properties for Lane.
		participantNode.put(XMLConstant.RESOURCE_ID, participant.getResourceId());

		ObjectNode propertiesNode = getObjectNode();

		// Already Set Name on Pool.
		String name = DEFAULT_LANE_ID.equalsIgnoreCase( participant.getName()) ? "" :  participant.getName();
		propertiesNode.put(XMLConstant.NAME, name);
		propertiesNode.put(XMLConstant.OVERRIDE_ID, participant.getResourceId());
		propertiesNode.put(XMLConstant.SHOW_CAPTION, false);
		propertiesNode.put(XMLConstant.DOCUMENTATION, "");

		participantNode.set(XMLConstant.PROPERTIES, propertiesNode);

		// Set stencil Id
		ObjectNode childStencilNode = getObjectNode();
		childStencilNode.put(XMLConstant.ID, ShapeType.LANE.getStencilId());
		participantNode.set(XMLConstant.STENCIL, childStencilNode);

		ArrayNode childShapes = getArrayNode();
		for (BpmnData model : participant.getBpmnDataList()) {
			if (! (ShapeType.SEQUENCE_FLOW.equals(model.getShapeType()) || ShapeType.ASSOCIATION.equals(model.getShapeType()))) {
				ObjectNode modelNode = getBpmnDataNode(model, bpmnDataMap);
				childShapes.add(modelNode);
			}
		}
		
		//Used for add Text Annotations.
		for(BpmnData textAnnotation : participant.getTextAnnotationList()) {
			ObjectNode modelNode = getBpmnDataNode(textAnnotation, bpmnDataMap);
			childShapes.add(modelNode);
		}

		participantNode.set(XMLConstant.CHILD_SHAPES, childShapes);
		participantNode.set(XMLConstant.OUTGOING, getArrayNode());
		participantNode.set(XMLConstant.DOCKERS, getArrayNode());
		return participantNode;
	}
	
	/**
	 * 
	 * @param taskbound
	 * @return
	 */
	private Bound getTextAnnotationBound(Bound taskbound) {
		int upperLeftX = taskbound.getUpperLeftX();
		int lowerRightX = upperLeftX + getProperty(PropertiesHelper.BPMN_TEXT_ANNOTATION_WIDTH, Integer.class);

		int textAnnotationDistance = getProperty(PropertiesHelper.BPMN_TEXT_ANNOTATION_DISTANCE, Integer.class);
		int textAnnotationHeight = getProperty(PropertiesHelper.BPMN_TEXT_ANNOTATION_HEIGHT, Integer.class);

		int upperLeftY = taskbound.getUpperLeftY() - (textAnnotationDistance + textAnnotationHeight);
		int lowerRightY = upperLeftY + textAnnotationHeight;
		return new Bound(lowerRightX, lowerRightY, upperLeftX, upperLeftY);
	}
	
	/**
	 * Used for Get Bounds for Lane.
	 * 
	 * @param model
	 * @return
	 */
	private Bound getParticipantBound(Participant model, Bound previouseBound) {

		int upperLeftY = getProperty(PropertiesHelper.BPMN_LANE_UPPERLEFTY, Integer.class);
		int upperLeftX = getProperty(PropertiesHelper.BPMN_LANE_UPPERLEFTX, Integer.class);

		if (previouseBound != null) {
			upperLeftY = previouseBound.getLowerRightY()
					+ getProperty(PropertiesHelper.BPMN_POOL_DISTANCE, Integer.class);
		}

		int lowerRightX = upperLeftX + getProperty(PropertiesHelper.BPMN_LANE_WIDTH, Integer.class);
		int lowerRightY = upperLeftY + getProperty(PropertiesHelper.BPMN_LANE_HEIGHT, Integer.class);

		for (BpmnData data : model.getBpmnDataList()) {

			if(data.getBound() != null) {
				if (lowerRightX < data.getBound().getLowerRightX()) {
					lowerRightX = data.getBound().getLowerRightX();
				}

				if (lowerRightY < data.getBound().getLowerRightY()) {
					lowerRightY = data.getBound().getLowerRightY();
				}
			}
			
		}

		lowerRightX = lowerRightX + 100;

		return new Bound(lowerRightX, lowerRightY, upperLeftX, upperLeftY);

	}

	/**
	 * 
	 * @param participantBound
	 * @param prevPoolBound
	 * @return
	 */
	private Bound getPoolBound(Bound participantBound, Bound prevPoolBound) {
		int upperLeftX = getProperty(PropertiesHelper.BPMN_POOL_UPPERLEFTX, Integer.class);
		int upperLeftY = participantBound.getUpperLeftY();
		int lowerRightY = participantBound.getLowerRightY();
		int lowerRightX = upperLeftX + participantBound.getLowerRightX();

		if (prevPoolBound != null) {
			int prevLowerRightY = prevPoolBound.getLowerRightY();
			upperLeftY = prevLowerRightY + getProperty(PropertiesHelper.BPMN_POOL_DISTANCE, Integer.class);

			int distance = participantBound.getLowerRightY() - participantBound.getUpperLeftY();
			lowerRightY = upperLeftY + distance;
		}
		return new Bound(lowerRightX, lowerRightY, upperLeftX, upperLeftY);
	}

	/**
	 * Used for get ObjectNode for BpmnData.
	 * 
	 * @param model
	 * @param bpmnDataMap
	 * @return
	 */
	private ObjectNode getBpmnDataNode(BpmnData model, Map<String, BpmnData> bpmnDataMap) {

		ObjectNode editorNode = getObjectNode();
		editorNode.put(XMLConstant.RESOURCE_ID, model.getResourceId());

		// Set BPMN Properties.
		ObjectNode propertiesNode = getObjectNode();
		setBpmnProperties(propertiesNode, model);
		editorNode.set(XMLConstant.PROPERTIES, propertiesNode);

		// Set stencil Id
		ObjectNode childStencilNode = getObjectNode();
		editorNode.set(XMLConstant.STENCIL, childStencilNode);
		childStencilNode.put(XMLConstant.ID, model.getShapeType().getStencilId());

		// Set Child Shapes
		editorNode.set(XMLConstant.CHILD_SHAPES, getArrayNode());

		// Set upperLeft & Lower right Boundaries for BPMN Model.
		setBoundProperties(model.getBound(), editorNode);

		// Set Docker Properties.
		setDockerProperties(editorNode, model, null, bpmnDataMap);

		// Set Outgoing Properties.
		ArrayNode outgoingNode = getArrayNode();
		editorNode.set(XMLConstant.OUTGOING, outgoingNode);
		if ((model.getOutgoing() != null) && !model.getOutgoing().isEmpty()) {
			for (String outGoindStepId : model.getOutgoing()) {
				ObjectNode outgoingChildNode = getObjectNode();
				outgoingChildNode.put(XMLConstant.RESOURCE_ID, bpmnDataMap.get(outGoindStepId).getResourceId());
				outgoingNode.add(outgoingChildNode);
			}
		}

		return editorNode;
	}

	/**
	 * Used for convert ProcessTableDto to BpmnData.
	 * 
	 * @param dto
	 * @param model
	 * @param bound
	 * @param bpmnDataList
	 * @param resourceIdMap
	 */
	private void addBpmnDataFromProcessTable(ProcessTableDto dto, BpmnData model, Bound bound,
			List<BpmnData> bpmnDataList, Map<String, BpmnData> resourceIdMap) {

		if (model == null) {
			model = new BpmnData();
		}

		if (bound == null) {
			bound = new Bound(0, 0, 0, 0);
		}

		model.setIndex(dto.getIndex());
		model.setStepId(dto.getStepId());
		model.setShapeType(dto.getShapeType());
		String laneId = ValidationUtils.getInstance().isEmptyString(dto.getLaneId()) ? DEFAULT_LANE_ID : dto.getLaneId();
		model.setLaneId(laneId);
		String participantStr = ValidationUtils.getInstance().isEmptyString(dto.getParticipant()) ? DEFAULT_LANE_ID : dto.getParticipant();
		model.setParticipant(participantStr);
		model.setResourceId(dto.getShapeType().getStencilId() + "_" + generateUUId());
		model.setTextAnnotation(dto.getTextAnnotation());
		model.setBound(bound);
		resourceIdMap.put(dto.getStepId(), model);

		String description = dto.getStepDescription();
		if (ValidationUtils.getInstance().isEmptyString(description)) {
			description = (ShapeType.EVENT_START_NONE.equals(dto.getShapeType())
					|| ShapeType.EVENT_END_NONE.equals(dto.getShapeType())) ? dto.getShapeType().getLabel() : "";
		}
		model.setDescription(description);

		// Set Outgoing List.
		List<StepConnectorDto> connectors = getOutGoing(dto);
		model.setConnectors(connectors);

		List<String> modelOutGoing = new LinkedList<>();
		// Create BPMN model for SEQUENCE_FLOW.
		setConnectorsData(connectors, model, modelOutGoing, bpmnDataList, resourceIdMap);
		
		model.setOutgoing(modelOutGoing);

		bpmnDataList.add(model);
	}

	/**
	 * Used for get OutGoing list of BPMNData.
	 * 
	 * @param dto
	 * @return
	 */
	private List<StepConnectorDto> getOutGoing(ProcessTableDto dto) {
		List<StepConnectorDto> connectors = new ArrayList<>();
		if (!ValidationUtils.getInstance().isEmptyString(dto.getNextStepId())) {
			if (!dto.getNextStepId().contains(",")) {
				String label = ValidationUtils.getInstance().isEmptyString(dto.getConnectorLabel()) ? ""
						: dto.getConnectorLabel();
				connectors.add(new StepConnectorDto(dto.getNextStepId(), 1, label));
			} else {
				String[] steps = dto.getNextStepId().split(",");
				for (int i = 0; i < steps.length; i++) {
					String stepId = steps[i];
					String label = "";
					String[] labels = null;
					if (!ValidationUtils.getInstance().isEmptyString(dto.getConnectorLabel())
							&& dto.getConnectorLabel().contains(",")) {
						labels = dto.getConnectorLabel().split(",");
					} else if ((steps != null) && (steps.length == 2)
							&& ShapeType.GATEWAY_EXCLUSIVE.equals(dto.getShapeType())) {
						labels = XMLConstant.EXCLUSIVE_GATEWAY_LABEL.split(",");
					}
					label = ((labels != null) && (labels.length > i)) ? labels[i] : "";
					connectors.add(new StepConnectorDto(stepId, i + 1, label));
				}
			}
		}
		return connectors;
	}

	/**
	 * Used for add BPMN model of connectors.
	 * 
	 * @param connectors
	 * @param model
	 * @param modelOutGoing
	 * @param bpmnDataList
	 * @param resourceIdMap
	 */
	private void setConnectorsData(List<StepConnectorDto> connectors, BpmnData model, List<String> modelOutGoing,
			List<BpmnData> bpmnDataList, Map<String, BpmnData> resourceIdMap) {
		// Create BPMN model for SEQUENCE_FLOW.
		for (StepConnectorDto stepConnector : connectors) {
			BpmnData connectorModel = new BpmnData();

			connectorModel.setIndex(stepConnector.getNoOfConnector());
			connectorModel.setStepId(generateUUId());
			connectorModel.setDescription(stepConnector.getLabel());
			// Default Set SEQUENCE_FLOW Shape type.
			connectorModel.setShapeType(ShapeType.SEQUENCE_FLOW);
			connectorModel.setLaneId(model.getLaneId());
			connectorModel.setParticipant(model.getParticipant());
			connectorModel.setResourceId(ShapeType.SEQUENCE_FLOW.getStencilId() + "_" + generateUUId());

			resourceIdMap.put(connectorModel.getStepId(), connectorModel);

			List<String> childModeloutGoing = new LinkedList<>();
			childModeloutGoing.add(stepConnector.getNextStepId());
			connectorModel.setOutgoing(childModeloutGoing);

			modelOutGoing.add(connectorModel.getStepId());

			connectorModel.setFromStepId(model.getStepId());
			
			bpmnDataList.add(connectorModel);

		}

	}
	
	/**
	 * used for Get List of All Lane Elements from list of BPMNData.
	 * 
	 * @param modelList
	 * @return
	 */
	private List<Participant> getLaneList(List<BpmnData> modelList, Map<String, BpmnData> bpmnDataMap) {

		List<Participant> laneList = new LinkedList<>();
		Map<String, List<BpmnData>> laneBpmnDataMap = new LinkedHashMap<>();
		Map<String, List<BpmnData>> laneBpmnConnectorMap = new LinkedHashMap<>();

		int laneIndex = 1;
		for (BpmnData model : modelList) {
			if (!(ShapeType.SEQUENCE_FLOW.equals(model.getShapeType()) || ShapeType.ASSOCIATION.equals(model.getShapeType()) || ShapeType.TEXT_ANNOTATION.equals(model.getShapeType()))) {
				if (laneBpmnDataMap.containsKey(model.getLaneId())) {
					List<BpmnData> existingList = new LinkedList<>();
					existingList = laneBpmnDataMap.get(model.getLaneId());
					model.setIndex(existingList.size() + 1);
					existingList.add(model);
					laneBpmnDataMap.replace(model.getLaneId(), existingList);
				} else {
					List<BpmnData> bpmnDataList = new LinkedList<>();
					model.setIndex(bpmnDataList.size() + 1);
					bpmnDataList.add(model);
					laneBpmnDataMap.put(model.getLaneId(), bpmnDataList);
				}
			} else if(ShapeType.SEQUENCE_FLOW.equals(model.getShapeType())){
				BpmnData source = bpmnDataMap.get(model.getFromStepId());
				BpmnData destin = bpmnDataMap.get(model.getOutgoing().get(0));

				if (!ValidationUtils.getInstance().isEmptyString(source.getLaneId())
						&& !ValidationUtils.getInstance().isEmptyString(destin.getLaneId())
						&& source.getLaneId().equals(destin.getLaneId())) {
					model.setLaneId(source.getLaneId());
					if (laneBpmnConnectorMap.containsKey(model.getLaneId())) {
						List<BpmnData> existingList = new LinkedList<>();
						existingList = laneBpmnConnectorMap.get(model.getLaneId());
						model.setIndex(existingList.size() + 1);
						existingList.add(model);
						laneBpmnConnectorMap.replace(model.getLaneId(), existingList);
					} else {
						List<BpmnData> bpmnDataList = new LinkedList<>();
						model.setIndex(bpmnDataList.size() + 1);
						bpmnDataList.add(model);
						laneBpmnConnectorMap.put(model.getLaneId(), bpmnDataList);
					}

				}
			}
		}

		Bound previouseParticipantBound = null;
		Bound previouserPoolBound = null;
		int maxLowerRigtX = 0;
		for (String key : laneBpmnDataMap.keySet()) {
			Participant participant = new Participant();
			participant.setIndex(laneIndex);
			participant.setName(laneBpmnDataMap.get(key).get(0).getParticipant());
			participant.setStepId(key);
			participant.setResourceId(generateUUId());

			participant.setBpmnDataList(laneBpmnDataMap.get(key));
			
			if (laneBpmnConnectorMap.containsKey(key)) {
				List<BpmnData> connectorlist = laneBpmnConnectorMap.get(key);
				participant.setConnectorsList(connectorlist);
			}

			participant = getParticipantWithBound(participant, previouseParticipantBound);
			if(participant.getBound().getLowerRightX() > maxLowerRigtX) {
				maxLowerRigtX = participant.getBound().getLowerRightX();
			}
			Bound poolBound = getPoolBound(participant.getBound(), previouserPoolBound);
			participant.setPoolBound(poolBound);
			
			setTextAnnotationData(participant, poolBound);

			previouseParticipantBound = participant.getBound();
			previouserPoolBound = poolBound;
			laneList.add(participant);
			laneIndex += 1;
		}
		
		laneList = updateParticipantBounds(laneList, maxLowerRigtX);
		return laneList;
	}
	
	/**
	 * Used for update lower-right-X in all Participant,
	 * To make same width for all Participant.
	 * @param participants
	 * @param maxLowerRigtX
	 * @return
	 */
	private List<Participant> updateParticipantBounds(List<Participant> participants, int maxLowerRigtX) {
		List<Participant> participantList = new LinkedList<Participant>();
		for(Participant participant : participants) {
			Bound particpantBound = participant.getBound();
			particpantBound.setLowerRightX(maxLowerRigtX);
			participant.setBound(particpantBound);
			participantList.add(participant);
		}
		return participantList;
	}
	
	/**
	 * 
	 * @param participant
	 * @param poolBound
	 */
	private void setTextAnnotationData(Participant participant,Bound poolBound) {
		if(participant.getBpmnDataList() != null && !participant.getBpmnDataList().isEmpty()) {
			List<BpmnData> textAnnotationList = new LinkedList<BpmnData>();
			List<BpmnData> associationList = new LinkedList<BpmnData>();
			for(BpmnData model : participant.getBpmnDataList()) {
				if(!ValidationUtils.getInstance().isEmptyString(model.getTextAnnotation())) {
					BpmnData textAnnotation = new BpmnData();

					textAnnotation.setIndex(model.getIndex());
					textAnnotation.setStepId(generateUUId());
					textAnnotation.setDescription(model.getTextAnnotation());
					// Default Set SEQUENCE_FLOW Shape type.
					textAnnotation.setShapeType(ShapeType.TEXT_ANNOTATION);
					textAnnotation.setLaneId(model.getLaneId());
					textAnnotation.setLaneIndex(model.getLaneIndex());
					textAnnotation.setParticipant(model.getParticipant());
					textAnnotation.setResourceId(ShapeType.TEXT_ANNOTATION.getStencilId() + "_" + generateUUId());
					textAnnotation.setBound(getTextAnnotationBound(model.getBound()));
                    
					BpmnData association = new BpmnData();

					association.setIndex(model.getIndex());
					association.setStepId(generateUUId());
					association.setDescription("");
					// Default Set SEQUENCE_FLOW Shape type.
					association.setShapeType(ShapeType.ASSOCIATION);
					association.setLaneId(model.getLaneId());
					association.setParticipant(model.getParticipant());
					association.setResourceId(ShapeType.ASSOCIATION.getStencilId() + "_" + generateUUId());

					association.setFromStepId(model.getStepId());
					textAnnotation.setFromStepId(association.getStepId());

					List<String> associationOutGoing = new LinkedList<>();
					associationOutGoing.add(textAnnotation.getStepId());
					association.setOutgoing(associationOutGoing);
					association.setBound(poolBound);
					
					associationList.add(association);
					textAnnotationList.add(textAnnotation);

				}
			}
			participant.setAssociationList(associationList);
			participant.setTextAnnotationList(textAnnotationList);
		}
	}
	
	/**
	 * 
	 * @param patricipant
	 * @param previouseBound
	 * @return
	 */
	private Participant getParticipantWithBound(Participant patricipant, Bound previouseBound) {

		Map<String, BpmnData> bpmnDataMap = new LinkedHashMap<String, BpmnData>();

		Bound participantBound = getParticipantBound(patricipant, previouseBound);
		patricipant.setBound(participantBound);

		List<BpmnData> taskModels = new LinkedList<>();
		List<BpmnData> connectors = new LinkedList<>();

		Bound taskPreviouseBound = null;

		// First Set Simple bound
		int maxUpperLeftY = 0;
		int maxLowerRightY = 0;
		int maxLowerRightX = participantBound.getLowerRightX();
		boolean isContainTextAnnotation = false;
		for (BpmnData taskModel : patricipant.getBpmnDataList()) {
			if(!ValidationUtils.getInstance().isEmptyString(taskModel.getTextAnnotation()) && !isContainTextAnnotation) {
				isContainTextAnnotation = true;
			}
			taskModel.setLaneIndex(patricipant.getIndex());
			Bound taskBound = getBound(taskModel.getShapeType(), taskPreviouseBound);
			taskModel.setBound(taskBound);
			taskPreviouseBound = taskBound;
			taskModels.add(taskModel);

			if (maxUpperLeftY < taskBound.getUpperLeftY()) {
				maxUpperLeftY = taskBound.getUpperLeftY();
			}
			if (maxLowerRightY < taskBound.getLowerRightY()) {
				maxLowerRightY = taskBound.getLowerRightY();
			}
			if (maxLowerRightX < taskBound.getLowerRightX()) {
				maxLowerRightX = taskBound.getLowerRightX();
			}

			bpmnDataMap.put(taskModel.getStepId(), taskModel);
		}
		
		participantBound.setLowerRightX(maxLowerRightX + 100);
		patricipant.setBound(participantBound);

		int maxLowerDockerYPoint = 0;
		int maxUpperDockerPoint = 0;

		// First Set Simple bound
		if (patricipant.getConnectorsList() != null && !patricipant.getConnectorsList().isEmpty()) {
			for (BpmnData connectorModel : patricipant.getConnectorsList()) {
				BpmnData sourceModel = bpmnDataMap.get(connectorModel.getFromStepId());
				BpmnData destinationModel = bpmnDataMap.get(connectorModel.getOutgoing().get(0));
				Bound connectorBound = getBoundForConnector(sourceModel.getBound(), destinationModel.getBound());
				connectorModel.setBound(connectorBound);
				connectors.add(connectorModel);

				// Found maximun & minimum Docker Point.
				int distance = destinationModel.getIndex() - sourceModel.getIndex();
				if (distance > 1) {
					FlowType flowType = FlowType.UPPER_FORWARD; 
					BpmnData fromModel = null;
					for(BpmnData bpmnData : patricipant.getBpmnDataList()) {
						if(connectorModel.getFromStepId().equalsIgnoreCase(bpmnData.getStepId())) {
							fromModel = bpmnData;
						}
						if(!ValidationUtils.getInstance().isEmptyString(bpmnData.getTextAnnotation())) {
							if(fromModel != null && !ValidationUtils.getInstance().isEmptyString(fromModel.getTextAnnotation()) && (bpmnData.getIndex() == (fromModel.getIndex() + 1))) {
								flowType = FlowType.LOWER_FORWARD;
								break;
							}else if(connectorModel.getOutgoing().get(0).equalsIgnoreCase(bpmnData.getStepId())) {
								flowType = FlowType.LOWER_FORWARD;
								break;
							}
						}
					
					}
					
					int minPoint = getDockerYPoint(connectorModel.getIndex(), flowType, sourceModel,
							destinationModel,false,bpmnDataMap);
					if(FlowType.UPPER_FORWARD.equals(flowType)) {
						int difference = maxUpperLeftY - minPoint;
						if (difference < 0) {
							difference = Math.abs(difference);
						}
						if (maxUpperDockerPoint < difference) {
							maxUpperDockerPoint = difference;
						}	
					}else if(FlowType.LOWER_FORWARD.equals(flowType) && minPoint > maxLowerRightY) {
							int difference = minPoint - maxLowerRightY;
							if (difference > maxLowerDockerYPoint) {
								maxLowerDockerYPoint = difference;
							}
					}
					

				} else if (distance < -1) {
					int maxPoint = getDockerYPoint(connectorModel.getIndex(), FlowType.LOWER_BACKWARD, sourceModel,
							destinationModel,false,bpmnDataMap);
					if (maxPoint > maxLowerRightY) {
						int difference = maxPoint - maxLowerRightY;
						if (difference > maxLowerDockerYPoint) {
							maxLowerDockerYPoint = difference;
						}
					}
				}
			}
		}
		

		if (isContainTextAnnotation) {
			int textAnnotationDistace = getProperty(PropertiesHelper.BPMN_TEXT_ANNOTATION_DISTANCE, Integer.class);
			int textAnnotationHeight = getProperty(PropertiesHelper.BPMN_TEXT_ANNOTATION_HEIGHT, Integer.class);
			maxUpperDockerPoint = maxUpperDockerPoint + textAnnotationDistace + textAnnotationHeight;
		}

		int increamentY = maxLowerDockerYPoint + maxUpperDockerPoint;
		if (maxUpperDockerPoint > 0) {
			List<BpmnData> bpmnTaskAfterBound = new LinkedList<BpmnData>();
			for (BpmnData taskModel : taskModels) {
				Bound bound = taskModel.getBound();
				bound.setLowerRightY(bound.getLowerRightY() + maxUpperDockerPoint);
				bound.setUpperLeftY(bound.getUpperLeftY() + maxUpperDockerPoint);
				taskModel.setBound(bound);

				bpmnTaskAfterBound.add(taskModel);
			}

			List<BpmnData> sequenceFlows = new LinkedList<>();
			for (BpmnData sequenceFlow : connectors) {
				Bound bound = sequenceFlow.getBound();
				bound.setLowerRightY(bound.getLowerRightY() + maxUpperDockerPoint);
				bound.setUpperLeftY(bound.getUpperLeftY() + maxUpperDockerPoint);
				sequenceFlow.setBound(bound);
				sequenceFlows.add(sequenceFlow);
			}
			patricipant.setBpmnDataList(bpmnTaskAfterBound);
			patricipant.setConnectorsList(sequenceFlows);

		}
		participantBound.setLowerRightY(participantBound.getLowerRightY() + increamentY);

		patricipant.setBound(participantBound);
		return patricipant;
	}

	/**
	 * Used for generate ObjectNode for Pool Element.
	 * 
	 * @param participants
	 * @param bpmnDataMap
	 * @return
	 */
	private ObjectNode generatePoolNode(List<Participant> participants, Map<String, BpmnData> bpmnDataMap,
			String fileName) {
		ObjectNode poolNode = getObjectNode();
		String resourceId = generateUUId();
		poolNode.put(XMLConstant.RESOURCE_ID, resourceId);

		ObjectNode propertiesNode = getObjectNode();
		poolNode.set(XMLConstant.PROPERTIES, propertiesNode);
		propertiesNode.put(XMLConstant.OVERRIDE_ID, resourceId);
		propertiesNode.put(XMLConstant.PROCESS_ID, "Process_" + generateUUId());
		propertiesNode.put(XMLConstant.NAME, fileName);
		propertiesNode.put(XMLConstant.ISEXECUTABLE, "false");
		propertiesNode.put(XMLConstant.DOCUMENTATION, "");

		// Set stencil Id
		ObjectNode childStencilNode = getObjectNode();
		poolNode.set(XMLConstant.STENCIL, childStencilNode);
		childStencilNode.put(XMLConstant.ID, ShapeType.POOL.getStencilId());

		poolNode.set(XMLConstant.OUTGOING, getArrayNode());
		poolNode.set(XMLConstant.DOCKERS, getArrayNode());

		ArrayNode childShapes = getArrayNode();
		int upperLeftX = getProperty(PropertiesHelper.BPMN_POOL_UPPERLEFTX, Integer.class);
		int lowerRightX = upperLeftX + 100;
		int upperLeftY = getProperty(PropertiesHelper.BPMN_DEFAULT_UPPERLEFTY, Integer.class);
		int lowerRightY = getProperty(PropertiesHelper.BPMN_DEFAULT_HEIGHT, Integer.class);
		for (Participant participant : participants) {
			ObjectNode participantNode = getParticipantNode(participant, bpmnDataMap);
			setBoundProperties(participant.getBound(), participantNode);
			childShapes.add(participantNode);
			if (participant.getBound().getUpperLeftY() < upperLeftY) {
				upperLeftY = participant.getBound().getUpperLeftY();
			}
			if (participant.getBound().getLowerRightY() > lowerRightY) {
				lowerRightY = participant.getBound().getLowerRightY();
			}
			if (participant.getBound().getLowerRightX() > lowerRightX) {
				lowerRightX = participant.getBound().getLowerRightX();
			}

		}

		lowerRightX = lowerRightX + upperLeftX;

		Bound poolBound = new Bound(lowerRightX, lowerRightY, upperLeftX, upperLeftY);
		setBoundProperties(poolBound, poolNode);
		poolNode.set(XMLConstant.CHILD_SHAPES, childShapes);

		return poolNode;
	}

}
