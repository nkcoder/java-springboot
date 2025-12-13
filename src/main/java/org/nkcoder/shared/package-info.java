/**
 * Shared kernel containing domain-driven design fundamentals.
 *
 * <p>This module provides:
 *
 * <ul>
 *   <li>Base domain event infrastructure
 *   <li>Aggregate root base class
 *   <li>Common exceptions
 *   <li>REST utilities (ApiResponse, GlobalExceptionHandler)
 * </ul>
 *
 * <p>This is a shared module - all other modules can access it.
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package org.nkcoder.shared;

import org.springframework.modulith.ApplicationModule;
