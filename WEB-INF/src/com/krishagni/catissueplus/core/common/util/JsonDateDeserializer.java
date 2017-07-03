package com.krishagni.catissueplus.core.common.util;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.UntypedObjectDeserializer;


public class JsonDateDeserializer extends UntypedObjectDeserializer {
	private Log logger = LogFactory.getLog(JsonDateDeserializer.class);

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt)
	throws IOException {
		Object ret;

		if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
			try {
				ret = DateUtils.parseDate(p.getText(), "yyyy-MM-dd");
			} catch (Exception e) {
				try {
					ret = Date.from(Instant.parse(p.getText()));
				} catch (Exception e1) {
					ret = super.deserialize(p, ctxt);
				}
			}
		} else if (p.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
			try {
				ret = new Date(p.getLongValue());
			} catch (Exception e) {
				ret = super.deserialize(p, ctxt);
			}
		} else {
			ret = super.deserialize(p, ctxt);
		}

		logger.info("Input date " + p.getText() + " deserialized to " + ret);
		return ret;
	}
}
