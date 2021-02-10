/*
 * #%L
 * de-metas-camel-externalsystems
 * %%
 * Copyright (C) 2021 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package de.metas.camel.externalsystems.metasfresh.calldispatcher;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.metas.common.externalsystem.JsonExternalSystemRequest;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.stereotype.Component;

@Component
public class CallDispatcherRouteBuilder extends RouteBuilder
{

	@Override
	public void configure()
	{
		final var objectMapper = (new ObjectMapper())
				.findAndRegisterModules()
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
				.enable(MapperFeature.USE_ANNOTATIONS);

		final JacksonDataFormat jacksonDataFormat = new JacksonDataFormat();
		jacksonDataFormat.setCamelContext(getContext());
		jacksonDataFormat.setObjectMapper(objectMapper);
		jacksonDataFormat.setUnmarshalType(JsonExternalSystemRequest.class);

		rest("/").produces("text/plain") // TODO fix
				.get("hello")
				.to("direct:dispatch");

		from("direct:dispatch")
				.routeId("MF-HTTP-To-ExternalSystem-Dispatcher")
				.streamCaching()
				.unmarshal(jacksonDataFormat)
				.process(exchange -> {
					final var request = exchange.getIn().getBody(JsonExternalSystemRequest.class);
					exchange.getIn().setHeader("targetRoute", request.getExternalSystemName() + "-" + request.getCommand());
				})
				.log("routing request to route ${header.targetRoute}")
				.toD("direct:${header.targetRoute}", false);
	}
}
