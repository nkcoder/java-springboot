/**
 * Infrastructure module containing cross-cutting concerns.
 *
 * <p>This module provides:
 *
 * <ul>
 *   <li>Security configuration (JWT, CORS)
 *   <li>Web configuration
 *   <li>OpenAPI/Swagger configuration
 *   <li>JPA auditing configuration
 * </ul>
 *
 * <p>This is a shared module - all other modules can access it.
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package org.nkcoder.infrastructure;

import org.springframework.modulith.ApplicationModule;
