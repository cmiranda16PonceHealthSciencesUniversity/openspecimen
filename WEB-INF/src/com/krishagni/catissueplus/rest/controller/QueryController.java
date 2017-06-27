package com.krishagni.catissueplus.rest.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.util.ConfigUtil;
import com.krishagni.catissueplus.core.de.events.ExecuteQueryEventOp;
import com.krishagni.catissueplus.core.de.events.ExecuteSavedQueryOp;
import com.krishagni.catissueplus.core.de.events.FacetDetail;
import com.krishagni.catissueplus.core.de.events.GetFacetValuesOp;
import com.krishagni.catissueplus.core.de.events.QueryDataExportResult;
import com.krishagni.catissueplus.core.de.events.QueryExecResult;
import com.krishagni.catissueplus.core.de.services.QueryService;
import com.krishagni.catissueplus.core.de.services.SavedQueryErrorCode;

import edu.common.dynamicextensions.nutility.IoUtil;

@Controller
@RequestMapping("/query")
public class QueryController {
	
	@Autowired
	private HttpServletRequest httpServletRequest;
	
	@Autowired
	private QueryService querySvc;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody	
	public QueryExecResult executeQuery(@RequestBody ExecuteQueryEventOp opDetail) {
		return response(querySvc.executeQuery(request(opDetail)));
	}

	@RequestMapping(method = RequestMethod.POST, value="/{queryId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public QueryExecResult executeQuery(
			@PathVariable("queryId")
			long queryId,

			@RequestBody
			ExecuteSavedQueryOp opDetail) {

		opDetail.setSavedQueryId(queryId);
		return response(querySvc.executeSavedQuery(request(opDetail)));
	}

	@RequestMapping(method = RequestMethod.GET, value="/default-result-view")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<String> getDefaultResultView() {
		String fieldsJson = ConfigUtil.getInstance().getFileContent("query", "default_result_view", null);
		if (StringUtils.isBlank(fieldsJson)) {
			return Collections.emptyList();
		} else {
			try {
				return new ObjectMapper().readValue(fieldsJson, new TypeReference<List<String>>(){});
			} catch (IOException e) {
				throw OpenSpecimenException.userError(SavedQueryErrorCode.INV_RV_CFG, e.getMessage());
			}
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value="/export")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public QueryDataExportResult exportQueryData(@RequestBody ExecuteQueryEventOp opDetail) {
		return response(querySvc.exportQueryData(request(opDetail)));
	}	
	
	@RequestMapping(method = RequestMethod.GET, value="/export")
	@ResponseStatus(HttpStatus.OK)
	public void downloadExportDataFile(
			@RequestParam(value = "fileId", required = true) 
			String fileId,
			
			@RequestParam(value = "filename", required = false, defaultValue = "QueryResults.csv")
			String filename,
			
			HttpServletResponse response) {
		
		File file = response(querySvc.getExportDataFile(request(fileId)));

		response.setContentType("text/csv;");
		response.setHeader("Content-Disposition", "attachment;filename=" + filename);

		InputStream in = null;
		try {
			in = new FileInputStream(file);
			IoUtil.copy(in, response.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException("Error sending file", e);
		} finally {
			IoUtil.close(in);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value="/facet-values")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<FacetDetail> getFacetValues(@RequestBody GetFacetValuesOp op) {
		return response(querySvc.getFacetValues(request(op)));
	}

	private <T> T response(ResponseEvent<T> resp) {
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}

	private <T> RequestEvent<T> request(T payload) {
		return new RequestEvent<>(payload);
	}
}
