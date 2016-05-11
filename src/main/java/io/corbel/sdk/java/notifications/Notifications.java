package io.corbel.sdk.java.notifications;

import io.corbel.sdk.error.ApiError;
import scala.Unit;
import scala.util.Either;

import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * @author Ismael Madirolas (ismael.madirolas@devialab.com)
 */
public interface Notifications {

    CompletionStage<Either<ApiError, Unit>> sendNotification(String id, String recipient, Map<String, String> properties);
}
