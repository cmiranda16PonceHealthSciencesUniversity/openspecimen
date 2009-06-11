/**
 * 
 */

package edu.wustl.catissuecore.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.common.dynamicextensions.bizlogic.BizLogicFactory;
import edu.common.dynamicextensions.dao.impl.DynamicExtensionDAO;
import edu.common.dynamicextensions.domain.AbstractMetadata;
import edu.common.dynamicextensions.domain.integration.EntityMap;
import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
import edu.wustl.catissuecore.bizlogic.AnnotationBizLogic;
import edu.wustl.catissuecore.util.global.AppUtility;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.bizlogic.DefaultBizLogic;
import edu.wustl.common.exception.ApplicationException;
import edu.wustl.common.util.logger.Logger;
import edu.wustl.common.util.logger.LoggerConfig;

/**
 * @author suhas_khot
 * This class adds default entityMapConditions to the forms/Entities. 
 */
public final class AddEntityMapConditions
{
	static
	{
		LoggerConfig.configureLogger(System.getProperty("user.dir"));
	}
	private static Logger logger = Logger.getCommonLogger(AddEntityMapConditions.class);
	/*
	 * create singleton object
	 */
	private static AddEntityMapConditions addEntityMapCond= new AddEntityMapConditions();
	/*
	 * Private constructor
	 */
	private AddEntityMapConditions()
	{
		
	}
	
	/*
	 * returns single object
	 */
	public static AddEntityMapConditions getInstance()
	{
		return addEntityMapCond;
	}
	
	
	/*
	 * @param args command line inputs
	 * @throws DAOException if fails to get Object from database
	 * @throws DynamicExtensionsSystemException fails to get container for all entities
	 */
	public static void main(String[] args) throws DynamicExtensionsSystemException, ApplicationException
	{
		Long typeId = (Long) AppUtility.getObjectIdentifier(Constants.COLLECTION_PROTOCOL,
				AbstractMetadata.class.getName(), Constants.NAME);
		Map<Long, Long> entityIdsVsContId = AppUtility.getAllContainers();
		Collection<Long> containerIdColl = (Collection) entityIdsVsContId.values();
		Long cpId = Long.valueOf(-1);
		associateFormsToCP(cpId, typeId, containerIdColl);
	}

	/**
	 * @param cpId stores Id of Collection Protocol 
	 * @param typeId stores Id of Collection Protocol object
	 * @param entityIds entityIds collection
	 * @throws ApplicationException 
	 * @throws DynamicExtensionsSystemException 
	 */
	private static void associateFormsToCP(Long cpId, Long typeId, Collection<Long> containerIds)
			throws DynamicExtensionsSystemException, ApplicationException
	{
		AnnotationBizLogic annotation = new AnnotationBizLogic();
		annotation.setAppName(DynamicExtensionDAO.getInstance().getAppName());
		DefaultBizLogic defaultBizLogic = BizLogicFactory.getDefaultBizLogic();
		for (Long containerId : containerIds)
		{
			if (containerId != null)
			{
				List<EntityMap> entityMapList = defaultBizLogic.retrieve(EntityMap.class.getName(),
						Constants.CONTAINERID, containerId);
				if (entityMapList != null && !entityMapList.isEmpty())
				{
					EntityMap entityMap = entityMapList.get(0);
					AppUtility.editConditions(entityMap, cpId, typeId, false);
					annotation.updateEntityMap(entityMap);
				}
			}
		}
	}

}
