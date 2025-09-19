package org.quarkus.assignment.api.exception;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.quarkus.assignment.dto.ErrorResponse;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Global mapper that converts exceptions to JSON error responses.
 */
@Provider
@ApplicationScoped
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
	private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

	@Context
	UriInfo uriInfo;

	/**
	 * Builds a JSON error response from the thrown exception.
	 * @param exception the thrown exception
	 * @return response with status and ErrorResponse body
	 */
	@Override
	public Response toResponse(Throwable exception) {
		int status = mapStatus(exception);
		LOG.error("Unhandled exception", exception);
		ErrorResponse body = ErrorResponse.builder()
				.status(status)
				.error(Response.Status.fromStatusCode(status).getReasonPhrase())
				.message(exception.getMessage())
				.path(uriInfo != null ? uriInfo.getPath() : "")
				.timestamp(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
				.build();
		return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).entity(body).build();
	}

	/**
	 * Maps exception types to HTTP status codes.
	 * @param ex the thrown exception
	 * @return HTTP status code
	 */
	private int mapStatus(Throwable ex) {
		if (ex instanceof WebApplicationException wae) {
			return wae.getResponse() != null ? wae.getResponse().getStatus() : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
		}
		if (ex instanceof NotFoundException) return Response.Status.NOT_FOUND.getStatusCode();
		if (ex instanceof BadRequestException) return Response.Status.BAD_REQUEST.getStatusCode();
		return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
	}
}
